package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import com.digitaldownpour.atouchofclass.items.KeyRingItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class KeyRingGameTests {
    private static ItemStack key(String code, int count) {
        ItemStack key = new ItemStack(ModItems.DOOR_KEY.get(), count);
        key.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, new KeyableInfo(code, "Test key"));
        return key;
    }

    @GameTest(template = "chandelier_test")
    public static void anvilRenamingPreservesKeys(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.giveExperienceLevels(10);
        for (boolean filled : new boolean[] {false, true}) {
            ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
            if (filled) ring.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(key("front-door", 1))));
            var contents = ring.get(DataComponents.CONTAINER);
            var anvil = new net.minecraft.world.inventory.AnvilMenu(1, player.getInventory());
            anvil.getSlot(0).set(ring);
            anvil.setItemName("House Keys");
            ItemStack renamed = anvil.getSlot(2).getItem().copy();
            helper.assertTrue(!renamed.isEmpty(), "Anvil must offer a renamed ring");
            helper.assertTrue(anvil.getSlot(2).mayPickup(player), "Survival player must be able to take renamed ring");
            helper.assertTrue(anvil.getCost() == 1, "Fresh ring rename must cost one level");
            helper.assertTrue(renamed.getHoverName().getString().equals("House Keys"), "Custom name must display");
            helper.assertTrue(contents.equals(renamed.get(DataComponents.CONTAINER)), "Renaming must preserve all keys");
            helper.assertTrue(renamed.hasNonDefault(DataComponents.CONTAINER) == filled, "Renaming must preserve texture state");
            var registries = helper.getLevel().registryAccess();
            ItemStack restored = ItemStack.parseOptional(registries, (net.minecraft.nbt.CompoundTag) renamed.save(registries));
            helper.assertTrue(restored.getHoverName().getString().equals("House Keys"), "Name must survive saving");
            if (filled) helper.assertTrue(KeyRingItem.matchesCode(restored, "front-door"), "Renamed key ring must still unlock matching doors");
            anvil.getSlot(0).set(restored);
            anvil.setItemName("");
            ItemStack cleared = anvil.getSlot(2).getItem();
            helper.assertTrue(!cleared.isEmpty() && !cleared.has(DataComponents.CUSTOM_NAME), "Anvil must allow clearing a custom name");
            helper.assertTrue(contents.equals(cleared.get(DataComponents.CONTAINER)), "Clearing name must preserve keys");
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void textureStateTracksFirstAndLastKey(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inventory = player.getInventory();
        ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
        inventory.setItem(0, ring);
        var menu = new KeyRingMenu(1, inventory, 0);
        // The item model uses has_component(container, ignore_default=true).
        helper.assertTrue(!ring.hasNonDefault(DataComponents.CONTAINER), "New ring must use the empty texture");
        menu.setCarried(key("alpha", 1));
        menu.clicked(15, 0, ClickType.PICKUP, player);
        helper.assertTrue(ring.hasNonDefault(DataComponents.CONTAINER), "A key in the last slot must select the filled texture");
        var registries = helper.getLevel().registryAccess();
        ItemStack restored = ItemStack.parseOptional(registries, (net.minecraft.nbt.CompoundTag) ring.save(registries));
        helper.assertTrue(restored.hasNonDefault(DataComponents.CONTAINER), "Filled texture state must survive saving");
        menu.quickMoveStack(player, 15);
        helper.assertTrue(!ring.hasNonDefault(DataComponents.CONTAINER), "Removing the last key must restore the empty texture");
        helper.assertTrue(ring.get(DataComponents.CONTAINER).equals(ItemContainerContents.EMPTY), "Empty slots must normalize to the default container");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void transfersCapacityAndPersistence(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inventory = player.getInventory();
        ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
        inventory.setItem(0, ring);
        var menu = new KeyRingMenu(1, inventory, 0);
        helper.assertTrue(!menu.getSlot(0).mayPlace(new ItemStack(ModItems.DOOR_KEY.get())), "Blank keys must be rejected");
        helper.assertTrue(!menu.getSlot(0).mayPlace(key("", 1)), "Empty codes must be rejected");
        helper.assertTrue(!menu.getSlot(0).mayPlace(ring), "Rings cannot be nested");
        helper.assertTrue(!menu.getSlot(0).mayPlace(new ItemStack(Items.STONE)), "Non-keys must be rejected");
        inventory.setItem(9, key("alpha", 20));
        menu.quickMoveStack(player, 16);
        helper.assertTrue(inventory.getItem(9).getCount() == 4, "Only sixteen keys may be transferred");
        for (int i = 0; i < 16; i++) helper.assertTrue(menu.getSlot(i).getItem().getCount() == 1, "One key per slot");
        menu.quickMoveStack(player, 16);
        helper.assertTrue(inventory.getItem(9).getCount() == 4, "Full ring must reject more keys");
        var registries = helper.getLevel().registryAccess();
        ItemStack restored = ItemStack.parseOptional(registries, (net.minecraft.nbt.CompoundTag) ring.save(registries));
        helper.assertTrue(restored.get(DataComponents.CONTAINER).nonEmptyStream().count() == 16, "Keys must survive serialization");
        helper.assertTrue(KeyRingItem.matchesCode(restored, "alpha"), "Key codes must survive serialization");
        menu.removed(player);
        menu = new KeyRingMenu(2, inventory, 0);
        menu.quickMoveStack(player, 0);
        helper.assertTrue(ring.get(DataComponents.CONTAINER).nonEmptyStream().count() == 15, "Removal must persist immediately");
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(ModItems.DOOR_KEY.get())) total += inventory.getItem(i).getCount();
        }
        helper.assertTrue(total == 5, "Transfer back must preserve the exact number of keys");
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void openRingCannotMove(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inventory = player.getInventory();
        for (int ringSlot : new int[] {0, 40}) {
            ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
            inventory.setItem(ringSlot, ring);
            inventory.setItem(9, key("alpha", 1));
            var menu = new KeyRingMenu(1, inventory, ringSlot);
            menu.clicked(16, ringSlot, ClickType.SWAP, player);
            helper.assertTrue(inventory.getItem(ringSlot) == ring, "Hotbar/offhand swap must not move open ring");
            if (ringSlot == 0) {
                for (ClickType type : new ClickType[] {ClickType.PICKUP, ClickType.QUICK_MOVE, ClickType.THROW, ClickType.CLONE}) {
                    menu.clicked(43, 0, type, player);
                    helper.assertTrue(inventory.getItem(0) == ring, "Open ring must remain in its inventory slot");
                }
            }
            inventory.setItem(ringSlot, ring.copy());
            helper.assertTrue(!menu.stillValid(player), "Replacing the ring must invalidate its menu");
            menu.quickMoveStack(player, 16);
            helper.assertTrue(inventory.getItem(9).getCount() == 1, "Invalid menu must not transfer keys");
        }
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void ringAndIndividualKeysOperateDoors(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos base = new BlockPos(2, 1, 2);
        helper.setBlock(base.below(), Blocks.STONE);
        for (int part = 0; part < 3; part++) {
            helper.setBlock(base.above(part), ModBlocks.TALL_BIRCH_DOOR.get().defaultBlockState().setValue(TallDoorBlock.PART, part));
        }
        var door = (TallDoorBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(base));
        door.setKeyableInfo(new KeyableInfo("match", "Test door"));
        ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
        ring.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(key("wrong", 1), key("match", 1), key("match", 1))));
        player.getInventory().setItem(player.getInventory().selected, ring);
        helper.useBlock(base.above(2), player);
        helper.assertTrue(helper.getBlockState(base).getValue(TallDoorBlock.OPEN), "A later matching key must open the door exactly once");
        helper.useBlock(base.above(), player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), "Ring must also close the door");
        ring.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(key("wrong", 1))));
        helper.useBlock(base, player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), "Wrong key must not open door");
        ring.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.useBlock(base, player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), "Empty ring must not open locked door");
        player.getInventory().setItem(player.getInventory().selected, key("match", 1));
        helper.useBlock(base, player);
        helper.assertTrue(helper.getBlockState(base).getValue(TallDoorBlock.OPEN), "Individual keys must still work");
        door.setKeyableInfo(new KeyableInfo("", ""));
        player.getInventory().setItem(player.getInventory().selected, ring);
        helper.useBlock(base, player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), "Empty ring must operate unlocked doors");
        helper.succeed();
    }
}
