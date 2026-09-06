package com.digitaldownpour.atouchofclass;
import com.digitaldownpour.atouchofclass.blocks.OakNewelPostBlock;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class NewelPostGameTests {
    @GameTest(template="chandelier_test")
    public static void everyWoodNewelPlacesAndRemovesBothHalves(GameTestHelper helper) {
        var player=helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos base=new BlockPos(2,1,2),pos=base.above();
        for(var family:ModBlocks.WOOD_RAILINGS.values()) {
            for(double x:new double[]{0.25,0.75})for(double z:new double[]{0.25,0.75}) {
                helper.setBlock(base,Blocks.STONE);
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(family.newelPost().get()));
                helper.useBlock(base,player,new BlockHitResult(Vec3.atLowerCornerOf(helper.absolutePos(base)).add(x,1,z),Direction.UP,helper.absolutePos(base),false));
                Direction expected=z>=0.5?(x>=0.5?Direction.NORTH:Direction.EAST):(x>=0.5?Direction.WEST:Direction.SOUTH);
                for(int h=0;h<2;h++) {
                    var state=helper.getLevel().getBlockState(helper.absolutePos(pos.above(h)));
                    helper.assertTrue(state.is(family.newelPost().get()),"Both halves must retain the selected wood");
                    helper.assertTrue(state.getValue(OakNewelPostBlock.FACING)==expected,"Every wood must obey clicked quadrant");
                }
                helper.setBlock(x<0.5?pos:pos.above(),Blocks.AIR);
                helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir()&&helper.getLevel().getBlockState(helper.absolutePos(pos.above())).isAir(),"Either half must remove the complete newel");
            }
        }
        helper.succeed();
    }
    @GameTest(template="chandelier_test")
    public static void quadrantsAndTwoBlockLifecycle(GameTestHelper helper) {
        var player=helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos base=new BlockPos(2,1,2),pos=base.above();
        for(double x:new double[]{0.25,0.75})for(double z:new double[]{0.25,0.75}) {
            helper.setBlock(base,Blocks.STONE);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.OAK_NEWEL_POST.get()));
            helper.useBlock(base,player,new BlockHitResult(Vec3.atLowerCornerOf(helper.absolutePos(base)).add(x,1,z),Direction.UP,helper.absolutePos(base),false));
            Direction expected=z>=0.5?(x>=0.5?Direction.NORTH:Direction.EAST):(x>=0.5?Direction.WEST:Direction.SOUTH);
            for(int h=0;h<2;h++) {
                var state=helper.getLevel().getBlockState(helper.absolutePos(pos.above(h)));
                helper.assertTrue(state.is(ModBlocks.OAK_NEWEL_POST.get()),"Both newel halves must exist");
                helper.assertTrue(state.getValue(OakNewelPostBlock.FACING)==expected,"Click quadrant must control orientation");
                helper.assertTrue(state.getValue(OakNewelPostBlock.HALF)==(h==0?DoubleBlockHalf.LOWER:DoubleBlockHalf.UPPER),"Incorrect half");
            }
            if(x<0.5) helper.setBlock(pos.above(),Blocks.AIR);
            else helper.setBlock(base,Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir()&&helper.getLevel().getBlockState(helper.absolutePos(pos.above())).isAir(),"Breaking top or support must remove both halves");
        }
        helper.setBlock(base,Blocks.STONE);
        helper.setBlock(pos.above(),Blocks.STONE);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.OAK_NEWEL_POST.get()));
        helper.useBlock(base,player,new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(base)).add(0,0.5,0),Direction.UP,helper.absolutePos(base),false));
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir(),"Occupied upper cell must reject placement");
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos.above())).is(Blocks.STONE),"Placement must preserve obstruction");
        helper.setBlock(pos.above(),Blocks.AIR);
        helper.setBlock(base.east().below(),Blocks.STONE);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.OAK_NEWEL_POST.get()));
        helper.useBlock(base,player,new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(base)),Direction.EAST,helper.absolutePos(base),false));
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(base.east())).isAir(),"Side placement must be rejected");
        helper.succeed();
    }
}
