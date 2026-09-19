package com.digitaldownpour.atouchofclass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.DataMapHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class CopperFurnitureGameTests {
    private static final Block[] VANILLA_STAGES = {
            Blocks.CUT_COPPER,
            Blocks.EXPOSED_CUT_COPPER,
            Blocks.WEATHERED_CUT_COPPER,
            Blocks.OXIDIZED_CUT_COPPER
    };

    @GameTest(template = "chandelier_test")
    public static void countertopsOxidizeWaxAndUseVanillaCopperProperties(GameTestHelper helper) {
        verifyFamily(helper,
                new Block[]{
                        ModBlocks.CUT_COPPER_COUNTERTOP.get(),
                        ModBlocks.EXPOSED_CUT_COPPER_COUNTERTOP.get(),
                        ModBlocks.WEATHERED_CUT_COPPER_COUNTERTOP.get(),
                        ModBlocks.OXIDIZED_CUT_COPPER_COUNTERTOP.get()
                },
                new Block[]{
                        ModBlocks.WAXED_CUT_COPPER_COUNTERTOP.get(),
                        ModBlocks.WAXED_EXPOSED_CUT_COPPER_COUNTERTOP.get(),
                        ModBlocks.WAXED_WEATHERED_CUT_COPPER_COUNTERTOP.get(),
                        ModBlocks.WAXED_OXIDIZED_CUT_COPPER_COUNTERTOP.get()
                }, true);
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void sinksPreserveFacingThroughCopperTransitions(GameTestHelper helper) {
        verifyFamily(helper,
                new Block[]{
                        ModBlocks.CUT_COPPER_SINK.get(),
                        ModBlocks.EXPOSED_CUT_COPPER_SINK.get(),
                        ModBlocks.WEATHERED_CUT_COPPER_SINK.get(),
                        ModBlocks.OXIDIZED_CUT_COPPER_SINK.get()
                },
                new Block[]{
                        ModBlocks.WAXED_CUT_COPPER_SINK.get(),
                        ModBlocks.WAXED_EXPOSED_CUT_COPPER_SINK.get(),
                        ModBlocks.WAXED_WEATHERED_CUT_COPPER_SINK.get(),
                        ModBlocks.WAXED_OXIDIZED_CUT_COPPER_SINK.get()
                }, true);
        helper.succeed();
    }

    private static void verifyFamily(GameTestHelper helper, Block[] stages, Block[] waxedStages, boolean oriented) {
        BlockPos mapColorPos = helper.absolutePos(new BlockPos(1, 2, 1));

        for (int i = 0; i < stages.length; i++) {
            Block stage = stages[i];
            BlockState state = stage.defaultBlockState();
            if (oriented) {
                state = state.setValue(HorizontalDirectionalBlock.FACING, Direction.EAST);
            }

            helper.assertTrue(stage instanceof WeatheringCopper,
                    "Every unwaxed copper furniture stage must implement WeatheringCopper");
            helper.assertTrue(state.getSoundType() == VANILLA_STAGES[i].defaultBlockState().getSoundType(),
                    "Copper furniture must use the matching vanilla copper sound");
            helper.assertTrue(state.getMapColor(helper.getLevel(), mapColorPos)
                            == VANILLA_STAGES[i].defaultBlockState().getMapColor(helper.getLevel(), mapColorPos),
                    "Copper furniture must use the matching vanilla copper map color");

            BlockState waxed = HoneycombItem.getWaxed(state).orElseThrow();
            helper.assertTrue(waxed.is(waxedStages[i]), "Honeycomb must select the matching waxed finish");
            helper.assertTrue(DataMapHooks.getBlockUnwaxed(waxed.getBlock()) == stage,
                    "Axe unwaxing must restore the matching unwaxed finish");
            helper.assertFalse(waxed.isRandomlyTicking(), "Waxed copper furniture must not oxidize");
            if (oriented) {
                helper.assertTrue(waxed.getValue(HorizontalDirectionalBlock.FACING) == Direction.EAST,
                        "Waxing a sink must preserve its facing");
            }

            if (i < stages.length - 1) {
                BlockState next = ((WeatheringCopper) stage).getNext(state).orElseThrow();
                helper.assertTrue(next.is(stages[i + 1]), "Oxidation must advance exactly one finish");
                if (oriented) {
                    helper.assertTrue(next.getValue(HorizontalDirectionalBlock.FACING) == Direction.EAST,
                            "Oxidizing a sink must preserve its facing");
                }
            } else {
                helper.assertFalse(state.isRandomlyTicking(), "Fully oxidized furniture must stop ticking");
            }

            if (i > 0) {
                BlockState previous = WeatheringCopper.getPrevious(state).orElseThrow();
                helper.assertTrue(previous.is(stages[i - 1]), "Axe scraping must reverse exactly one finish");
                if (oriented) {
                    helper.assertTrue(previous.getValue(HorizontalDirectionalBlock.FACING) == Direction.EAST,
                            "Scraping a sink must preserve its facing");
                }
            }
        }
    }
}
