package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.SofaBlock;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class SofaGameTests {
    @GameTest(template = "chandelier_test")
    public static void sofaPlacesBreaksAndSupportsThreeIndependentSeats(GameTestHelper helper) {
        SofaBlock block = ModBlocks.LEATHER_SOFA.get();
        BlockPos relativeCenter = new BlockPos(3, 1, 3);
        BlockPos absoluteCenter = helper.absolutePos(relativeCenter);
        Direction facing = Direction.NORTH;

        for (SofaBlock.Section section : SofaBlock.Section.values()) {
            helper.setBlock(SofaBlock.partPos(relativeCenter, facing, section).below(), Blocks.STONE);
        }
        var centerState = block.defaultBlockState()
                .setValue(SofaBlock.FACING, facing)
                .setValue(SofaBlock.SECTION, SofaBlock.Section.MIDDLE);
        helper.setBlock(relativeCenter, centerState);
        block.setPlacedBy(helper.getLevel(), absoluteCenter, centerState, null, ItemStack.EMPTY);

        helper.assertTrue(helper.getLevel().getBlockState(absoluteCenter)
                        .getValue(SofaBlock.SECTION) == SofaBlock.Section.MIDDLE,
                "the clicked sofa position must be its middle section");

        List<SeatEntity> seats = new ArrayList<>();
        for (SofaBlock.Section section : SofaBlock.Section.values()) {
            BlockPos sectionPos = SofaBlock.partPos(absoluteCenter, facing, section);
            var sectionState = helper.getLevel().getBlockState(sectionPos);
            helper.assertTrue(sectionState.is(block)
                            && sectionState.getValue(SofaBlock.SECTION) == section,
                    "each sofa section must be placed in the correct position");

            SeatEntity seat = new SeatEntity(ModEntities.LOVE_SEAT_SEAT.get(), helper.getLevel());
            seat.setPos(sectionPos.getX() + 0.5, sectionPos.getY() + 0.1, sectionPos.getZ() + 0.5);
            helper.getLevel().addFreshEntity(seat);
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            helper.assertTrue(player.startRiding(seat, true),
                    "each sofa cushion must accept an independent player");
            helper.assertTrue(seat.hasPassenger(player),
                    "each sofa cushion must hold its own player passenger");
            seats.add(seat);
        }

        BlockPos middle = SofaBlock.partPos(absoluteCenter, facing, SofaBlock.Section.MIDDLE);
        helper.getLevel().destroyBlock(middle, false);
        for (SofaBlock.Section section : SofaBlock.Section.values()) {
            helper.assertTrue(helper.getLevel().getBlockState(
                    SofaBlock.partPos(absoluteCenter, facing, section)).isAir(),
                    "breaking any section must remove the complete sofa");
        }
        helper.assertTrue(seats.stream().allMatch(SeatEntity::isRemoved),
                "breaking the sofa must remove all three invisible seat entities");
        helper.succeed();
    }
}
