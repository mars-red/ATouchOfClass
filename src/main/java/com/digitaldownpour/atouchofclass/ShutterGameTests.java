package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.ShutterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class ShutterGameTests {
    private static BlockState shutter(Direction facing, DoorHingeSide hinge) {
        return ModBlocks.OAK_SHUTTER.get().defaultBlockState()
                .setValue(ShutterBlock.FACING, facing).setValue(ShutterBlock.HINGE, hinge);
    }

    private static void toggle(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        ModBlocks.OAK_SHUTTER.get().useWithoutItem(level.getBlockState(pos), level, pos,
                helper.makeMockPlayer(GameType.SURVIVAL),
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    }

    private static void assertOpen(GameTestHelper helper, BlockPos pos, boolean expected) {
        helper.assertTrue(helper.getLevel().getBlockState(pos).getValue(ShutterBlock.OPEN) == expected,
                "Unexpected shutter open state at " + pos);
    }

    @GameTest(template = "chandelier_test")
    public static void wallPlacementUsesFaceAndClickedHinge(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (DoorHingeSide hinge : DoorHingeSide.values()) {
                BlockPos pos = wall.relative(facing);
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                Direction clickedSide = hinge == DoorHingeSide.LEFT
                        ? facing.getClockWise() : facing.getCounterClockWise();
                Vec3 click = Vec3.atCenterOf(wall).add(facing.getStepX() * .5, 0, facing.getStepZ() * .5)
                        .add(clickedSide.getStepX() * .25, 0, clickedSide.getStepZ() * .25);
                var item = new ItemStack(ModItems.OAK_SHUTTER.get());
                player.setItemInHand(InteractionHand.MAIN_HAND, item);
                // Deliberately look in a direction unrelated to the clicked wall face.
                player.setYRot(37);
                var hit = new BlockHitResult(click, facing, wall, false);
                helper.assertTrue(item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit))
                        .consumesAction(), "Oak shutter must place on " + facing);
                var state = level.getBlockState(pos);
                helper.assertTrue(state.is(ModBlocks.OAK_SHUTTER.get())
                        && state.getValue(ShutterBlock.FACING) == facing
                        && state.getValue(ShutterBlock.HINGE) == hinge, "Face and click must choose facing and hinge");
                AABB closed = state.getShape(level, pos).bounds();
                Direction back = facing.getOpposite();
                double touching = back.getAxisDirection() == Direction.AxisDirection.POSITIVE
                        ? closed.max(back.getAxis()) : closed.min(back.getAxis());
                helper.assertTrue(touching == (back.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0),
                        "Closed shutter must touch the clicked wall face");
                toggle(helper, pos);
                AABB open = level.getBlockState(pos).getShape(level, pos).bounds();
                double hingeEdge = clickedSide.getAxisDirection() == Direction.AxisDirection.POSITIVE
                        ? open.max(clickedSide.getAxis()) : open.min(clickedSide.getAxis());
                helper.assertTrue(hingeEdge == (clickedSide.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0),
                        "Open shutter must pivot around the clicked side");
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void joinedColumnTogglesFromAnySection(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(2, 1, 2));
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (DoorHingeSide hinge : DoorHingeSide.values()) {
                for (int i = 0; i < 3; i++) level.setBlockAndUpdate(base.above(i), shutter(facing, hinge));
                for (int i = 0; i < 3; i++) {
                    var state = level.getBlockState(base.above(i));
                    helper.assertTrue(state.getValue(ShutterBlock.JOINED_BELOW) == (i > 0)
                            && state.getValue(ShutterBlock.JOINED_ABOVE) == (i < 2), "Vertical caps must track neighbors");
                    toggle(helper, base.above(i));
                    for (int j = 0; j < 3; j++) assertOpen(helper, base.above(j), i % 2 == 0);
                }
                // Deliberately mix states: use must set the whole group, not invert each panel.
                level.setBlockAndUpdate(base.above(), level.getBlockState(base.above()).setValue(ShutterBlock.OPEN, false));
                toggle(helper, base.above());
                for (int i = 0; i < 3; i++) assertOpen(helper, base.above(i), true);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void gapsAndMismatchedHingesOrFacingsStopJoining(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(2, 1, 2));
        var state = shutter(Direction.NORTH, DoorHingeSide.LEFT);
        for (BlockState barrier : new BlockState[]{Blocks.AIR.defaultBlockState(),
                shutter(Direction.NORTH, DoorHingeSide.RIGHT), shutter(Direction.EAST, DoorHingeSide.LEFT)}) {
            level.setBlockAndUpdate(base, state);
            level.setBlockAndUpdate(base.above(), barrier);
            level.setBlockAndUpdate(base.above(2), state);
            helper.assertTrue(!level.getBlockState(base).getValue(ShutterBlock.JOINED_ABOVE), "Mismatch must retain an end cap");
            toggle(helper, base);
            assertOpen(helper, base, true);
            assertOpen(helper, base.above(2), false);
            if (!barrier.isAir()) assertOpen(helper, base.above(), false);
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void placementBridgesGroupsAndExtendsVertically(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos base = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockState state = shutter(Direction.NORTH, DoorHingeSide.LEFT);
        level.setBlockAndUpdate(base, state.setValue(ShutterBlock.OPEN, true));
        level.setBlockAndUpdate(base.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(base.above(2), state);
        BlockPos wall = base.above().south();
        level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        var item = new ItemStack(ModItems.OAK_SHUTTER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        var hit = new BlockHitResult(Vec3.atCenterOf(wall).add(.25, 0, -.5), Direction.NORTH, wall, false);
        helper.assertTrue(item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit))
                .consumesAction(), "Placement into the gap must succeed");
        for (int i = 0; i < 3; i++) assertOpen(helper, base.above(i), true);
        item = new ItemStack(ModItems.OAK_SHUTTER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        BlockPos top = base.above(2);
        var topHit = new BlockHitResult(Vec3.atCenterOf(top).add(.25, .5, 0), Direction.UP, top, false);
        helper.assertTrue(item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, topHit))
                .consumesAction(), "Top-face placement must extend the shutter stack");
        var placed = level.getBlockState(base.above(3));
        helper.assertTrue(placed.getValue(ShutterBlock.FACING) == Direction.NORTH
                && placed.getValue(ShutterBlock.HINGE) == DoorHingeSide.LEFT
                && placed.getValue(ShutterBlock.OPEN), "Extension must preserve the clicked hinge and group state");
        level.destroyBlock(wall, false);
        for (int i = 0; i < 4; i++) helper.assertTrue(level.getBlockState(base.above(i)).is(ModBlocks.OAK_SHUTTER.get()),
                "Shutters must not cascade when their mounting block is removed");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void recipeCraftsEightAndMaterialUnlocksIt(GameTestHelper helper) {
        var level = helper.getLevel();
        var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "oak_shutter");
        var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id);
        var holder = level.getServer().getRecipeManager().byKey(key).orElseThrow();
        helper.assertTrue(holder.value() instanceof net.minecraft.world.item.crafting.ShapedRecipe, "Shutter recipe must load");
        var recipe = (net.minecraft.world.item.crafting.ShapedRecipe) holder.value();
        var wood = new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS);
        var stick = new ItemStack(net.minecraft.world.item.Items.STICK);
        var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 2,
                java.util.List.of(wood, stick, wood, wood, stick, wood));
        helper.assertTrue(recipe.matches(input, level), "Oak planks and sticks must match the recipe");
        var result = recipe.assemble(input, level.registryAccess());
        helper.assertTrue(result.is(ModItems.OAK_SHUTTER.get()) && result.getCount() == 8, "Recipe must craft eight shutters");
        // Use a real ServerPlayer for advancement awards, with a no-op test
        // connection and without joining the world (which sends overlay payloads).
        var player = new net.minecraft.server.level.ServerPlayer(level.getServer(), level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "shutter-recipe-test"),
                net.minecraft.server.level.ClientInformation.createDefault());
        player.connection = new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "shutter-test-connection")).connection;
        var advancement = level.getServer().getAdvancements().get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "recipes/building_blocks/oak_shutter"));
        helper.assertTrue(advancement != null, "Shutter unlock advancement must load");
        player.getInventory().add(wood.copy());
        net.minecraft.advancements.CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), wood);
        helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone()
                && player.getRecipeBook().contains(key), "Obtaining oak planks must unlock the shutter recipe");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void breakingAnySectionDropsOnlyThatShutter(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new net.neoforged.neoforge.common.util.FakePlayer(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "shutter-drop-test"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        BlockPos base = helper.absolutePos(new BlockPos(2, 1, 2));
        for (boolean open : new boolean[]{false, true}) {
            for (int broken = 0; broken < 3; broken++) {
                for (int i = 0; i < 3; i++) level.setBlockAndUpdate(base.above(i),
                        shutter(Direction.NORTH, DoorHingeSide.LEFT).setValue(ShutterBlock.OPEN, open));
                helper.assertTrue(player.gameMode.destroyBlock(base.above(broken)), "Survival break must succeed");
                for (int i = 0; i < 3; i++) {
                    var remaining = level.getBlockState(base.above(i));
                    helper.assertTrue(i == broken ? remaining.isAir() : remaining.is(ModBlocks.OAK_SHUTTER.get()),
                            "Breaking one section must leave all others intact");
                    if (i != broken) {
                        helper.assertTrue(remaining.getValue(ShutterBlock.JOINED_ABOVE) == (i < 2 && i+1 != broken)
                                && remaining.getValue(ShutterBlock.JOINED_BELOW) == (i > 0 && i-1 != broken),
                                "Neighbors must restore caps at the broken section");
                    }
                }
                var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(1, 3, 1));
                helper.assertTrue(drops.size() == 1 && drops.getFirst().getItem().is(ModItems.OAK_SHUTTER.get())
                        && drops.getFirst().getItem().getCount() == 1, "Exactly one oak shutter must drop");
                drops.forEach(ItemEntity::discard);
            }
        }
        // Creative breaking must also remain independent and produce no items.
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        for (int i = 0; i < 3; i++) level.setBlockAndUpdate(base.above(i), shutter(Direction.NORTH, DoorHingeSide.LEFT));
        player.gameMode.destroyBlock(base.above());
        helper.assertTrue(level.getBlockState(base).is(ModBlocks.OAK_SHUTTER.get())
                && level.getBlockState(base.above(2)).is(ModBlocks.OAK_SHUTTER.get()), "Creative break must also be independent");
        helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(1, 3, 1)).isEmpty(),
                "Creative breaking must not drop a shutter");
        helper.succeed();
    }
}
