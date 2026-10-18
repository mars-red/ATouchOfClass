package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.BaseboardBlock;
import com.digitaldownpour.atouchofclass.blocks.CasingBlock;
import com.digitaldownpour.atouchofclass.blocks.CrownMouldingBlock;
import com.digitaldownpour.atouchofclass.blocks.PlinthBlock;
import com.digitaldownpour.atouchofclass.blocks.WindowApronBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

public class BaseboardGameTests {

    public static void uprightPlinthsCreateBaseboardCorners(GameTestHelper helper) {
        checkCorners(helper, false);
    }

    public static void apronsWithBaseboardCreateCorners(GameTestHelper helper) {
        checkCorners(helper, true);
    }

    private static void checkCorners(GameTestHelper helper, boolean apron) {
        BlockPos pos = new BlockPos(3, 1, 3);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (boolean inner : new boolean[] {false, true}) {
                Direction offset = inner ? facing : facing.getOpposite();
                for (Direction other : new Direction[] {facing.getCounterClockWise(), facing.getClockWise()}) {
                    for (CasingBlock.Edge edge : new CasingBlock.Edge[] {CasingBlock.Edge.LEFT, CasingBlock.Edge.RIGHT}) {
                        for (boolean baseboardFirst : new boolean[] {false, true}) {
                            BlockPos neighborPos = pos.relative(offset);
                            BlockPos sourcePos = neighborPos.relative(offset);
                            BlockState board = ModBlocks.BASEBOARD.get().defaultBlockState()
                                    .setValue(BaseboardBlock.FACING, facing);
                            if (baseboardFirst) helper.setBlock(pos, board);
                            BlockState neighbor = (apron ? ModBlocks.WINDOW_APRON.get() : ModBlocks.PLINTH_BLOCK.get())
                                    .defaultBlockState().setValue(CasingBlock.FACING, other);
                            if (!apron) neighbor = neighbor.setValue(CasingBlock.EDGE, edge);
                            helper.setBlock(neighborPos, neighbor);
                            if (apron) {
                                // A real baseboard source enables the apron's combined variant.
                                helper.setBlock(sourcePos, board.setValue(BaseboardBlock.FACING, other));
                            }
                            if (apron) helper.assertTrue(state(helper, neighborPos).getValue(WindowApronBlock.BASEBOARD), Component.literal("the apron must have its baseboard variant enabled by the source"));
                            boolean left = other == facing.getCounterClockWise();
                            StairsShape expected = inner
                                    ? (left ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT)
                                    : (left ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT);
                            if (!baseboardFirst) {
                                BlockPos support = pos.relative(facing.getOpposite());
                                boolean temporarySupport = state(helper, support).isAir();
                                if (temporarySupport) helper.setBlock(support, Blocks.STONE);
                                var player = helper.makeMockPlayer(GameType.CREATIVE);
                                BlockPos absoluteSupport = helper.absolutePos(support);
                                var hit = new BlockHitResult(Vec3.atCenterOf(absoluteSupport), facing, absoluteSupport, false);
                                var context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                                        new ItemStack(ModBlocks.BASEBOARD.get()), hit);
                                BlockState placed = ModBlocks.BASEBOARD.get().getStateForPlacement(context);
                                helper.assertTrue(placed != null && placed.getValue(BaseboardBlock.SHAPE) == expected, Component.literal("placing baseboard next to connected trim must immediately select " + expected));
                                helper.setBlock(pos, placed);
                                if (temporarySupport) helper.setBlock(support, Blocks.AIR);
                            }
                            helper.assertTrue(state(helper, pos).getValue(BaseboardBlock.SHAPE) == expected, Component.literal("connected trim must select " + expected + " in either placement order"));
                            Direction gap = edge == CasingBlock.Edge.RIGHT
                                    ? other.getClockWise() : other.getCounterClockWise();
                            boolean stub = !apron && gap == offset.getOpposite();
                            if (!apron) {
                                helper.assertTrue(state(helper, neighborPos).getValue(PlinthBlock.BASEBOARD) == stub, Component.literal("a plinth's open side must show the stub when the corner return reaches it"));
                            }
                            // A same-facing continuation overrides a corner, including mixed trim.
                            Direction continuation = inner ? other.getOpposite() : other;
                            BlockPos continuationPos = pos.relative(continuation);
                            helper.setBlock(continuationPos, ModBlocks.PLINTH_BLOCK.get().defaultBlockState()
                                    .setValue(CasingBlock.FACING, facing).setValue(CasingBlock.EDGE, edge));
                            helper.assertTrue(state(helper, pos).getValue(BaseboardBlock.SHAPE) == StairsShape.STRAIGHT, Component.literal("a straight continuation must take precedence over a corner"));
                            if (stub) helper.assertTrue(!state(helper, neighborPos).getValue(PlinthBlock.BASEBOARD), Component.literal("a suppressed corner must clear the plinth's baseboard stub"));
                            helper.setBlock(continuationPos, Blocks.AIR);
                            helper.assertTrue(state(helper, pos).getValue(BaseboardBlock.SHAPE) == expected, Component.literal("removing the continuation must restore the corner"));
                            if (stub) helper.assertTrue(state(helper, neighborPos).getValue(PlinthBlock.BASEBOARD), Component.literal("restoring the corner must restore the plinth's baseboard stub"));
                            if (apron) {
                                helper.setBlock(sourcePos, Blocks.AIR);
                                helper.assertTrue(state(helper, pos).getValue(BaseboardBlock.SHAPE) == StairsShape.STRAIGHT, Component.literal("removing an apron's baseboard source must clear the corner"));
                            }
                            helper.setBlock(neighborPos, Blocks.AIR);
                            helper.assertTrue(state(helper, pos).getValue(BaseboardBlock.SHAPE) == StairsShape.STRAIGHT, Component.literal("removing adjacent trim must clear the corner"));
                            helper.setBlock(pos, Blocks.AIR);
                        }
                    }
                }
            }
        }
        helper.succeed();
    }

    public static void unrelatedTrimDoesNotCreateCorners(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Direction offset : new Direction[] {facing, facing.getOpposite()}) {
                Direction other = facing.getClockWise();
                BlockPos neighborPos = pos.relative(offset);
                for (BlockState neighbor : new BlockState[] {
                        ModBlocks.PLINTH_BLOCK.get().defaultBlockState().setValue(CasingBlock.FACING, other)
                                .setValue(CasingBlock.EDGE, CasingBlock.Edge.TOP),
                        ModBlocks.PLINTH_BLOCK.get().defaultBlockState().setValue(CasingBlock.FACING, other)
                                .setValue(CasingBlock.EDGE, CasingBlock.Edge.BOTTOM),
                        ModBlocks.PLINTH_BLOCK.get().defaultBlockState().setValue(CasingBlock.FACING, Direction.UP),
                        ModBlocks.WINDOW_APRON.get().defaultBlockState().setValue(CasingBlock.FACING, other),
                        ModBlocks.CROWN_MOULDING.get().defaultBlockState().setValue(CrownMouldingBlock.FACING, other)}) {
                    helper.setBlock(pos, ModBlocks.BASEBOARD.get().defaultBlockState().setValue(BaseboardBlock.FACING, facing));
                    helper.setBlock(neighborPos, neighbor);
                    helper.assertTrue(state(helper, pos).getValue(BaseboardBlock.SHAPE) == StairsShape.STRAIGHT, Component.literal("horizontal plinths, bare aprons, and crown moulding must not create baseboard corners"));
                    helper.setBlock(pos, ModBlocks.CROWN_MOULDING.get().defaultBlockState().setValue(CrownMouldingBlock.FACING, facing));
                    if (!(neighbor.getBlock() instanceof CrownMouldingBlock)) {
                        helper.assertTrue(state(helper, pos).getValue(CrownMouldingBlock.SHAPE) == StairsShape.STRAIGHT, Component.literal("crown moulding must keep its existing connection rules"));
                    }
                    helper.setBlock(neighborPos, Blocks.AIR);
                    helper.setBlock(pos, Blocks.AIR);
                }
            }
        }
        helper.succeed();
    }

    private static BlockState state(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getBlockState(helper.absolutePos(pos));
    }
}
