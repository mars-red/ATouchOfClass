package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.CopperChandelierChainBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.DataMapHooks;
import net.minecraft.network.chat.Component;

public class ChandelierGameTests {

    public static void ceilingAndChainSupport(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        BlockState chandelier = ModBlocks.COPPER_CHANDELIER.get().defaultBlockState();
        helper.setBlock(pos.above(), Blocks.STONE);
        helper.assertTrue(chandelier.canSurvive(helper.getLevel(), helper.absolutePos(pos)), Component.literal("Ceiling must support chandelier"));
        helper.setBlock(pos.above(), ModBlocks.COPPER_CHANDELIER_CHAIN.get());
        helper.assertTrue(chandelier.canSurvive(helper.getLevel(), helper.absolutePos(pos)), Component.literal("Vertical copper chandelier chain must support chandelier"));
        helper.setBlock(pos, chandelier);
        helper.setBlock(pos.above(), ModBlocks.WAXED_OXIDIZED_COPPER_CHANDELIER_CHAIN.get());
        helper.assertBlockPresent(ModBlocks.COPPER_CHANDELIER.get(), pos);
        helper.setBlock(pos.above(), Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.setBlock(pos.above(), ModBlocks.COPPER_CHANDELIER_CHAIN.get().defaultBlockState().setValue(CopperChandelierChainBlock.AXIS, Direction.Axis.X));
        helper.assertFalse(chandelier.canSurvive(helper.getLevel(), helper.absolutePos(pos)), Component.literal("Horizontal chain must not count as a ceiling attachment"));
        helper.succeed();
    }

    public static void oxidationWaxingAndLight(GameTestHelper helper) {
        Block[] stages = {ModBlocks.COPPER_CHANDELIER.get(), ModBlocks.EXPOSED_COPPER_CHANDELIER.get(),
                ModBlocks.WEATHERED_COPPER_CHANDELIER.get(), ModBlocks.OXIDIZED_COPPER_CHANDELIER.get()};
        for (int i = 0; i < stages.length; i++) {
            BlockState state = stages[i].defaultBlockState();
            BlockState waxed = HoneycombItem.getWaxed(state).orElseThrow();
            helper.assertTrue(state.getLightEmission() == 15 && waxed.getLightEmission() == 15, Component.literal("All chandelier finishes must emit level 15"));
            helper.assertTrue(DataMapHooks.getBlockUnwaxed(waxed.getBlock()) == stages[i], Component.literal("Wax removal must restore the same finish"));
            helper.assertFalse(waxed.isRandomlyTicking(), Component.literal("Waxed chandelier must not age"));
            if (i < 3) helper.assertTrue(WeatheringCopper.getNext(stages[i]).orElseThrow() == stages[i + 1], Component.literal("Oxidation must advance one stage"));
            if (i > 0) helper.assertTrue(WeatheringCopper.getPrevious(stages[i]).orElseThrow() == stages[i - 1], Component.literal("Scraping must reverse one stage"));
        }
        helper.assertFalse(stages[3].defaultBlockState().isRandomlyTicking(), Component.literal("Fully oxidized chandelier must stop aging"));
        helper.succeed();
    }

    public static void chainStateSurvivesTransitions(GameTestHelper helper) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            BlockState state = ModBlocks.COPPER_CHANDELIER_CHAIN.get().defaultBlockState()
                    .setValue(CopperChandelierChainBlock.AXIS, axis).setValue(CopperChandelierChainBlock.WATERLOGGED, true);
            for (int i = 0; i < 4; i++) {
                BlockState waxed = HoneycombItem.getWaxed(state).orElseThrow();
                helper.assertTrue(waxed.getValue(CopperChandelierChainBlock.AXIS) == axis, Component.literal("Waxing must preserve chain axis"));
                helper.assertTrue(waxed.getFluidState().getType() == Fluids.WATER, Component.literal("Waxing must preserve waterlogging"));
                helper.assertFalse(waxed.isRandomlyTicking(), Component.literal("Waxed chain must not age"));
                helper.assertTrue(DataMapHooks.getBlockUnwaxed(waxed.getBlock()) == state.getBlock(), Component.literal("Wax removal must restore chain finish"));
                if (i < 3) {
                    BlockState next = ((WeatheringCopper)state.getBlock()).getNext(state).orElseThrow();
                    helper.assertTrue(WeatheringCopper.getPrevious(next).orElseThrow().equals(state), Component.literal("Scraping must preserve axis and water"));
                    state = next;
                }
            }
        }
        helper.succeed();
    }
}
