package com.digitaldownpour.atouchofclass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;

public class SideTableGameTests {

    public static void sideTableHasFullHeightTopSupport(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(3, 1, 3);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.setBlock(relativePos.below(), Blocks.STONE);
        helper.setBlock(relativePos, ModBlocks.OAK_SIDE_TABLE.get().defaultBlockState());

        var state = helper.getLevel().getBlockState(absolutePos);
        var collision = state.getCollisionShape(helper.getLevel(), absolutePos);
        helper.assertTrue(Math.abs(collision.max(Direction.Axis.Y) - 1.0) < 0.0001, Component.literal("side-table collision must reach the full 16-unit block height"));
        helper.assertTrue(state.isFaceSturdy(helper.getLevel(), absolutePos, Direction.UP), Component.literal("the tabletop must expose a sturdy full-height upper face"));
        helper.assertTrue(Blocks.FLOWER_POT.defaultBlockState()
                        .canSurvive(helper.getLevel(), absolutePos.above()), Component.literal("the tabletop must support centered decorative blocks above it"));
        helper.succeed();
    }
}
