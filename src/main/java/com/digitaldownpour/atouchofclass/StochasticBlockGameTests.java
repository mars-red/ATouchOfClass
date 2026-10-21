package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.StochasticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class StochasticBlockGameTests {
    public static void marblePlacementRerollsAndDropsPlainItems(GameTestHelper helper) {
        var level = helper.getLevel();
        var block = ModBlocks.BASIC_BLOCKS.get("marble_tile").get();
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        var support = helper.absolutePos(new BlockPos(2, 1, 2));
        var pos = support.above();
        level.setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
        var hit = new BlockHitResult(Vec3.atCenterOf(support).add(0, .5, 0), Direction.UP, support, false);
        int seen = 0;
        ItemStack item = new ItemStack(block.asItem());
        for (int i = 0; i < 128; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, item);
            item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit));
            var state = level.getBlockState(pos);
            helper.assertTrue(state.is(block), Component.literal("Collected marble item must place"));
            helper.assertTrue(state.getValue(StochasticBlock.TEXTURE_VARIANT) == 0,
                    Component.literal("Only the existing marble texture is enabled"));
            seen |= 1 << state.getValue(StochasticBlock.TEXTURE_ROTATION);
            var drops = Block.getDrops(state, level, pos, null);
            helper.assertTrue(drops.size() == 1 && ItemStack.isSameItemSameComponents(
                            drops.getFirst(), new ItemStack(block.asItem())),
                    Component.literal("Every marble appearance must drop a plain item"));
            item = drops.getFirst();
            level.destroyBlock(pos, false);
        }
        helper.assertTrue(seen == 15, Component.literal("Repeated placement at one position must reroll rotation"));
        helper.succeed();
    }
}
