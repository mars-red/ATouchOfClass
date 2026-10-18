package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.ChairBlock;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;

public class ChairGameTests {

    public static void chairSeatsPlayerAndCleansUpSeatWhenBroken(GameTestHelper helper) {
        ChairBlock block = ModBlocks.LEATHER_CHAIR.get();
        BlockPos relativePos = new BlockPos(3, 1, 3);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.setBlock(relativePos.below(), Blocks.STONE);
        helper.setBlock(relativePos, block.defaultBlockState().setValue(ChairBlock.FACING, Direction.NORTH));

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        helper.useBlock(relativePos, player);
        var seats = helper.getLevel().getEntitiesOfClass(SeatEntity.class, new AABB(absolutePos).inflate(0.25));
        helper.assertTrue(seats.size() == 1, Component.literal("using the chair must create exactly one invisible seat"));
        helper.assertTrue(seats.getFirst().hasPassenger(player), Component.literal("using the chair must seat the player"));

        helper.getLevel().destroyBlock(absolutePos, false);
        helper.assertTrue(helper.getLevel().getBlockState(absolutePos).isAir(), Component.literal("breaking the chair must remove the chair block"));
        helper.assertTrue(seats.getFirst().isRemoved(), Component.literal("breaking the chair must remove its invisible seat entity"));
        helper.succeed();
    }
}
