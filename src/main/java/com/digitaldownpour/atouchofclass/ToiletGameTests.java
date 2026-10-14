package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.ToiletBlock;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class ToiletGameTests {
    @GameTest(template = "chandelier_test")
    public static void closedLidSupportsSitting(GameTestHelper helper) {
        checkSitting(helper, false);
    }

    @GameTest(template = "chandelier_test")
    public static void openLidSupportsSitting(GameTestHelper helper) {
        checkSitting(helper, true);
    }

    private static void checkSitting(GameTestHelper helper, boolean open) {
        BlockPos relativePos = new BlockPos(3, 1, 3);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        var level = helper.getLevel();
        var bounds = new AABB(absolutePos).inflate(0.25);
        helper.setBlock(relativePos.below(), Blocks.STONE);

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            helper.setBlock(relativePos, ModBlocks.TOILET.get().defaultBlockState()
                    .setValue(ToiletBlock.FACING, facing).setValue(ToiletBlock.OPEN, open));
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            float approachYaw = facing.getOpposite().toYRot();
            player.setYRot(approachYaw);
            player.setYHeadRot(approachYaw);
            player.setYBodyRot(approachYaw);
            helper.useBlock(relativePos, player);
            var seats = level.getEntitiesOfClass(SeatEntity.class, bounds);
            helper.assertTrue(seats.size() == 1 && seats.getFirst().hasPassenger(player),
                    "using a toilet must seat the player in either lid state and any facing");
            var seat = seats.getFirst();
            seat.tick();
            helper.assertTrue(!seat.isRemoved(), "an occupied toilet seat must survive ticking");
            helper.assertTrue(level.getBlockState(absolutePos).getValue(ToiletBlock.OPEN) == open,
                    "sitting must preserve the lid state");
            helper.assertTrue(player.getYRot() == facing.toYRot(), "the seated player must face away from the tank");
            helper.assertTrue(player.getYHeadRot() == facing.toYRot()
                            && player.yBodyRot == facing.toYRot(),
                    "sitting must turn both the head and body toward the toilet's front");

            var otherPlayer = helper.makeMockPlayer(GameType.CREATIVE);
            helper.useBlock(relativePos, otherPlayer);
            helper.assertTrue(!otherPlayer.isPassenger() && seat.hasPassenger(player)
                            && level.getEntitiesOfClass(SeatEntity.class, bounds).size() == 1,
                    "an occupied toilet must reject another rider without creating an extra seat");

            otherPlayer.setShiftKeyDown(true);
            helper.useBlock(relativePos, otherPlayer);
            helper.assertTrue(level.getBlockState(absolutePos).getValue(ToiletBlock.OPEN) != open,
                    "sneak-use must toggle the lid");
            seat.tick();
            helper.assertTrue(!seat.isRemoved() && seat.hasPassenger(player),
                    "toggling the lid must preserve an occupied seat");

            player.stopRiding();
            seat.tick();
            helper.assertTrue(seat.isRemoved(), "dismounting must clean up the empty seat");
            helper.useBlock(relativePos, player);
            var replacement = level.getEntitiesOfClass(SeatEntity.class, bounds).getFirst();
            helper.assertTrue(replacement.hasPassenger(player), "the toilet must allow sitting again");
            level.destroyBlock(absolutePos, false);
            helper.assertTrue(replacement.isRemoved() && !player.isPassenger(),
                    "breaking the toilet must remove its seat and dismount the rider");
        }
        helper.succeed();
    }
}
