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
