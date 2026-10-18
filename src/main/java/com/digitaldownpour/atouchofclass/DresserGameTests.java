package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.DresserBlock;
import com.digitaldownpour.atouchofclass.entities.DresserBlockEntity;
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

public class DresserGameTests {

    public static void everyVariantUsesTheSharedSixRowInventory(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(2, 1, 2);
        var player = helper.makeMockPlayer(GameType.CREATIVE);

        for (var dresserHolder : ModBlocks.dresserBlocks()) {
            helper.setBlock(relativePos, dresserHolder.get());
            var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
            helper.assertTrue(blockEntity instanceof DresserBlockEntity, Component.literal(dresserHolder.getId() + " must create a dresser block entity"));
            DresserBlockEntity dresser = (DresserBlockEntity) blockEntity;
            helper.assertTrue(dresser.getContainerSize() == 54, Component.literal(dresserHolder.getId() + " must expose 54 storage slots"));
            helper.assertTrue(dresser.createMenu(1, player.getInventory(), player) instanceof ChestMenu, Component.literal(dresserHolder.getId() + " must open the six-row chest menu"));
            helper.setBlock(relativePos, Blocks.AIR);
        }
        helper.succeed();
    }

    public static void dresserBuildsAndBreaksAsOnePersistentFourPartBlock(GameTestHelper helper) {
        DresserBlock block = ModBlocks.DRESSERS.get("acacia_dresser").get();
        BlockPos relativeAnchor = new BlockPos(3, 1, 3);
        BlockPos absoluteAnchor = helper.absolutePos(relativeAnchor);
        Direction facing = Direction.NORTH;

        helper.setBlock(relativeAnchor.below(), Blocks.STONE);
        helper.setBlock(relativeAnchor.below().east(), Blocks.STONE);
        var anchorState = block.defaultBlockState()
                .setValue(DresserBlock.FACING, facing)
                .setValue(DresserBlock.SIDE, DresserBlock.Side.LEFT)
                .setValue(DresserBlock.UPPER, false);
        helper.setBlock(relativeAnchor, anchorState);
        block.setPlacedBy(helper.getLevel(), absoluteAnchor, anchorState, null, ItemStack.EMPTY);

        int blockEntityCount = 0;
        for (boolean upper : new boolean[]{false, true}) {
            for (DresserBlock.Side side : DresserBlock.Side.values()) {
                BlockPos partPos = DresserBlock.partPos(absoluteAnchor, facing, side, upper);
                var state = helper.getLevel().getBlockState(partPos);
                helper.assertTrue(state.is(block), Component.literal("every dresser section must be placed"));
                helper.assertTrue(state.getValue(DresserBlock.SIDE) == side, Component.literal("each dresser section must have the correct side"));
                helper.assertTrue(state.getValue(DresserBlock.UPPER) == upper, Component.literal("each dresser section must have the correct height"));
                if (helper.getLevel().getBlockEntity(partPos) != null) blockEntityCount++;
            }
        }
        helper.assertTrue(blockEntityCount == 1, Component.literal("only the lower-left dresser section may own storage"));

        DresserBlockEntity dresser = (DresserBlockEntity) helper.getLevel().getBlockEntity(absoluteAnchor);
        dresser.setItem(53, new ItemStack(Items.DIAMOND, 3));
        var saved = dresser.saveWithoutMetadata(helper.getLevel().registryAccess());
        var restored = new DresserBlockEntity(absoluteAnchor, anchorState);
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        helper.assertTrue(restored.getItem(53).is(Items.DIAMOND) && restored.getItem(53).getCount() == 3, Component.literal("all 54 dresser slots must persist through saving and loading"));

        dresser.clearContent();
        BlockPos upperRight = DresserBlock.partPos(
                absoluteAnchor, facing, DresserBlock.Side.RIGHT, true);
        helper.getLevel().destroyBlock(upperRight, false);
        for (boolean upper : new boolean[]{false, true}) {
            for (DresserBlock.Side side : DresserBlock.Side.values()) {
                helper.assertTrue(helper.getLevel().getBlockState(
                                DresserBlock.partPos(absoluteAnchor, facing, side, upper)).isAir(), Component.literal("breaking any dresser section must remove the entire dresser"));
            }
        }
        helper.succeed();
    }
}
