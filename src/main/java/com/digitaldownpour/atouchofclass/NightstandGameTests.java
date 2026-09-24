package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.NightstandBlock;
import com.digitaldownpour.atouchofclass.entities.NightstandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class NightstandGameTests {
    @GameTest(template = "chandelier_test")
    public static void oakNightstandIsFullHeightAndProvidesPersistentChestStorage(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(2, 1, 2);
        helper.setBlock(relativePos, ModBlocks.OAK_NIGHTSTAND.get());

        var state = helper.getBlockState(relativePos);
        var shape = state.getShape(helper.getLevel(), helper.absolutePos(relativePos));
        helper.assertTrue(shape.max(Direction.Axis.Y) == 1.0,
                "oak nightstand must occupy the full 16-unit block height");
        helper.assertTrue(state.isFaceSturdy(helper.getLevel(), helper.absolutePos(relativePos), Direction.UP),
                "oak nightstand must support blocks and decorations on its top face");
        helper.assertTrue(state.getValue(NightstandBlock.FACING) == Direction.NORTH,
                "oak nightstand must use the shared horizontal-facing state");

        var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
        helper.assertTrue(blockEntity instanceof NightstandBlockEntity,
                "oak nightstand must create its storage block entity");
        NightstandBlockEntity nightstand = (NightstandBlockEntity) blockEntity;
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        helper.assertTrue(nightstand.getContainerSize() == 27,
                "oak nightstand must expose the 27 slots of a single chest");
        helper.assertTrue(nightstand.createMenu(1, player.getInventory(), player) instanceof ChestMenu,
                "oak nightstand must open the standard three-row chest menu");

        nightstand.setItem(0, new ItemStack(Items.DIAMOND, 3));
        var saved = nightstand.saveWithoutMetadata(helper.getLevel().registryAccess());
        var restored = new NightstandBlockEntity(helper.absolutePos(relativePos), state);
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.getItem(0).is(Items.DIAMOND) && restored.getItem(0).getCount() == 3,
                "oak nightstand must preserve stored items when saved and loaded");

        helper.succeed();
    }
}
