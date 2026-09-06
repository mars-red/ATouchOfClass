package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class WoodRailingGameTests {
    @GameTest(template="chandelier_test")
    public static void everyWoodSupportsPostsStairsAndWallMounts(GameTestHelper helper) {
        helper.assertTrue(ModBlocks.WOOD_RAILINGS.size()==11,"All vanilla wood families must be registered");
        for(var entry:ModBlocks.WOOD_RAILINGS.entrySet()) {
            var family=entry.getValue();
            BlockPos pos=new BlockPos(2,2,2);
            helper.setBlock(pos.below(2),Blocks.STONE);
            helper.setBlock(pos.below(),family.baluster().get());
            helper.setBlock(pos,family.balustrade().get());
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).canSurvive(helper.getLevel(),helper.absolutePos(pos)),entry.getKey()+" post must support balustrade");
            helper.setBlock(pos,Blocks.AIR);
            helper.setBlock(pos.below(),Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
            helper.setBlock(pos,family.balustrade().get());
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.SLOPE)==OakRailingBlock.Slope.UPPER_LEFT,entry.getKey()+" must select diagonal");
            helper.setBlock(pos.above(),family.balustrade().get());
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos.above())).getValue(OakBalustradeBlock.SLOPE)==OakRailingBlock.Slope.LOWER_RIGHT,entry.getKey()+" must alternate diagonal halves");
            helper.setBlock(pos.above(),Blocks.AIR);
            helper.setBlock(pos.east(),Blocks.STONE);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN),entry.getKey()+" must support landing transitions");
            helper.setBlock(pos.east(),Blocks.AIR);
            helper.setBlock(pos,Blocks.AIR);
            helper.setBlock(pos.below(),Blocks.AIR);
            helper.setBlock(pos.below(2),Blocks.AIR);
            helper.setBlock(pos.south(),Blocks.STONE);
            helper.setBlock(pos,family.railing().get());
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).canSurvive(helper.getLevel(),helper.absolutePos(pos)),entry.getKey()+" must mount on walls");
            helper.setBlock(pos.south(),Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir(),entry.getKey()+" wall rail must drop without support");
        }
        helper.succeed();
    }
}
