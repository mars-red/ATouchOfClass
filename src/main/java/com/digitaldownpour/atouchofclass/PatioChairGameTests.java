package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.PatioChairBlock;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PatioChairGameTests {
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    public static void chairsFaceAndSeatPlayerAndKeepRiderWhenDyed(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.setBlock(POS.below().relative(direction), Blocks.STONE);
        }
        helper.setBlock(POS.below(), Blocks.STONE);

        var white = ModBlocks.PATIO_CHAIRS.get(DyeColor.WHITE).get();
        var blue = ModBlocks.PATIO_CHAIRS.get(DyeColor.BLUE).get();
        for (Direction front : Direction.Plane.HORIZONTAL) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setYRot(front.getOpposite().toYRot());
            var chair = new ItemStack(white.asItem());
            var placement = new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false);
            chair.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, chair, placement));
            var state = level.getBlockState(pos);
            helper.assertTrue(state.is(white) && state.getValue(PatioChairBlock.FACING) == front,
                    Component.literal("Patio chair must face its placer"));

            helper.useBlock(POS, player);
            var seat = GameTestChecks.requireType(player.getVehicle(), SeatEntity.class);
            seat.tick();
            helper.assertTrue(!seat.isRemoved() && player.getYRot() == front.toYRot(),
                    Component.literal("The rider must sit facing the front"));

            var painter = helper.makeMockPlayer(GameType.SURVIVAL);
            var dye = new ItemStack(Items.DYE.pick(DyeColor.BLUE));
            var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            state.useItemOn(dye, level, painter, InteractionHand.MAIN_HAND, hit);
            var dyed = level.getBlockState(pos);
            helper.assertTrue(dyed.is(blue) && dyed.getValue(PatioChairBlock.FACING) == front
                            && dye.isEmpty() && player.getVehicle() == seat,
                    Component.literal("Dyeing must consume one dye while preserving orientation and rider"));

            var red = new ItemStack(Items.DYE.pick(DyeColor.RED));
            dyed.useItemOn(red, level, painter, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(level.getBlockState(pos).is(blue) && red.getCount() == 1,
                    Component.literal("Only a white chair can be dyed"));

            helper.setBlock(POS.relative(front), Blocks.STONE);
            player.stopRiding();
            helper.assertTrue(player.blockPosition().equals(pos.relative(front.getClockWise())) && level.noCollision(player),
                    Component.literal("Dismount must find an open neighboring space"));
            seat.tick();
            helper.assertTrue(seat.isRemoved(), Component.literal("The mount must be removed after dismount"));
            helper.setBlock(POS.relative(front), Blocks.AIR);
            level.destroyBlock(pos, false);
        }
        helper.succeed();
    }

    public static void adirondackLowSeatKeepsItsMountAndDismountsToOpenSide(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        helper.setBlock(POS.below(), Blocks.STONE);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.setBlock(POS.below().relative(direction), Blocks.STONE);
        }
        var chair = ModBlocks.SPRUCE_ADIRONDACK_CHAIR.get();
        for (Direction front : Direction.Plane.HORIZONTAL) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setYRot(front.getOpposite().toYRot());
            var stack = new ItemStack(ModItems.SPRUCE_ADIRONDACK_CHAIR_ITEM.get());
            var hit = new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false);
            stack.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack, hit));
            var state = level.getBlockState(pos);
            helper.assertTrue(state.is(chair) && state.getValue(PatioChairBlock.FACING) == front,
                    Component.literal("The Adirondack chair must face its placer"));

            helper.useBlock(POS, player);
            var seat = GameTestChecks.requireType(player.getVehicle(), SeatEntity.class);
            seat.tick();
            helper.assertTrue(!seat.isRemoved() && seat.blockPosition().equals(pos)
                            && player.getYRot() == front.toYRot()
                            && Math.abs(seat.getPassengerRidingPosition(player).y - pos.getY() - chair.seatHeight()) < 0.0001,
                    Component.literal("The rider must face forward and sit at the lowered seat height"));

            helper.setBlock(POS.relative(front), Blocks.STONE);
            player.stopRiding();
            helper.assertTrue(player.blockPosition().equals(pos.relative(front.getClockWise())) && level.noCollision(player),
                    Component.literal("Dismount must find the open side when the front is blocked"));
            seat.tick();
            helper.assertTrue(seat.isRemoved(), Component.literal("Dismount must clean up the mount"));
            helper.setBlock(POS.relative(front), Blocks.AIR);
            level.destroyBlock(pos, false);
        }
        helper.succeed();
    }

    public static void adirondackVariantsCraftWithMatchingPlanks(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        for (var wood : DiningChairVariants.VALUES) {
            String id = wood.wood() + "_adirondack_chair";
            var chair = ModBlocks.ADIRONDACK_CHAIRS.get(id).get();
            var item = ModItems.ADIRONDACK_CHAIR_ITEMS.get(id).get();
            helper.assertTrue(item.getBlock() == chair, Component.literal(id + " must place its matching block"));

            var planks = new ItemStack(BuiltInRegistries.ITEM.getValue(
                    Identifier.withDefaultNamespace(wood.wood() + "_planks")));
            var empty = ItemStack.EMPTY;
            var input = CraftingInput.of(3, 3, List.of(planks, empty, empty, planks, planks, empty, planks, empty, planks));
            var key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ATouchofClass.MODID, id));
            var recipe = (ShapedRecipe) server.getRecipeManager().byKey(key).orElseThrow().value();
            var result = recipe.assemble(input);
            helper.assertTrue(recipe.matches(input, level) && result.is(item) && result.getCount() == 2,
                    Component.literal(id + " must yield two chairs from matching planks"));
            var wrong = new ItemStack(wood.wood().equals("oak") ? Blocks.SPRUCE_PLANKS : Blocks.OAK_PLANKS);
            var mixed = CraftingInput.of(3, 3, List.of(wrong, empty, empty, planks, planks, empty, planks, empty, planks));
            helper.assertTrue(!recipe.matches(mixed, level), Component.literal(id + " must reject mixed planks"));
            helper.assertTrue(server.getAdvancements().get(Identifier.fromNamespaceAndPath(
                            ATouchofClass.MODID, "recipes/decorations/" + id)) != null,
                    Component.literal(id + " must have a recipe advancement"));
        }
        helper.succeed();
    }

    public static void allColorsCraftWithMatchingWoolOrDye(GameTestHelper helper) {
        var level = helper.getLevel();
        var iron = new ItemStack(Items.IRON_INGOT);
        var alternate = new ItemStack(Items.WOOL.pick(DyeColor.WHITE));
        var whiteChair = new ItemStack(ModItems.PATIO_CHAIR_ITEMS.get(DyeColor.WHITE).get());
        for (DyeColor color : DyeColor.values()) {
            var item = ModItems.PATIO_CHAIR_ITEMS.get(color).get();
            String name = color.getName() + "_patio_chair";
            var wool = new ItemStack(Items.WOOL.pick(color));
            var input = CraftingInput.of(3, 2, List.of(iron, wool, iron, iron, ItemStack.EMPTY, iron));
            var key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ATouchofClass.MODID, name));
            var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
            helper.assertTrue(recipe.matches(input, level), Component.literal(name + " must match its wool"));
            var result = recipe.assemble(input);
            helper.assertTrue(result.is(item) && result.getCount() == 1,
                    Component.literal(name + " must yield one matching chair"));
            if (color != DyeColor.WHITE) {
                var wrong = CraftingInput.of(3, 2, List.of(iron, alternate, iron, iron, ItemStack.EMPTY, iron));
                helper.assertTrue(!recipe.matches(wrong, level), Component.literal(name + " must reject white wool"));
                var dye = new ItemStack(Items.DYE.pick(color));
                var dyeInput = CraftingInput.of(2, 1, List.of(whiteChair, dye));
                var dyeKey = ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(ATouchofClass.MODID, name + "_from_dye"));
                var dyeRecipe = (ShapelessRecipe) level.getServer().getRecipeManager().byKey(dyeKey).orElseThrow().value();
                helper.assertTrue(dyeRecipe.matches(dyeInput, level) && dyeRecipe.assemble(dyeInput).is(item),
                        Component.literal(name + " must be obtainable by dyeing a white chair"));
            }
        }
        helper.succeed();
    }
}
