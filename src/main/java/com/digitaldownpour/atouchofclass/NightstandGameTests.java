package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.NightstandBlock;
import com.digitaldownpour.atouchofclass.entities.NightstandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;

public class NightstandGameTests {

    public static void everyNightstandIsFullHeightAndProvidesPersistentChestStorage(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        int index = 0;

        for (var nightstandHolder : ModBlocks.nightstandBlocks()) {
            BlockPos relativePos = new BlockPos(1 + index % 4 * 2, 2, 1 + index / 4 * 2);
            helper.setBlock(relativePos, nightstandHolder.get());

            var state = helper.getBlockState(relativePos);
            var absolutePos = helper.absolutePos(relativePos);
            var shape = state.getShape(helper.getLevel(), absolutePos);
            String id = nightstandHolder.getId().toString();
            helper.assertTrue(shape.max(Direction.Axis.Y) == 1.0, Component.literal(id + " must occupy the full 16-unit block height"));
            helper.assertTrue(state.isFaceSturdy(helper.getLevel(), absolutePos, Direction.UP), Component.literal(id + " must support blocks and decorations on its top face"));
            helper.assertTrue(state.getValue(NightstandBlock.FACING) == Direction.NORTH, Component.literal(id + " must use the shared horizontal-facing state"));

            var blockEntity = helper.getLevel().getBlockEntity(absolutePos);
            helper.assertTrue(blockEntity instanceof NightstandBlockEntity, Component.literal(id + " must create its storage block entity"));
            NightstandBlockEntity nightstand = (NightstandBlockEntity) blockEntity;
            helper.assertTrue(nightstand.getContainerSize() == 27, Component.literal(id + " must expose the 27 slots of a single chest"));
            helper.assertTrue(nightstand.createMenu(1, player.getInventory(), player) instanceof ChestMenu, Component.literal(id + " must open the standard three-row chest menu"));

            nightstand.setItem(0, new ItemStack(Items.DIAMOND, 3));
            var saved = nightstand.saveWithoutMetadata(helper.getLevel().registryAccess());
            var restored = new NightstandBlockEntity(absolutePos, state);
            restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
            helper.assertTrue(restored.getItem(0).is(Items.DIAMOND)
                            && restored.getItem(0).getCount() == 3, Component.literal(id + " must preserve stored items when saved and loaded"));

            nightstand.clearContent();
            helper.setBlock(relativePos, Blocks.AIR);
            index++;
        }

        helper.succeed();
    }
}
