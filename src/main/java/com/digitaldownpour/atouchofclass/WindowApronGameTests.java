package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.WindowApronBlock;
import com.digitaldownpour.atouchofclass.blocks.PlinthBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class WindowApronGameTests {

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

    public static void perpendicularBaseboardConnectsInFront(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Direction other : new Direction[] {
                    facing.getClockWise(), facing.getCounterClockWise()}) {
                for (Block block : new Block[] {
                        ModBlocks.WINDOW_APRON.get(), ModBlocks.PLINTH_BLOCK.get()}) {
                    var baseboardProperty = block instanceof PlinthBlock
                            ? PlinthBlock.BASEBOARD : WindowApronBlock.BASEBOARD;
                    helper.setBlock(pos, block.defaultBlockState()
                            .setValue(com.digitaldownpour.atouchofclass.blocks.CasingBlock.FACING, facing));
                    helper.setBlock(pos.relative(facing), ModBlocks.BASEBOARD.get().defaultBlockState()
                            .setValue(com.digitaldownpour.atouchofclass.blocks.BaseboardBlock.FACING, other));
                    BlockState connected = helper.getLevel().getBlockState(helper.absolutePos(pos));
                    helper.assertTrue(connected.getValue(baseboardProperty), Component.literal("Front baseboard must enable the combined variant"));
                    Property<?> property =
                            block.getStateDefinition().getProperty("baseboard_return");
                    helper.assertTrue(connected.getValues().get(property) == other, Component.literal("Return must follow the perpendicular baseboard"));
                    helper.setBlock(pos.relative(facing), Blocks.AIR);
                    BlockState cleared = helper.getLevel().getBlockState(helper.absolutePos(pos));
                    helper.assertTrue(!cleared.getValue(baseboardProperty), Component.literal("Removing front baseboard must clear the connection"));
                    helper.setBlock(pos, Blocks.AIR);
                }
            }
        }
        helper.succeed();
    }
    private static void assertRun(GameTestHelper helper, boolean expected) {
        for (int x = 1; x <= 3; x++) {
            BlockState state = helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(x, 1, 2)));
            helper.assertTrue(state.getValue(WindowApronBlock.BASEBOARD) == expected, Component.literal("Apron " + x + " must have baseboard=" + expected));
        }
    }
}
