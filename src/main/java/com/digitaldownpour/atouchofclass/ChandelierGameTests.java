package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.CopperChainBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.common.DataMapHooks;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class ChandelierGameTests {
    @GameTest(template = "chandelier_test")
    public static void ceilingAndChainSupport(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        BlockState chandelier = ModBlocks.COPPER_CHANDELIER.get().defaultBlockState();
        helper.setBlock(pos.above(), Blocks.STONE);
        helper.assertTrue(chandelier.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "Ceiling must support chandelier");
        helper.setBlock(pos.above(), ModBlocks.COPPER_CHAIN.get());
        helper.assertTrue(chandelier.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "Vertical copper chain must support chandelier");
        helper.setBlock(pos, chandelier);
        helper.setBlock(pos.above(), ModBlocks.WAXED_OXIDIZED_COPPER_CHAIN.get());
        helper.assertBlockPresent(ModBlocks.COPPER_CHANDELIER.get(), pos);
        helper.setBlock(pos.above(), Blocks.AIR);
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.setBlock(pos.above(), ModBlocks.COPPER_CHAIN.get().defaultBlockState().setValue(CopperChainBlock.AXIS, Direction.Axis.X));
        helper.assertFalse(chandelier.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "Horizontal chain must not count as a ceiling attachment");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void oxidationWaxingAndLight(GameTestHelper helper) {
        Block[] stages = {ModBlocks.COPPER_CHANDELIER.get(), ModBlocks.EXPOSED_COPPER_CHANDELIER.get(),
                ModBlocks.WEATHERED_COPPER_CHANDELIER.get(), ModBlocks.OXIDIZED_COPPER_CHANDELIER.get()};
        for (int i = 0; i < stages.length; i++) {
            BlockState state = stages[i].defaultBlockState();
            BlockState waxed = HoneycombItem.getWaxed(state).orElseThrow();
            helper.assertTrue(state.getLightEmission() == 15 && waxed.getLightEmission() == 15, "All chandelier finishes must emit level 15");
            helper.assertTrue(DataMapHooks.getBlockUnwaxed(waxed.getBlock()) == stages[i], "Wax removal must restore the same finish");
            helper.assertFalse(waxed.isRandomlyTicking(), "Waxed chandelier must not age");
            if (i < 3) helper.assertTrue(WeatheringCopper.getNext(stages[i]).orElseThrow() == stages[i + 1], "Oxidation must advance one stage");
            if (i > 0) helper.assertTrue(WeatheringCopper.getPrevious(stages[i]).orElseThrow() == stages[i - 1], "Scraping must reverse one stage");
        }
        helper.assertFalse(stages[3].defaultBlockState().isRandomlyTicking(), "Fully oxidized chandelier must stop aging");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void chainStateSurvivesTransitions(GameTestHelper helper) {
        for (Direction.Axis axis : Direction.Axis.values()) {
            BlockState state = ModBlocks.COPPER_CHAIN.get().defaultBlockState()
                    .setValue(CopperChainBlock.AXIS, axis).setValue(CopperChainBlock.WATERLOGGED, true);
            for (int i = 0; i < 4; i++) {
                BlockState waxed = HoneycombItem.getWaxed(state).orElseThrow();
                helper.assertTrue(waxed.getValue(CopperChainBlock.AXIS) == axis, "Waxing must preserve chain axis");
                helper.assertTrue(waxed.getFluidState().getType() == Fluids.WATER, "Waxing must preserve waterlogging");
                helper.assertFalse(waxed.isRandomlyTicking(), "Waxed chain must not age");
                helper.assertTrue(DataMapHooks.getBlockUnwaxed(waxed.getBlock()) == state.getBlock(), "Wax removal must restore chain finish");
                if (i < 3) {
                    BlockState next = ((WeatheringCopper)state.getBlock()).getNext(state).orElseThrow();
                    helper.assertTrue(WeatheringCopper.getPrevious(next).orElseThrow().equals(state), "Scraping must preserve axis and water");
                    state = next;
                }
            }
        }
        helper.succeed();
    }
}
