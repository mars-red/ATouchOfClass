package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.OakBalustradeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;

public class OakBalustradeGameTests {

    public static void bottomLandingsBlendAndReset(GameTestHelper helper) {
        var blocks=new ArrayList<OakBalustradeBlock>();
        blocks.add(ModBlocks.OAK_BALUSTRADE.get());
        ModBlocks.WOOD_RAILINGS.values().forEach(family->blocks.add(family.balustrade().get()));
        for(var block:blocks) for(Direction facing:Direction.Plane.HORIZONTAL)
            for(boolean right:new boolean[]{true,false}) for(boolean reverse:new boolean[]{true,false}) {
                BlockPos landing=new BlockPos(2,2,2),bridge=landing.above();
                Direction uphill=right?facing.getClockWise():facing.getCounterClockWise();
                BlockPos stairRail=bridge.relative(uphill),flat=landing.relative(uphill.getOpposite());
                var state=block.defaultBlockState().setValue(OakBalustradeBlock.FACING,facing);
                helper.setBlock(landing.below(),Blocks.STONE);
                helper.setBlock(flat.below(),Blocks.STONE);
                helper.setBlock(stairRail.below(),Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING,uphill));
                helper.setBlock(stairRail,state);
                helper.setBlock(flat,state);
                if(reverse) {helper.setBlock(bridge,state);helper.setBlock(landing,state);}
                else {helper.setBlock(landing,state);helper.setBlock(bridge,state);}
                var expected=right?com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.BOTTOM_LEFT
                        :com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.BOTTOM_RIGHT;
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(landing)).getValue(OakBalustradeBlock.SLOPE)==expected, Component.literal("Landing must blend into the diagonal for every wood, direction, and placement order"));
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(flat)).getValue(OakBalustradeBlock.SLOPE)
                        ==com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.FLAT, Component.literal("Transition must not spread along landing"));
                helper.setBlock(bridge,Blocks.AIR);
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(landing)).getValue(OakBalustradeBlock.SLOPE)
                        ==com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.FLAT, Component.literal("Removing diagonal must restore flat landing"));
                for(BlockPos p:new BlockPos[]{stairRail,landing,flat}) helper.setBlock(p,Blocks.AIR);
                for(BlockPos p:new BlockPos[]{stairRail,landing,flat}) helper.setBlock(p.below(),Blocks.AIR);
            }
        helper.succeed();
    }

    public static void topTransitionsFollowAnyLandingNeighbor(GameTestHelper helper) {
        for(Direction facing:Direction.Plane.HORIZONTAL) for(boolean right:new boolean[]{true,false}) {
            BlockPos pos=new BlockPos(2,2,2);
            Direction uphill=right?facing.getClockWise():facing.getCounterClockWise();
            helper.setBlock(pos.below(),Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,uphill));
            helper.setBlock(pos,ModBlocks.OAK_BALUSTRADE.get().defaultBlockState().setValue(OakBalustradeBlock.FACING,facing));
            for(var neighbor:new Block[]{Blocks.STONE,Blocks.GLASS,ModBlocks.OAK_BALUSTRADE.get()}) {
                helper.setBlock(pos.relative(uphill).below(),Blocks.STONE);
                helper.setBlock(pos.relative(uphill),neighbor);
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN), Component.literal("Any landing block must enable top transition"));
                helper.setBlock(pos.above(),ModBlocks.OAK_BALUSTRADE.get().defaultBlockState().setValue(OakBalustradeBlock.FACING,facing));
                helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN), Component.literal("Continuing diagonal must not flatten"));
                helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(pos.above())).getValue(OakBalustradeBlock.TOP_JOIN), Component.literal("Lower half must never transition"));
                helper.setBlock(pos.above(),Blocks.AIR);
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN), Component.literal("Removing continuation must restore transition"));
                helper.setBlock(pos.relative(uphill),Blocks.AIR);
                helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN), Component.literal("Removing landing must reset transition"));
                helper.setBlock(pos.relative(uphill).below(),Blocks.AIR);
            }
            helper.setBlock(pos,Blocks.AIR);helper.setBlock(pos.below(),Blocks.AIR);
        }
        helper.succeed();
    }

    public static void diagonalRunsPlaceOnStairsAndThroughRailFaces(GameTestHelper helper) {
        var player=helper.makeMockPlayer(GameType.CREATIVE);
        for(Direction facing:Direction.Plane.HORIZONTAL) for(boolean ascendingRight:new boolean[]{true,false}) {
            Direction ascent=ascendingRight?facing.getClockWise():facing.getCounterClockWise();
            var upper=ascendingRight?com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.UPPER_LEFT:com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.UPPER_RIGHT;
            var lower=ascendingRight?com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.LOWER_RIGHT:com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.LOWER_LEFT;
            BlockPos first=new BlockPos(2,2,2),bridge=first.above(),second=bridge.relative(ascent);
            var stair=Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,ascent);
            helper.setBlock(first.below(),stair);
            helper.setBlock(second.below(),stair);
            player.setYRot(facing.getOpposite().toYRot());
            useRail(helper,player,first.below(),Direction.UP);
            assertDiagonal(helper,first,upper,facing);
            useRail(helper,player,first,Direction.UP);
            assertDiagonal(helper,bridge,lower,facing);
            useRail(helper,player,bridge,ascent);
            assertDiagonal(helper,second,upper,facing);
            // The joining piece can also be inserted by clicking the upper rail's downhill end.
            helper.setBlock(bridge,Blocks.AIR);
            useRail(helper,player,second,ascent.getOpposite());
            assertDiagonal(helper,bridge,lower,facing);
            // Broad side faces must not allow arbitrary sideways extensions.
            useRail(helper,player,bridge,facing);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(bridge.relative(facing))).isAir(), Component.literal("Broad side is not an end face"));
            helper.setBlock(first.below(),Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(first)).isAir(), Component.literal("Post-supported half must drop with stair"));
            assertDiagonal(helper,bridge,lower,facing);
            helper.setBlock(second.below(),Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(bridge)).isAir(), Component.literal("Bridge must drop after last stair anchor is removed"));
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(second)).isAir(), Component.literal("Second support must drop"));
        }
        helper.succeed();
    }

    private static void useRail(GameTestHelper helper,Player player,BlockPos source,Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.OAK_BALUSTRADE.get()));
        helper.useBlock(source,player,new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(source)).add(
                face.getStepX()*0.5,face.getStepY()*0.5,face.getStepZ()*0.5),face,helper.absolutePos(source),false));
    }

    private static void assertDiagonal(GameTestHelper helper,BlockPos pos,com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope slope,Direction facing) {
        var state=helper.getLevel().getBlockState(helper.absolutePos(pos));
        helper.assertTrue(state.is(ModBlocks.OAK_BALUSTRADE.get()), Component.literal("Missing diagonal at "+pos+" facing "+facing));
        helper.assertTrue(state.getValue(OakBalustradeBlock.SLOPE)==slope, Component.literal("Expected "+slope+" at "+pos+", got "+state));
        helper.assertTrue(state.getValue(OakBalustradeBlock.FACING)==facing, Component.literal("Placement must preserve side of stair run"));
    }

    public static void cornersAlignStackedPostsAndReset(GameTestHelper helper) {
        for(Direction facing:Direction.Plane.HORIZONTAL) for(boolean outer:new boolean[]{false,true})
            for(boolean left:new boolean[]{false,true}) for(boolean reverse:new boolean[]{false,true}) {
                BlockPos pos=new BlockPos(2,3,2),neighbor=pos.relative(outer?facing.getOpposite():facing);
                helper.setBlock(pos.below(3),Blocks.STONE);
                helper.setBlock(pos.below(2),ModBlocks.OAK_BALUSTER.get());
                helper.setBlock(pos.below(),ModBlocks.OAK_BALUSTER.get());
                helper.setBlock(neighbor.below(),Blocks.STONE);
                var rail=ModBlocks.OAK_BALUSTRADE.get().defaultBlockState().setValue(OakBalustradeBlock.FACING,facing);
                var other=rail.setValue(OakBalustradeBlock.FACING,left?facing.getClockWise():facing.getCounterClockWise());
                if(reverse){helper.setBlock(pos,rail);helper.setBlock(neighbor,other);}
                else{helper.setBlock(neighbor,other);helper.setBlock(pos,rail);}
                StairsShape expected=outer?(left?StairsShape.OUTER_LEFT:StairsShape.OUTER_RIGHT):(left?StairsShape.INNER_LEFT:StairsShape.INNER_RIGHT);
                for(int depth=0;depth<3;depth++) {
                    var state=helper.getLevel().getBlockState(helper.absolutePos(pos.below(depth)));
                    helper.assertTrue(state.getValue(OakBalustradeBlock.SHAPE)==expected, Component.literal("Corner/post shape mismatch: "+facing+" "+expected+" depth "+depth));
                    helper.assertTrue(state.getValue(OakBalustradeBlock.FACING)==facing, Component.literal("Post facing mismatch"));
                }
                helper.setBlock(neighbor,Blocks.AIR);
                for(int depth=0;depth<3;depth++) helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos.below(depth))).getValue(OakBalustradeBlock.SHAPE)==StairsShape.STRAIGHT, Component.literal("Corner must reset after removal"));
                helper.setBlock(pos.below(3),Blocks.AIR);
                for(int depth=0;depth<3;depth++) helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos.below(depth))).isAir(), Component.literal("Unsupported post/rail must drop"));
                helper.setBlock(neighbor.below(),Blocks.AIR);
            }
        helper.succeed();
    }

    public static void onlyTopPlacementAndPostsSupportRail(GameTestHelper helper) {
        var player=helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos base=new BlockPos(2,1,2);
        helper.setBlock(base,Blocks.STONE);
        for(var item:new Item[]{ModItems.OAK_BALUSTER.get(),ModItems.OAK_BALUSTRADE.get()}) {
            for(Direction side:Direction.Plane.HORIZONTAL) {
                BlockPos target=base.relative(side);
                helper.setBlock(target.below(),Blocks.STONE);
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
                helper.useBlock(base,player,new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(base)),side,helper.absolutePos(base),false));
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(target)).isAir(), Component.literal("Side placement must be rejected even above solid ground"));
                helper.setBlock(target.below(),Blocks.AIR);
            }
        }
        for(var item:new Item[]{ModItems.OAK_BALUSTER.get(),ModItems.OAK_BALUSTRADE.get()}) {
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
            helper.useBlock(base,player,new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(base)).add(0,0.5,0),Direction.UP,helper.absolutePos(base),false));
            base=base.above();
            helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(base)).isAir(), Component.literal("Top placement must succeed"));
        }
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(base)).is(ModBlocks.OAK_BALUSTRADE.get()), Component.literal("Railing must place on baluster"));
        helper.succeed();
    }
}
