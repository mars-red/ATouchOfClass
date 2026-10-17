package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.FourPostBedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class FourPostBedGameTests {
    private static final BlockPos ANCHOR = new BlockPos(3, 1, 3);

    private static void place(GameTestHelper helper, FourPostBedBlock bed, Direction facing) {
        var level = helper.getLevel();
        var anchor = helper.absolutePos(ANCHOR);
        level.destroyBlock(anchor, false);
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) helper.setBlock(ANCHOR.offset(x, -1, z), Blocks.STONE);
        var state = bed.defaultBlockState().setValue(FourPostBedBlock.FACING, facing);
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
        for (var sleeper : java.util.List.of(left, right)) {
            var pos = sleeper.getSleepingPos().orElseThrow();
            helper.assertTrue(Math.abs(sleeper.getY() - pos.getY() - 11.0 / 16) < 0.000001,
                    "regression setup must reproduce vanilla's too-low sleeping height");
            NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(sleeper));
            helper.assertTrue(Math.abs(sleeper.getY() - pos.getY() - 15.0 / 16) < 0.000001,
                    "sleeping body must rest two model units above the highest bedding surface");
            helper.assertTrue(sleeper.yo == sleeper.getY() && sleeper.yOld == sleeper.getY(),
                    "camera and body interpolation must not sink back through the mattress");
            helper.assertTrue(sleeper.getX() == pos.getX() + 0.5 && sleeper.getZ() == pos.getZ() + 0.5,
                    "height correction must keep each sleeper on the selected mattress side");
            helper.assertTrue(sleeper.getBedOrientation() == facing.getOpposite(), "sleep direction must remain unchanged");
            helper.assertTrue(level.getBlockState(pos).getValue(FourPostBedBlock.OCCUPIED), "each sleeping side must stay occupied");
            NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(sleeper));
            helper.assertTrue(sleeper.getY() == pos.getY() + FourPostBedSleepEvents.SLEEP_HEIGHT,
                    "repeated updates must not accumulate vertical offsets");
        }
        left.stopSleeping();
        double wakeY = left.getY();
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(left));
        helper.assertTrue(!left.isSleeping() && left.getY() == wakeY, "correction must stop when the player wakes");
        helper.assertTrue(!level.getBlockState(leftPos).getValue(FourPostBedBlock.OCCUPIED)
                        && level.getBlockState(rightPos).getValue(FourPostBedBlock.OCCUPIED),
                "waking one player must leave the other side occupied");
        right.stopSleeping();
    }

    @GameTest(template = "chandelier_test")
    public static void everyWoodAndBeddingColorSleepsAboveMattress(GameTestHelper helper) {
        helper.assertTrue(ModBlocks.FOUR_POST_BEDS.size() == 176, "all eleven woods and sixteen colors must be covered");
        for (var bed : ModBlocks.FOUR_POST_BEDS.values()) {
            place(helper, bed.get(), Direction.SOUTH);
            checkSleepers(helper, Direction.SOUTH);
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void bothMattressSidesKeepCorrectHeightInEveryFacing(GameTestHelper helper) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            place(helper, ModBlocks.DARK_OAK_RED_FOUR_POST_BED.get(), facing);
            checkSleepers(helper, facing);
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void vanillaBedsKeepVanillaSleepingHeight(GameTestHelper helper) {
        helper.setBlock(ANCHOR.below(), Blocks.STONE);
        helper.setBlock(ANCHOR, Blocks.RED_BED);
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        var pos = helper.absolutePos(ANCHOR);
        player.startSleeping(pos);
        double y = player.getY();
        NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(player));
        helper.assertTrue(player.getY() == y && y == pos.getY() + 11.0 / 16,
                "normal vanilla beds must retain their original sleep height");
        player.stopSleeping();
        helper.succeed();
    }
}
