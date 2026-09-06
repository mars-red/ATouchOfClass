package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.OakRailingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class OakRailingGameTests {
    @GameTest(template = "chandelier_test")
    public static void insideCornersJoinAndResetInEveryOrientation(GameTestHelper helper) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (boolean left : new boolean[] {true,false}) {
                for (boolean reverse : new boolean[] {false,true}) {
                    BlockPos pos=new BlockPos(2,2,2), front=pos.relative(facing);
                    Direction other=left?facing.getClockWise():facing.getCounterClockWise();
                    helper.setBlock(pos.relative(other.getOpposite()),Blocks.STONE);
                    if(reverse) { placeRail(helper,pos,facing);placeRail(helper,front,other); }
                    else { placeRail(helper,front,other);placeRail(helper,pos,facing); }
                    assertSlope(helper,pos,left?OakRailingBlock.Slope.INNER_LEFT:OakRailingBlock.Slope.INNER_RIGHT);
                    helper.setBlock(front,Blocks.AIR);
                    assertSlope(helper,pos,OakRailingBlock.Slope.FLAT);
                    helper.setBlock(pos,Blocks.AIR);
                    for(BlockPos wall:new BlockPos[]{pos.relative(facing.getOpposite()),pos.relative(other.getOpposite()),front.relative(other.getOpposite())})
                        helper.setBlock(wall,Blocks.AIR);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void outsideCornersPlaceOnRailEndsAndRequireAnchors(GameTestHelper helper) {
        var player=helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(ModItems.OAK_RAILING.get()));
        for(Direction facing:Direction.Plane.HORIZONTAL) {
            for(boolean left:new boolean[]{true,false}) {
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(ModItems.OAK_RAILING.get()));
                BlockPos pos=new BlockPos(2,2,2);
                Direction side=left?facing.getCounterClockWise():facing.getClockWise();
                BlockPos anchor=pos.relative(side), other=pos.relative(facing.getOpposite());
                placeRail(helper,anchor,facing);
                BlockPos absolute=helper.absolutePos(anchor);
                var hit=new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(absolute).add(
                                side.getOpposite().getStepX()*0.5,0.375,side.getOpposite().getStepZ()*0.5),
                        side.getOpposite(),absolute,false);
                helper.useBlock(anchor,player,hit);
                assertSlope(helper,pos,left?OakRailingBlock.Slope.OUTER_LEFT:OakRailingBlock.Slope.OUTER_RIGHT);
                BlockState elbow=helper.getLevel().getBlockState(helper.absolutePos(pos));
                helper.assertTrue(!elbow.canSurvive(helper.getLevel(),helper.absolutePos(pos.relative(side.getOpposite()))),
                        "Outside elbows must not support an unanchored chain");
                placeRail(helper,other,side.getOpposite());
                helper.setBlock(anchor,Blocks.AIR);
                assertSlope(helper,pos,left?OakRailingBlock.Slope.OUTER_LEFT:OakRailingBlock.Slope.OUTER_RIGHT);
                helper.setBlock(other,Blocks.AIR);
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir(),
                        "Outside elbow must drop after its last wall-mounted anchor is removed");
                helper.setBlock(anchor.relative(facing.getOpposite()),Blocks.AIR);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void horizontalRunsTransitionToEitherSideOfStairs(GameTestHelper helper) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (boolean diagonalRight : new boolean[] {false, true}) {
                for (int dy : new int[] {-1, 1}) {
                    for (int[] order : new int[][] {{0,1,2,3,4}, {4,3,2,1,0}, {1,3,0,4,2}}) {
                        BlockPos center = new BlockPos(2,2,2);
                        Direction towardStairs = diagonalRight ? facing.getClockWise() : facing.getCounterClockWise();
                        BlockPos[] run = {center.relative(towardStairs.getOpposite()), center,
                                center.relative(towardStairs), center.relative(towardStairs).offset(0,dy,0),
                                center.relative(towardStairs,2).offset(0,dy,0)};
                        for (int i : order) placeRail(helper, run[i], facing);
                        assertSlope(helper, run[0], OakRailingBlock.Slope.FLAT);
                        assertSlope(helper, center, diagonalRight ? OakRailingBlock.Slope.TRANSITION_RIGHT : OakRailingBlock.Slope.TRANSITION_LEFT);
                        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(run[2])).getValue(OakRailingBlock.SLOPE)
                                != OakRailingBlock.Slope.FLAT, "Stair neighbor must remain diagonal");
                        helper.setBlock(run[3].relative(facing.getOpposite()), Blocks.AIR);
                        assertSlope(helper, center, OakRailingBlock.Slope.FLAT);
                        assertSlope(helper, run[0], OakRailingBlock.Slope.FLAT);
                        for (BlockPos pos : run) {
                            helper.setBlock(pos, Blocks.AIR);
                            helper.setBlock(pos.relative(facing.getOpposite()), Blocks.AIR);
                        }
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void stairStepRunsAlternateInEveryDirectionAndPlacementOrder(GameTestHelper helper) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (int dy : new int[] {-1, 1}) {
                for (boolean verticalFirst : new boolean[] {false, true}) {
                    for (int[] order : new int[][] {{0,1,2,3}, {3,2,1,0}, {2,0,3,1}}) {
                        Direction right = facing.getClockWise();
                        BlockPos base = new BlockPos(2, 2, 2);
                        BlockPos[] run = verticalFirst
                                ? new BlockPos[] {base, base.offset(0,dy,0), base.relative(right).offset(0,dy,0), base.relative(right).offset(0,2*dy,0)}
                                : new BlockPos[] {base, base.relative(right), base.relative(right).offset(0,dy,0), base.relative(right,2).offset(0,dy,0)};
                        for (int i : order) placeRail(helper, run[i], facing);
                        OakRailingBlock.Slope first = dy > 0
                                ? (verticalFirst ? OakRailingBlock.Slope.UPPER_LEFT : OakRailingBlock.Slope.LOWER_RIGHT)
                                : (verticalFirst ? OakRailingBlock.Slope.LOWER_LEFT : OakRailingBlock.Slope.UPPER_RIGHT);
                        OakRailingBlock.Slope second = dy > 0
                                ? (verticalFirst ? OakRailingBlock.Slope.LOWER_RIGHT : OakRailingBlock.Slope.UPPER_LEFT)
                                : (verticalFirst ? OakRailingBlock.Slope.UPPER_RIGHT : OakRailingBlock.Slope.LOWER_LEFT);
                        for (int i = 0; i < run.length; i++) assertSlope(helper, run[i], i % 2 == 0 ? first : second);
                        // Losing the supporting wall must also update the rest of the run.
                        helper.setBlock(run[1].relative(facing.getOpposite()), Blocks.AIR);
                        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(run[1])).isAir(), "Unsupported railing must break");
                        for (int i : new int[] {0,2,3}) assertSlope(helper, run[i], OakRailingBlock.Slope.FLAT);
                        for (BlockPos pos : run) {
                            helper.setBlock(pos, Blocks.AIR);
                            helper.setBlock(pos.relative(facing.getOpposite()), Blocks.AIR);
                        }
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void straightAndDiagonalOnlyNeighborsStayFlat(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2,2,2);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Direction right = facing.getClockWise();
            for (BlockPos other : new BlockPos[] {pos.relative(right), pos.above(), pos.relative(right).above()}) {
                placeRail(helper, pos, facing);
                placeRail(helper, other, facing);
                assertSlope(helper, pos, OakRailingBlock.Slope.FLAT);
                assertSlope(helper, other, OakRailingBlock.Slope.FLAT);
                for (BlockPos p : new BlockPos[] {pos, other}) {
                    helper.setBlock(p, Blocks.AIR);
                    helper.setBlock(p.relative(facing.getOpposite()), Blocks.AIR);
                }
            }
        }
        helper.succeed();
    }
    private static void placeRail(GameTestHelper helper, BlockPos pos, Direction facing) {
        helper.setBlock(pos.relative(facing.getOpposite()), Blocks.STONE);
        helper.setBlock(pos, ModBlocks.OAK_RAILING.get().defaultBlockState().setValue(OakRailingBlock.FACING, facing));
    }

    private static void assertSlope(GameTestHelper helper, BlockPos pos, OakRailingBlock.Slope slope) {
        BlockState state = helper.getLevel().getBlockState(helper.absolutePos(pos));
        helper.assertTrue(state.is(ModBlocks.OAK_RAILING.get()) && state.getValue(OakRailingBlock.SLOPE) == slope,
                "Expected " + slope + " railing at " + pos + ", got " + state);
    }

    @GameTest(template = "chandelier_test")
    public static void requiresWallInEveryOrientation(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            BlockState rail = ModBlocks.OAK_RAILING.get().defaultBlockState()
                    .setValue(OakRailingBlock.FACING, facing);
            BlockPos support = pos.relative(facing.getOpposite());
            helper.setBlock(support, Blocks.STONE);
            helper.setBlock(pos, rail);
            helper.assertTrue(rail.canSurvive(helper.getLevel(), helper.absolutePos(pos)),
                    "Railing must attach to the wall for " + facing);
            helper.setBlock(support, Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir(),
                    "Railing must break when its supporting wall is removed for " + facing);
        }
        helper.succeed();
    }
}
