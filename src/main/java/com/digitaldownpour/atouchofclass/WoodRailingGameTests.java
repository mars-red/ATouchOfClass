package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.network.chat.Component;

public class WoodRailingGameTests {

    public static void everyWoodSupportsPostsStairsAndWallMounts(GameTestHelper helper) {
        helper.assertTrue(ModBlocks.WOOD_RAILINGS.size()==12, Component.literal("All twelve additional wood families must be registered alongside oak"));
        for(var entry:ModBlocks.WOOD_RAILINGS.entrySet()) {
            var family=entry.getValue();
            BlockPos pos=new BlockPos(2,2,2);
            helper.setBlock(pos.below(2),Blocks.STONE);
            helper.setBlock(pos.below(),family.baluster().get());
            helper.setBlock(pos,family.balustrade().get());
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).canSurvive(helper.getLevel(),helper.absolutePos(pos)), Component.literal(entry.getKey()+" post must support balustrade"));
            helper.setBlock(pos,Blocks.AIR);
            helper.setBlock(pos.below(),Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
            helper.setBlock(pos,family.balustrade().get());
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.SLOPE)==OakRailingBlock.Slope.UPPER_LEFT, Component.literal(entry.getKey()+" must select diagonal"));
            helper.setBlock(pos.above(),family.balustrade().get());
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos.above())).getValue(OakBalustradeBlock.SLOPE)==OakRailingBlock.Slope.LOWER_RIGHT, Component.literal(entry.getKey()+" must alternate diagonal halves"));
            helper.setBlock(pos.above(),Blocks.AIR);
            helper.setBlock(pos.east(),Blocks.STONE);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getValue(OakBalustradeBlock.TOP_JOIN), Component.literal(entry.getKey()+" must support landing transitions"));
            helper.setBlock(pos.east(),Blocks.AIR);
            helper.setBlock(pos,Blocks.AIR);
            helper.setBlock(pos.below(),Blocks.AIR);
            helper.setBlock(pos.below(2),Blocks.AIR);
            helper.setBlock(pos.south(),Blocks.STONE);
            helper.setBlock(pos,family.railing().get());
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).canSurvive(helper.getLevel(),helper.absolutePos(pos)), Component.literal(entry.getKey()+" must mount on walls"));
            helper.setBlock(pos.south(),Blocks.AIR);
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir(), Component.literal(entry.getKey()+" wall rail must drop without support"));
        }
        helper.succeed();
    }
}
