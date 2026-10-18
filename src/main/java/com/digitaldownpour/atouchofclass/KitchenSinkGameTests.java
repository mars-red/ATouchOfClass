package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.KitchenSinkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class KitchenSinkGameTests {
    private static final BlockPos ORIGIN = new BlockPos(5, 2, 5);

    private static void place(GameTestHelper helper, BlockPos pos, Direction front) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setYRot(front.getOpposite().toYRot());
        player.setShiftKeyDown(true);
        helper.setBlock(pos.below(), ModBlocks.OAK_DOOR_CABINET.get());
        var stack = new ItemStack(ModItems.KITCHEN_SINK.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos base = helper.absolutePos(pos.below());
        var context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atLowerCornerOf(base).add(.5, 1, .5), Direction.UP, base, false));
        var result = ModItems.KITCHEN_SINK.get().place(context);
        helper.assertTrue(result.consumesAction(),
                "sink must place at " + pos + " facing " + front + "; target="
                        + helper.getBlockState(pos) + "; clicked=" + context.getClickedPos()
                        + "; result=" + result);
        helper.assertTrue(helper.getBlockState(pos).is(ModBlocks.KITCHEN_SINK.get()),
                "placement must produce a kitchen sink");
    }

    private static void assertType(GameTestHelper helper, BlockPos pos, ChestType type) {
        helper.assertTrue(helper.getBlockState(pos).getValue(KitchenSinkBlock.TYPE) == type,
                "sink at " + pos + " must be " + type);
    }

    private static void clear(GameTestHelper helper, BlockPos... positions) {
        for (BlockPos pos : positions) helper.setBlock(pos, Blocks.AIR);
    }

    @GameTest(template = "chandelier_test")
    public static void pairInEitherOrderAndSplitInEveryFacing(GameTestHelper helper) {
        for (Direction front : Direction.Plane.HORIZONTAL) {
            BlockPos left = ORIGIN;
            BlockPos right = left.relative(front.getClockWise());
            for (boolean rightFirst : new boolean[] {false, true}) {
                for (boolean breakRight : new boolean[] {false, true}) {
                    clear(helper, left, right);
                    place(helper, rightFirst ? right : left, front);
                    assertType(helper, rightFirst ? right : left, ChestType.SINGLE);
                    place(helper, rightFirst ? left : right, front);
                    assertType(helper, left, ChestType.LEFT);
                    assertType(helper, right, ChestType.RIGHT);
                    helper.assertTrue(helper.getBlockState(left).getValue(KitchenSinkBlock.FACING) == front
                                    && helper.getBlockState(right).getValue(KitchenSinkBlock.FACING) == front,
                            "both halves must retain the placement facing");
                    clear(helper, breakRight ? right : left);
                    assertType(helper, breakRight ? left : right, ChestType.SINGLE);
                    helper.assertTrue(helper.getBlockState(left.below()).is(ModBlocks.OAK_DOOR_CABINET.get())
                                    && helper.getBlockState(right.below()).is(ModBlocks.OAK_DOOR_CABINET.get()),
                            "removing a sink must leave the cabinets intact");
                }
            }
            clear(helper, left, right);
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void rowsStopAtPairsAndBridgeChoosesOnlyOneNeighbor(GameTestHelper helper) {
        for (Direction front : Direction.Plane.HORIZONTAL) {
            Direction right = front.getClockWise();
            BlockPos a = ORIGIN;
            BlockPos b = a.relative(right);
            BlockPos c = b.relative(right);
            BlockPos d = c.relative(right);
            clear(helper, a, b, c, d);
            place(helper, a, front);
            place(helper, b, front);
            place(helper, c, front);
            assertType(helper, a, ChestType.LEFT);
            assertType(helper, b, ChestType.RIGHT);
            assertType(helper, c, ChestType.SINGLE);
            place(helper, d, front);
            assertType(helper, c, ChestType.LEFT);
            assertType(helper, d, ChestType.RIGHT);
            clear(helper, a, b, c, d);
            place(helper, a, front);
            place(helper, c, front);
            place(helper, b, front);
            assertType(helper, a, ChestType.LEFT);
            assertType(helper, b, ChestType.RIGHT);
            assertType(helper, c, ChestType.SINGLE);
            clear(helper, a, b, c);
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void mismatchedNeighborsAndRotatedPartnersDoNotJoin(GameTestHelper helper) {
        for (Direction front : Direction.Plane.HORIZONTAL) {
            BlockPos other = ORIGIN.relative(front.getClockWise());
            BlockPos behind = ORIGIN.relative(front.getOpposite());
            clear(helper, ORIGIN, other, behind);
            place(helper, ORIGIN, front);
            place(helper, other, front.getOpposite());
            assertType(helper, ORIGIN, ChestType.SINGLE);
            assertType(helper, other, ChestType.SINGLE);
            clear(helper, other);
            place(helper, other, front);
            assertType(helper, ORIGIN, ChestType.LEFT);
            helper.setBlock(other, helper.getBlockState(other).rotate(Rotation.CLOCKWISE_90));
            assertType(helper, ORIGIN, ChestType.SINGLE);
            clear(helper, ORIGIN, other);
            place(helper, ORIGIN, front);
            place(helper, behind, front);
            assertType(helper, ORIGIN, ChestType.SINGLE);
            assertType(helper, behind, ChestType.SINGLE);
            clear(helper, ORIGIN, behind);
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void shapesAndStructureTransformsKeepTheApronDecorative(GameTestHelper helper) {
        var block = ModBlocks.KITCHEN_SINK.get();
        for (Direction front : Direction.Plane.HORIZONTAL) for (ChestType type : ChestType.values()) {
            BlockState state = block.defaultBlockState().setValue(KitchenSinkBlock.FACING, front)
                    .setValue(KitchenSinkBlock.TYPE, type);
            var shape = state.getShape(helper.getLevel(), helper.absolutePos(ORIGIN));
            var collision = state.getCollisionShape(helper.getLevel(), helper.absolutePos(ORIGIN));
            helper.assertTrue(shape.min(Direction.Axis.Y) == 0 && collision.min(Direction.Axis.Y) == 0,
                    "selection and collision must not extend into the cabinet below");
            for (Rotation rotation : Rotation.values()) {
                var rotated = state.rotate(rotation);
                helper.assertTrue(rotated.getValue(KitchenSinkBlock.FACING) == rotation.rotate(front)
                                && rotated.getValue(KitchenSinkBlock.TYPE) == type,
                        "structure rotation must retain the half and turn its facing");
            }
            for (Mirror mirror : Mirror.values()) {
                var mirrored = state.mirror(mirror);
                helper.assertTrue(mirrored.getValue(KitchenSinkBlock.TYPE)
                                == (mirror == Mirror.NONE ? type : type.getOpposite()),
                        "reflection must exchange paired model halves");
                helper.assertTrue(mirrored.mirror(mirror).equals(state), "mirroring twice must restore the sink");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void recipeAndEachHalfDropOneSink(GameTestHelper helper) {
        var level = helper.getLevel();
        for (ChestType type : ChestType.values()) {
            BlockState state = ModBlocks.KITCHEN_SINK.get().defaultBlockState().setValue(KitchenSinkBlock.TYPE, type);
            var drops = Block.getDrops(state, level, helper.absolutePos(ORIGIN), null);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(ModItems.KITCHEN_SINK.get())
                            && drops.getFirst().getCount() == 1,
                    "each single or paired block must drop exactly one sink");
            helper.assertTrue(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(state),
                    "a pickaxe must harvest the sink");
            helper.assertTrue(!ItemStack.EMPTY.isCorrectToolForDrops(state),
                    "the sink must require a mining tool");
        }
        var key = ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "kitchen_sink"));
        var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var ingredients = recipe.getIngredients();
        helper.assertTrue(recipe.getWidth() == 3 && recipe.getHeight() == 3, "recipe must be a three-by-three pattern");
        helper.assertTrue(ingredients.get(1).orElseThrow().test(new ItemStack(Items.IRON_NUGGET)),
                "the top center must use an iron nugget");
        for (int slot : new int[] {3, 5, 6, 7, 8}) {
            helper.assertTrue(ingredients.get(slot).orElseThrow().test(new ItemStack(Items.IRON_INGOT)),
                    "the basin pattern must use five iron ingots");
        }
        var iron = new ItemStack(Items.IRON_INGOT);
        var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, java.util.List.of(
                ItemStack.EMPTY, new ItemStack(Items.IRON_NUGGET), ItemStack.EMPTY,
                iron, ItemStack.EMPTY, iron, iron, iron, iron));
        helper.assertTrue(recipe.matches(input, level), "the documented ingredients must match the recipe");
        var output = recipe.assemble(input, level.registryAccess());
        helper.assertTrue(output.is(ModItems.KITCHEN_SINK.get()) && output.getCount() == 1,
                "crafting must produce exactly one kitchen sink");
        helper.assertTrue(level.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(
                ATouchofClass.MODID, "recipes/building_blocks/kitchen_sink")) != null,
                "the recipe unlock advancement must load");
        helper.succeed();
    }
}
