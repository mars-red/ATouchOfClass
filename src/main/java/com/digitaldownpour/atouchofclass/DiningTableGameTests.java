package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.DiningTableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class DiningTableGameTests {
    private static final BlockPos ORIGIN = new BlockPos(2, 1, 2);

    private static void place(GameTestHelper helper, int x, int z) {
        BlockPos pos = ORIGIN.offset(x, 0, z);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, ModBlocks.OAK_DINING_TABLE.get().defaultBlockState());
    }

    private static int legs(GameTestHelper helper, int x, int z) {
        var state = helper.getLevel().getBlockState(helper.absolutePos(ORIGIN.offset(x, 0, z)));
        int count = 0;
        for (var leg : DiningTableBlock.LEGS) if (state.getValue(leg)) count++;
        return count;
    }

    @GameTest(template = "chandelier_test")
    public static void rectangleHasFourLegsAndContinuousTop(GameTestHelper helper) {
        for (int x = 0; x < 2; x++) for (int z = 0; z < 3; z++) place(helper, x, z);
        int total = 0;
        for (int x = 0; x < 2; x++) for (int z = 0; z < 3; z++) total += legs(helper, x, z);
        helper.assertTrue(total == 4, "a 2x3 table must have exactly four outside legs");
        helper.assertTrue(legs(helper, 0, 1) == 0 && legs(helper, 1, 1) == 0, "straight edges must have no legs");
        BlockPos pos = helper.absolutePos(ORIGIN.offset(0, 0, 1));
        var state = helper.getLevel().getBlockState(pos);
        var shape = state.getCollisionShape(helper.getLevel(), pos);
        helper.assertTrue(!Shapes.joinIsNotEmpty(Shapes.box(0, 13.0 / 16, 0, 1, 1, 1), shape, BooleanOp.ONLY_FIRST),
                "joined top must fill its corners with no seam holes");
        helper.assertTrue(!Shapes.joinIsNotEmpty(Shapes.box(0, 0, 0, 1, 10.0 / 16, 1), shape, BooleanOp.AND),
                "legless sections must have open collision underneath");
        helper.assertTrue(state.isFaceSturdy(helper.getLevel(), pos, Direction.UP), "top must support decoration");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void diagonalRemovalCreatesAndRefillsInsideCorner(GameTestHelper helper) {
        for (int x = 0; x < 2; x++) for (int z = 0; z < 2; z++) place(helper, x, z);
        helper.setBlock(ORIGIN.offset(1, 0, 1), Blocks.AIR);
        helper.runAfterDelay(2, () -> {
            var state = helper.getLevel().getBlockState(helper.absolutePos(ORIGIN));
            helper.assertTrue(state.getValue(DiningTableBlock.LEGS[1]), "diagonal removal must add the concave southeast leg");
            helper.assertTrue(legs(helper, 0, 0) + legs(helper, 1, 0) + legs(helper, 0, 1) == 6,
                    "L shape must have five convex legs and one concave leg");
            place(helper, 1, 1);
            state = helper.getLevel().getBlockState(helper.absolutePos(ORIGIN));
            helper.assertTrue(!state.getValue(DiningTableBlock.LEGS[1]), "filling the diagonal must remove the concave leg");
            helper.succeed();
        });
    }

    @GameTest(template = "chandelier_test")
    public static void isolatedMatchesSideTableAndDoesNotJoinOtherFurniture(GameTestHelper helper) {
        place(helper, 0, 0);
        helper.setBlock(ORIGIN.east().below(), Blocks.STONE);
        helper.setBlock(ORIGIN.east(), ModBlocks.OAK_SIDE_TABLE.get().defaultBlockState());
        helper.assertTrue(legs(helper, 0, 0) == 4, "side tables must not connect to dining tables");
        BlockPos pos = helper.absolutePos(ORIGIN);
        var state = helper.getLevel().getBlockState(pos);
        var diningShape = state.getShape(helper.getLevel(), pos);
        var sideShape = ModBlocks.OAK_SIDE_TABLE.get().defaultBlockState().getShape(helper.getLevel(), pos);
        helper.assertTrue(!Shapes.joinIsNotEmpty(diningShape, sideShape, BooleanOp.NOT_SAME),
                "isolated dining-table geometry must match side-table geometry");
        helper.setBlock(ORIGIN.below(), Blocks.AIR);
        helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), "unsupported dining tables must break");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void holeHasFourInsideCornersAndTransformsPreserveState(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) if (x != 1 || z != 1) place(helper, x, z);
        int total = 0;
        for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) if (x != 1 || z != 1) total += legs(helper, x, z);
        helper.assertTrue(total == 8, "ring must have four outside and four inside corner legs");
        for (var state : ModBlocks.OAK_DINING_TABLE.get().getStateDefinition().getPossibleStates()) {
            var transformed = state;
            for (int i = 0; i < 4; i++) transformed = transformed.rotate(Rotation.CLOCKWISE_90);
            helper.assertTrue(transformed == state, "four rotations must restore every connection and leg");
            for (Mirror mirror : Mirror.values()) helper.assertTrue(state.mirror(mirror).mirror(mirror) == state,
                    "two reflections must restore every connection and leg");
        }
        helper.succeed();
    }
}
