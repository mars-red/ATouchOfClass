package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.WindowApronBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class WindowApronGameTests {
    @GameTest(template = "chandelier_test")
    public static void baseboardCrossesApronsAndClearsOnRemoval(GameTestHelper helper) {
        BlockState apron = ModBlocks.WINDOW_APRON.get().defaultBlockState();
        for (int x = 1; x <= 3; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), apron.setValue(WindowApronBlock.CAP,
                    x == 1 ? WindowApronBlock.Cap.LEFT
                            : x == 3 ? WindowApronBlock.Cap.RIGHT : WindowApronBlock.Cap.NONE));
        }
        helper.setBlock(new BlockPos(0, 1, 2), ModBlocks.BASEBOARD.get());
        assertRun(helper, true);
        helper.setBlock(new BlockPos(4, 1, 2), ModBlocks.BASEBOARD.get());
        helper.setBlock(new BlockPos(0, 1, 2), Blocks.AIR);
        assertRun(helper, true);
        helper.setBlock(new BlockPos(4, 1, 2), Blocks.AIR);
        assertRun(helper, false);
        helper.succeed();
    }

    private static void assertRun(GameTestHelper helper, boolean expected) {
        for (int x = 1; x <= 3; x++) {
            BlockState state = helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(x, 1, 2)));
            helper.assertTrue(state.getValue(WindowApronBlock.BASEBOARD) == expected,
                    "Apron " + x + " must have baseboard=" + expected);
        }
    }
}
