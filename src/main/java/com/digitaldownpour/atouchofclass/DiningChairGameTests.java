package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.DiningChairBlock;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class DiningChairGameTests {
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    @GameTest(template = "chandelier_test")
    public static void everyWoodSeatsFacesAndDismountsInEveryDirection(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) helper.setBlock(POS.offset(x, -1, z), Blocks.STONE);
        for (var block : ModBlocks.DINING_CHAIRS.values()) for (Direction front : Direction.Plane.HORIZONTAL) {
            helper.setBlock(POS, block.get().defaultBlockState().setValue(DiningChairBlock.FACING, front));
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            player.setYRot(front.getOpposite().toYRot());
            helper.useBlock(POS, player);
            helper.assertTrue(player.getVehicle() instanceof SeatEntity, "every wood must seat the player");
            var seat = (SeatEntity) player.getVehicle();
            seat.tick();
            helper.assertTrue(!seat.isRemoved() && player.getYRot() == front.toYRot()
                    && player.getYHeadRot() == front.toYRot() && player.yBodyRot == front.toYRot(),
                    "all wood seats must survive ticking and face their visual front");
            var other = helper.makeMockPlayer(GameType.CREATIVE);
            helper.useBlock(POS, other);
            helper.assertTrue(!other.isPassenger(), "every wood must reject a second rider");
            player.stopRiding();
            helper.assertTrue(player.blockPosition().equals(pos.relative(front)) && level.noCollision(player),
                    "every wood must dismount onto open floor without jumping");
            seat.tick();
            helper.assertTrue(seat.isRemoved(), "every wood must clean up after dismount");

            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                helper.setBlock(POS.offset(x, 0, z), Blocks.STONE);
                helper.setBlock(POS.offset(x, 1, z), Blocks.STONE);
            }
            helper.useBlock(POS, other);
            helper.assertTrue(other.getVehicle() instanceof SeatEntity, "every wood must remain reusable");
            seat = (SeatEntity) other.getVehicle();
            other.stopRiding();
            helper.assertTrue(other.blockPosition().equals(pos) && other.getY() > pos.getY() + 10.0 / 16
                    && level.noCollision(other), "every wood must use the clear above-seat fallback when surrounded");
            seat.tick();
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                helper.setBlock(POS.offset(x, 0, z), Blocks.AIR);
                helper.setBlock(POS.offset(x, 1, z), Blocks.AIR);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void everyWoodHasCorrectRecipeItemMaterialAndDrop(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        helper.setBlock(POS.below(), Blocks.STONE);
        helper.assertTrue(ModBlocks.DINING_CHAIRS.size() == 12 && ModItems.DINING_CHAIR_ITEMS.size() == 12,
                "all twelve vanilla woods must have dining-chair blocks and items");
        for (var definition : DiningChairVariants.VALUES) {
            var block = ModBlocks.DINING_CHAIRS.get(definition.id()).get();
            var item = ModItems.DINING_CHAIR_ITEMS.get(definition.id()).get();
            helper.assertTrue(item.getBlock() == block && block.asItem() == item, "item must place matching wood");
            helper.assertTrue(block.defaultBlockState().ignitedByLava() == definition.sourceBlock().defaultBlockState().ignitedByLava(),
                    "chair must match source wood fire behavior");
            var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, definition.id());
            var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id);
            var recipe = (net.minecraft.world.item.crafting.ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
            var stick = new ItemStack(Items.STICK);
            var slab = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                    net.minecraft.resources.ResourceLocation.withDefaultNamespace(definition.wood() + "_slab")));
            var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3,
                    java.util.List.of(stick, stick, stick, slab, slab, slab, stick, ItemStack.EMPTY, stick));
            helper.assertTrue(recipe.matches(input, level), definition.id() + " must craft with matching slabs and sticks");
            var output = recipe.assemble(input, level.registryAccess());
            helper.assertTrue(output.is(item) && output.getCount() == 2, "every recipe must yield two matching chairs");
            if (!definition.wood().equals("oak")) {
                var wrong = net.minecraft.world.item.crafting.CraftingInput.of(3, 3,
                        java.util.List.of(stick, stick, stick, slab, new ItemStack(Items.OAK_SLAB), slab, stick, ItemStack.EMPTY, stick));
                helper.assertTrue(!recipe.matches(wrong, level), "mixed slab varieties must not match");
            }
            helper.assertTrue(level.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    ATouchofClass.MODID, "recipes/decorations/" + definition.id())) != null, "each recipe advancement must load");
            helper.setBlock(POS, block.defaultBlockState());
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            helper.useBlock(POS, player);
            level.destroyBlock(pos, true);
            helper.assertTrue(!player.isPassenger() && level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.25)).isEmpty(),
                    "every wood must dismount and clean up on break");
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1));
            helper.assertTrue(drops.stream().filter(drop -> drop.getItem().is(item)).mapToInt(drop -> drop.getItem().getCount()).sum() == 1,
                    "each wood must drop exactly one matching chair");
            drops.forEach(ItemEntity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void dismountStepsClearOfChairInEveryFacing(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) helper.setBlock(POS.offset(x, -1, z), Blocks.STONE);
        for (Direction front : Direction.Plane.HORIZONTAL) {
            helper.setBlock(POS, ModBlocks.OAK_DINING_CHAIR.get().defaultBlockState().setValue(DiningChairBlock.FACING, front));
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            helper.useBlock(POS, player);
            var seat = (SeatEntity) player.getVehicle();
            player.stopRiding();
            helper.assertTrue(player.blockPosition().equals(pos.relative(front)), "dismount must prefer open floor in front of " + front);
            helper.assertTrue(Math.abs(player.getY() - pos.getY()) < 0.0001 && level.noCollision(player),
                    "dismount must place the player outside chair collision without jumping");
            seat.tick();
            helper.assertTrue(seat.isRemoved(), "dismount must still clean up the seat");
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void dismountAvoidsTableAndBlockedSide(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) helper.setBlock(POS.offset(x, -1, z), Blocks.STONE);
        for (Direction front : Direction.Plane.HORIZONTAL) {
            helper.setBlock(POS, ModBlocks.OAK_DINING_CHAIR.get().defaultBlockState().setValue(DiningChairBlock.FACING, front));
            helper.setBlock(POS.relative(front), ModBlocks.OAK_DINING_TABLE.get().defaultBlockState());
            helper.setBlock(POS.relative(front.getClockWise()), Blocks.STONE);
            helper.setBlock(POS.relative(front.getClockWise()).above(), Blocks.STONE);
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            helper.useBlock(POS, player);
            var seat = (SeatEntity) player.getVehicle();
            player.stopRiding();
            helper.assertTrue(player.blockPosition().equals(pos.relative(front.getCounterClockWise())) && level.noCollision(player),
                    "a table in front and wall on the right must leave the clear left exit preferred");
            seat.tick();
            helper.setBlock(POS.relative(front), Blocks.AIR);
            helper.setBlock(POS.relative(front.getClockWise()), Blocks.AIR);
            helper.setBlock(POS.relative(front.getClockWise()).above(), Blocks.AIR);
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void blockedExitsDismountAboveSeatInsteadOfInsideChair(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        helper.setBlock(POS.below(), Blocks.STONE);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            if (x == 0 && z == 0) continue;
            helper.setBlock(POS.offset(x, 0, z), Blocks.STONE);
            helper.setBlock(POS.offset(x, 1, z), Blocks.STONE);
        }
        for (Direction front : Direction.Plane.HORIZONTAL) {
            helper.setBlock(POS, ModBlocks.OAK_DINING_CHAIR.get().defaultBlockState().setValue(DiningChairBlock.FACING, front));
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            helper.useBlock(POS, player);
            var seat = (SeatEntity) player.getVehicle();
            player.stopRiding();
            helper.assertTrue(player.blockPosition().equals(pos) && player.getY() > pos.getY() + 10.0 / 16,
                    "when every side is blocked the player must dismount slightly above the seat");
            helper.assertTrue(level.noCollision(player), "above-seat fallback must clear the seat and the tall back without a jump");
            seat.tick();
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void seatingFacesFrontInEveryDirectionAndCleansUp(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(POS);
        var bounds = new AABB(pos).inflate(0.25);
        helper.setBlock(POS.below(), Blocks.STONE);
        for (Direction front : Direction.Plane.HORIZONTAL) {
            helper.setBlock(POS, ModBlocks.OAK_DINING_CHAIR.get().defaultBlockState().setValue(DiningChairBlock.FACING, front));
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            player.setYRot(front.getOpposite().toYRot());
            player.setYHeadRot(front.getOpposite().toYRot());
            player.setYBodyRot(front.getOpposite().toYRot());
            helper.useBlock(POS, player);
            var seats = level.getEntitiesOfClass(SeatEntity.class, bounds);
            helper.assertTrue(seats.size() == 1 && seats.getFirst().hasPassenger(player), "chair must seat the player");
            var seat = seats.getFirst();
            seat.tick();
            helper.assertTrue(!seat.isRemoved(), "occupied dining chair must retain its mount");
            helper.assertTrue(player.getYRot() == front.toYRot() && player.getYHeadRot() == front.toYRot()
                    && player.yBodyRot == front.toYRot(), "player head and body must face the chair front in " + front);
            helper.assertTrue(Math.abs(seat.getY() + 0.55 - (pos.getY() + 10.0 / 16)) < 0.0001,
                    "mount attachment must line up with the 10-unit seat top");
            var other = helper.makeMockPlayer(GameType.CREATIVE);
            helper.useBlock(POS, other);
            helper.assertTrue(!other.isPassenger() && seat.hasPassenger(player)
                    && level.getEntitiesOfClass(SeatEntity.class, bounds).size() == 1,
                    "occupied chairs must reject another rider without duplicating mounts");
            player.stopRiding();
            seat.tick();
            helper.assertTrue(seat.isRemoved(), "empty mounts must disappear after dismount");
            helper.useBlock(POS, other);
            helper.assertTrue(other.isPassenger(), "chair must be reusable after dismount");
            level.destroyBlock(pos, false);
            helper.assertTrue(!other.isPassenger() && level.getEntitiesOfClass(SeatEntity.class, bounds).isEmpty(),
                    "breaking the chair must dismount riders and remove the mount");
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void actualPlacementFacesPlayerAndHasMatchingTallBack(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        helper.setBlock(POS.below(), Blocks.STONE);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (Direction front : Direction.Plane.HORIZONTAL) {
            player.setYRot(front.getOpposite().toYRot());
            var stack = new ItemStack(ModItems.OAK_DINING_CHAIR_ITEM.get());
            var hit = new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false);
            stack.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack, hit));
            var state = level.getBlockState(pos);
            helper.assertTrue(state.is(ModBlocks.OAK_DINING_CHAIR.get()) && state.getValue(DiningChairBlock.FACING) == front,
                    "item placement must face the chair front toward its placer");
            var shape = state.getCollisionShape(level, pos);
            helper.assertTrue(Math.abs(shape.max(Direction.Axis.Y) - 1.5) < 0.0001, "back must reach 24 units");
            var back = front.getOpposite();
            double x = 0.5 + back.getStepX() * 0.3125, z = 0.5 + back.getStepZ() * 0.3125;
            helper.assertTrue(Shapes.joinIsNotEmpty(shape, Shapes.box(x-.01, 1.35, z-.01, x+.01, 1.4, z+.01), BooleanOp.AND),
                    "upper collision must follow the back, opposite the visual front");
            helper.assertTrue(!Shapes.joinIsNotEmpty(shape, Shapes.box(1-x-.01, 1.35, 1-z-.01, 1-x+.01, 1.4, 1-z+.01), BooleanOp.AND),
                    "upper front must remain open");
            level.destroyBlock(pos, false);
        }
        helper.setBlock(POS.above(), Blocks.STONE);
        var stack = new ItemStack(ModItems.OAK_DINING_CHAIR_ITEM.get());
        var hit = new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false);
        stack.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack, hit));
        helper.assertTrue(level.getBlockState(pos).isAir() && stack.getCount() == 1,
                "chair must reject blocked headroom without consuming the item");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void supportLossDismountsAndDropsOneChair(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(POS);
        helper.setBlock(POS.below(), Blocks.STONE);
        helper.setBlock(POS, ModBlocks.OAK_DINING_CHAIR.get().defaultBlockState());
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        helper.useBlock(POS, player);
        helper.setBlock(POS.below(), Blocks.AIR);
        helper.assertTrue(level.getBlockState(pos).isAir() && !player.isPassenger(), "support loss must remove chair and rider");
        var bounds = new AABB(pos).inflate(1);
        helper.assertTrue(level.getEntitiesOfClass(SeatEntity.class, bounds).isEmpty(), "support loss must not leave a mount");
        int count = level.getEntitiesOfClass(ItemEntity.class, bounds).stream()
                .filter(item -> item.getItem().is(ModItems.OAK_DINING_CHAIR_ITEM.get()))
                .mapToInt(item -> item.getItem().getCount()).sum();
        helper.assertTrue(count == 1, "support loss must drop exactly one chair");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void stickAndOakSlabRecipeMakesTwoChairs(GameTestHelper helper) {
        var level = helper.getLevel();
        var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "oak_dining_chair");
        var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id);
        var recipe = (net.minecraft.world.item.crafting.ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var stick = new ItemStack(Items.STICK);
        var slab = new ItemStack(Items.OAK_SLAB);
        var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3,
                java.util.List.of(stick, stick, stick, slab, slab, slab, stick, ItemStack.EMPTY, stick));
        helper.assertTrue(recipe.matches(input, level), "TTT / LLL / T T must match sticks and oak slabs");
        var output = recipe.assemble(input, level.registryAccess());
        helper.assertTrue(output.is(ModItems.OAK_DINING_CHAIR_ITEM.get()) && output.getCount() == 2, "recipe must yield two chairs");
        for (var wrong : java.util.List.of(Items.SPRUCE_SLAB, Items.STRIPPED_OAK_WOOD, Items.OAK_PLANKS)) {
            var wrongInput = net.minecraft.world.item.crafting.CraftingInput.of(3, 3,
                    java.util.List.of(stick, stick, stick, slab, new ItemStack(wrong), slab, stick, ItemStack.EMPTY, stick));
            helper.assertTrue(!recipe.matches(wrongInput, level), "oak slabs must not be substituted by " + wrong);
        }
        helper.assertTrue(level.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                ATouchofClass.MODID, "recipes/decorations/oak_dining_chair")) != null, "recipe advancement must load");
        helper.succeed();
    }
}
