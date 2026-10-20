package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.FourPostBedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import java.util.List;
import net.minecraft.network.chat.Component;

public class FourPostBedGameTests {
    private static final BlockPos ANCHOR = new BlockPos(3, 1, 3);

    private static void place(GameTestHelper helper, FourPostBedBlock bed, Direction facing) {
        var level = helper.getLevel();
        var anchor = helper.absolutePos(ANCHOR);
        level.destroyBlock(anchor, false);
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) helper.setBlock(ANCHOR.offset(x, -1, z), Blocks.STONE);
        var state = bed.defaultBlockState().setValue(FourPostBedBlock.FACING, facing.getOpposite());
        helper.setBlock(ANCHOR, state);
        bed.setPlacedBy(level, anchor, state, null, ItemStack.EMPTY);
    }

    private static void checkSleepers(GameTestHelper helper, Direction facing) {
        var level = helper.getLevel();
        var leftPos = helper.absolutePos(ANCHOR).relative(facing.getOpposite());
        var rightPos = leftPos.relative(facing.getClockWise());
        var left = helper.makeMockPlayer(GameType.CREATIVE);
        var right = helper.makeMockPlayer(GameType.CREATIVE);
        left.startSleeping(leftPos);
        right.startSleeping(rightPos);
        for (var sleeper : List.of(left, right)) {
            var pos = sleeper.getSleepingPos().orElseThrow();
            helper.assertTrue(Math.abs(sleeper.getY() - pos.getY() - 15.0 / 16) < 0.000001, Component.literal("the bed sleep-height API must position sleepers above the mattress immediately"));
            NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(sleeper));
            helper.assertTrue(Math.abs(sleeper.getY() - pos.getY() - 15.0 / 16) < 0.000001, Component.literal("sleeping body must rest two model units above the highest bedding surface"));
            helper.assertTrue(sleeper.yo == sleeper.getY() && sleeper.yOld == sleeper.getY(), Component.literal("camera and body interpolation must not sink back through the mattress"));
            helper.assertTrue(sleeper.getX() == pos.getX() + 0.5 && sleeper.getZ() == pos.getZ() + 0.5, Component.literal("height correction must keep each sleeper on the selected mattress side"));
            helper.assertTrue(sleeper.getBedOrientation() == facing.getOpposite(), Component.literal("sleep direction must remain unchanged"));
            helper.assertTrue(level.getBlockState(pos).getValue(FourPostBedBlock.OCCUPIED), Component.literal("each sleeping side must stay occupied"));
            NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(sleeper));
            helper.assertTrue(sleeper.getY() == pos.getY() + FourPostBedSleepEvents.SLEEP_HEIGHT, Component.literal("repeated updates must not accumulate vertical offsets"));
        }
        left.stopSleeping();
        double wakeY = left.getY();
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(left));
        helper.assertTrue(!left.isSleeping() && left.getY() == wakeY, Component.literal("correction must stop when the player wakes"));
        helper.assertTrue(!level.getBlockState(leftPos).getValue(FourPostBedBlock.OCCUPIED)
                        && level.getBlockState(rightPos).getValue(FourPostBedBlock.OCCUPIED), Component.literal("waking one player must leave the other side occupied"));
        right.stopSleeping();
    }

    public static void everyWoodAndBeddingColorSleepsAboveMattress(GameTestHelper helper) {
        helper.assertTrue(ModBlocks.FOUR_POST_BEDS.size() == 208, Component.literal("all thirteen woods and sixteen colors must be covered"));
        for (var bed : ModBlocks.FOUR_POST_BEDS.values()) {
            place(helper, bed.get(), Direction.SOUTH);
            checkSleepers(helper, Direction.SOUTH);
        }
        helper.succeed();
    }

    public static void bothMattressSidesKeepCorrectHeightInEveryFacing(GameTestHelper helper) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            place(helper, ModBlocks.DARK_OAK_RED_FOUR_POST_BED.get(), facing);
            checkSleepers(helper, facing);
        }
        helper.succeed();
    }

    public static void vanillaBedsKeepVanillaSleepingHeight(GameTestHelper helper) {
        helper.setBlock(ANCHOR.below(), Blocks.STONE);
        helper.setBlock(ANCHOR, Blocks.BED.red());
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        var pos = helper.absolutePos(ANCHOR);
        player.startSleeping(pos);
        double y = player.getY();
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(player));
        helper.assertTrue(player.getY() == y && y == pos.getY() + 11.0 / 16, Component.literal("normal vanilla beds must retain their original sleep height"));
        player.stopSleeping();
        helper.succeed();
    }
}
