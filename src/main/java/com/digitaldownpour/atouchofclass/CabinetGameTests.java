package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import com.digitaldownpour.atouchofclass.blocks.DoorCabinetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class CabinetGameTests {
    @GameTest(template = "chandelier_test")
    public static void everyCabinetVariantProvidesPersistentChestStorage(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        int x = 1;

        for (var cabinetHolder : ModBlocks.cabinetBlocks()) {
            BlockPos relativePos = new BlockPos(x, 2, 2);
            helper.setBlock(relativePos, cabinetHolder.get());

            var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
            helper.assertTrue(blockEntity instanceof CabinetBlockEntity,
                    cabinetHolder.getId() + " must create a cabinet block entity");
            CabinetBlockEntity cabinet = (CabinetBlockEntity) blockEntity;

            helper.assertTrue(cabinet.getContainerSize() == 27,
                    cabinetHolder.getId() + " must expose 27 storage slots");
            helper.assertTrue(cabinet.createMenu(1, player.getInventory(), player) instanceof ChestMenu,
                    cabinetHolder.getId() + " must open the standard chest menu");

            cabinet.setItem(0, new ItemStack(Items.DIAMOND, 3));
            var saved = cabinet.saveWithoutMetadata(helper.getLevel().registryAccess());
            var restored = new CabinetBlockEntity(helper.absolutePos(relativePos), cabinet.getBlockState());
            restored.loadWithComponents(saved, helper.getLevel().registryAccess());
            helper.assertTrue(restored.getItem(0).is(Items.DIAMOND) && restored.getItem(0).getCount() == 3,
                    cabinetHolder.getId() + " must preserve stored items when saved and loaded");

            cabinet.clearContent();
            helper.setBlock(relativePos, Blocks.AIR);
            x += 2;
        }

        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void matchingDoorCabinetsJoinWhenStacked(GameTestHelper helper) {
        BlockPos lowerPos = new BlockPos(2, 1, 2);
        BlockPos upperPos = lowerPos.above();
        var cabinet = ModBlocks.DARK_OAK_DOOR_CABINET.get();
        var northFacing = cabinet.defaultBlockState().setValue(DoorCabinetBlock.FACING, Direction.NORTH);

        helper.setBlock(lowerPos, northFacing);
        helper.setBlock(upperPos, northFacing);

        var lower = helper.getBlockState(lowerPos);
        var upper = helper.getBlockState(upperPos);
        helper.assertTrue(lower.getValue(DoorCabinetBlock.JOINED_ABOVE),
                "the lower door cabinet must join the matching cabinet above");
        helper.assertTrue(upper.getValue(DoorCabinetBlock.JOINED_BELOW),
                "the upper door cabinet must join the matching cabinet below");

        helper.setBlock(upperPos, northFacing.setValue(DoorCabinetBlock.FACING, Direction.EAST));
        lower = helper.getBlockState(lowerPos);
        upper = helper.getBlockState(upperPos);
        helper.assertTrue(!lower.getValue(DoorCabinetBlock.JOINED_ABOVE),
                "door cabinets facing different directions must not join");
        helper.assertTrue(!upper.getValue(DoorCabinetBlock.JOINED_BELOW),
                "a rotated door cabinet must not join the cabinet below");

        helper.succeed();
    }
}
