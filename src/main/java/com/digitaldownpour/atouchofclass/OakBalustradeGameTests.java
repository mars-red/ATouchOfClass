package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.OakBalustradeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class OakBalustradeGameTests {
    @GameTest(template="chandelier_test")
    public static void topTransitionsFollowAnyLandingNeighbor(GameTestHelper helper) {
        for(Direction facing:Direction.Plane.HORIZONTAL) for(boolean right:new boolean[]{true,false}) {
            BlockPos pos=new BlockPos(2,2,2);
            Direction uphill=right?facing.getClockWise():facing.getCounterClockWise();
            helper.setBlock(pos.below(),Blocks.OAK_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING,uphill));
            helper.setBlock(pos,ModBlocks.OAK_BALUSTRADE.get().defaultBlockState().setValue(OakBalustradeBlock.FACING,facing));
            for(var neighbor:new net.minecraft.world.level.block.Block[]{Blocks.STONE,Blocks.GLASS,ModBlocks.OAK_BALUSTRADE.get()}) {
                helper.setBlock(pos.relative(uphill).below(),Blocks.STONE);
                helper.setBlock(pos.relative(uphill),neighbor);
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN),"Any landing block must enable top transition");
                helper.setBlock(pos.above(),ModBlocks.OAK_BALUSTRADE.get().defaultBlockState().setValue(OakBalustradeBlock.FACING,facing));
                helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN),"Continuing diagonal must not flatten");
                helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(pos.above())).getValue(OakBalustradeBlock.TOP_JOIN),"Lower half must never transition");
                helper.setBlock(pos.above(),Blocks.AIR);
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN),"Removing continuation must restore transition");
                helper.setBlock(pos.relative(uphill),Blocks.AIR);
                helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN),"Removing landing must reset transition");
                helper.setBlock(pos.relative(uphill).below(),Blocks.AIR);
            }
            helper.setBlock(pos,Blocks.AIR);helper.setBlock(pos.below(),Blocks.AIR);
        }
        helper.succeed();
    }
    @GameTest(template="chandelier_test")
    public static void diagonalRunsPlaceOnStairsAndThroughRailFaces(GameTestHelper helper) {
        var player=helper.makeMockPlayer(GameType.CREATIVE);
        for(Direction facing:Direction.Plane.HORIZONTAL) for(boolean ascendingRight:new boolean[]{true,false}) {
            Direction ascent=ascendingRight?facing.getClockWise():facing.getCounterClockWise();
            var upper=ascendingRight?com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.UPPER_LEFT:com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.UPPER_RIGHT;
            var lower=ascendingRight?com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.LOWER_RIGHT:com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope.LOWER_LEFT;
            BlockPos first=new BlockPos(2,2,2),bridge=first.above(),second=bridge.relative(ascent);
            var stair=Blocks.OAK_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING,ascent);
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
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(bridge.relative(facing))).isAir(),"Broad side is not an end face");
            helper.setBlock(first.below(),Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(first)).isAir(),"Post-supported half must drop with stair");
            assertDiagonal(helper,bridge,lower,facing);
            helper.setBlock(second.below(),Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(bridge)).isAir(),"Bridge must drop after last stair anchor is removed");
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(second)).isAir(),"Second support must drop");
        }
        helper.succeed();
    }

    private static void useRail(GameTestHelper helper,net.minecraft.world.entity.player.Player player,BlockPos source,Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.OAK_BALUSTRADE.get()));
        helper.useBlock(source,player,new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(source)).add(
                face.getStepX()*0.5,face.getStepY()*0.5,face.getStepZ()*0.5),face,helper.absolutePos(source),false));
    }

    private static void assertDiagonal(GameTestHelper helper,BlockPos pos,com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope slope,Direction facing) {
        var state=helper.getLevel().getBlockState(helper.absolutePos(pos));
        helper.assertTrue(state.is(ModBlocks.OAK_BALUSTRADE.get()),"Missing diagonal at "+pos+" facing "+facing);
        helper.assertTrue(state.getValue(OakBalustradeBlock.SLOPE)==slope,"Expected "+slope+" at "+pos+", got "+state);
        helper.assertTrue(state.getValue(OakBalustradeBlock.FACING)==facing,"Placement must preserve side of stair run");
    }

    @GameTest(template="chandelier_test")
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
                    helper.assertTrue(state.getValue(OakBalustradeBlock.SHAPE)==expected,"Corner/post shape mismatch: "+facing+" "+expected+" depth "+depth);
                    helper.assertTrue(state.getValue(OakBalustradeBlock.FACING)==facing,"Post facing mismatch");
                }
                helper.setBlock(neighbor,Blocks.AIR);
                for(int depth=0;depth<3;depth++) helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos.below(depth))).getValue(OakBalustradeBlock.SHAPE)==StairsShape.STRAIGHT,"Corner must reset after removal");
                helper.setBlock(pos.below(3),Blocks.AIR);
                for(int depth=0;depth<3;depth++) helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos.below(depth))).isAir(),"Unsupported post/rail must drop");
                helper.setBlock(neighbor.below(),Blocks.AIR);
            }
        helper.succeed();
    }

    @GameTest(template="chandelier_test")
    public static void onlyTopPlacementAndPostsSupportRail(GameTestHelper helper) {
        var player=helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos base=new BlockPos(2,1,2);
        helper.setBlock(base,Blocks.STONE);
        for(var item:new net.minecraft.world.item.Item[]{ModItems.OAK_BALUSTER.get(),ModItems.OAK_BALUSTRADE.get()}) {
            for(Direction side:Direction.Plane.HORIZONTAL) {
                BlockPos target=base.relative(side);
                helper.setBlock(target.below(),Blocks.STONE);
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
                helper.useBlock(base,player,new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(base)),side,helper.absolutePos(base),false));
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(target)).isAir(),"Side placement must be rejected even above solid ground");
                helper.setBlock(target.below(),Blocks.AIR);
            }
        }
        for(var item:new net.minecraft.world.item.Item[]{ModItems.OAK_BALUSTER.get(),ModItems.OAK_BALUSTRADE.get()}) {
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
            helper.useBlock(base,player,new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(base)).add(0,0.5,0),Direction.UP,helper.absolutePos(base),false));
            base=base.above();
            helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(base)).isAir(),"Top placement must succeed");
        }
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(base)).is(ModBlocks.OAK_BALUSTRADE.get()),"Railing must place on baluster");
        helper.succeed();
    }
}
