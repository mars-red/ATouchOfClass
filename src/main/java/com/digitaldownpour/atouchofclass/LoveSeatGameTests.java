package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.LoveSeatBlock;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;

public class LoveSeatGameTests {

    public static void loveSeatPlacesBreaksAndSupportsIndependentSeats(GameTestHelper helper) {
        LoveSeatBlock block = ModBlocks.LEATHER_LOVE_SEAT.get();
        BlockPos relativeAnchor = new BlockPos(3, 1, 3);
        BlockPos absoluteAnchor = helper.absolutePos(relativeAnchor);
        Direction facing = Direction.NORTH;
        BlockPos absoluteRight = LoveSeatBlock.partPos(
                absoluteAnchor, facing, LoveSeatBlock.Side.RIGHT);

        helper.setBlock(relativeAnchor.below(), Blocks.STONE);
        helper.setBlock(relativeAnchor.below().east(), Blocks.STONE);
        var anchorState = block.defaultBlockState()
                .setValue(LoveSeatBlock.FACING, facing)
                .setValue(LoveSeatBlock.SIDE, LoveSeatBlock.Side.LEFT);
        helper.setBlock(relativeAnchor, anchorState);
        block.setPlacedBy(helper.getLevel(), absoluteAnchor, anchorState, null, ItemStack.EMPTY);

        helper.assertTrue(helper.getLevel().getBlockState(absoluteAnchor).is(block), Component.literal("the left love-seat section must be placed"));
        var rightState = helper.getLevel().getBlockState(absoluteRight);
        helper.assertTrue(rightState.is(block)
                        && rightState.getValue(LoveSeatBlock.SIDE) == LoveSeatBlock.Side.RIGHT, Component.literal("the right love-seat section must be placed with the correct side"));

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        SeatEntity seat = new SeatEntity(ModEntities.LOVE_SEAT_SEAT.get(), helper.getLevel());
        seat.setPos(absoluteAnchor.getX() + 0.5, absoluteAnchor.getY() + 0.1,
                absoluteAnchor.getZ() + 0.5);
        helper.getLevel().addFreshEntity(seat);
        helper.assertTrue(player.startRiding(seat, true, true), Component.literal("a player must be able to sit on a cushion"));
        helper.assertTrue(seat.hasPassenger(player), Component.literal("the cushion seat must hold its player passenger"));

        helper.getLevel().destroyBlock(absoluteRight, false);
        helper.assertTrue(helper.getLevel().getBlockState(absoluteAnchor).isAir()
                        && helper.getLevel().getBlockState(absoluteRight).isAir(), Component.literal("breaking either section must remove the complete love seat"));
        helper.assertTrue(seat.isRemoved(), Component.literal("breaking the love seat must remove its invisible seat entities"));
        helper.succeed();
    }
}
