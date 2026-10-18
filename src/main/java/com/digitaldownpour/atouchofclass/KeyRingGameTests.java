package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import com.digitaldownpour.atouchofclass.items.KeyRingItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

public class KeyRingGameTests {

    public static void survivalBreakAndPickupCodedDoor(GameTestHelper helper) {
        checkSurvivalDoorDrops(helper, false, false);
    }

    public static void survivalBreakAndPickupPreviouslySavedCodedDoor(GameTestHelper helper) {
        checkSurvivalDoorDrops(helper, true, false);
    }

    public static void survivalBreakAndPickupAfterUsingCodedDoor(GameTestHelper helper) {
        checkSurvivalDoorDrops(helper, false, true);
    }

    private static void checkSurvivalDoorDrops(GameTestHelper helper, boolean previouslySaved, boolean toggle) {
        var level = helper.getLevel();
        var player = new FakePlayer(level,
                new GameProfile(UUID.randomUUID(), "door-drop-test"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        BlockPos base = helper.absolutePos(new BlockPos(2, 2, 2));
        KeyableInfo expected = new KeyableInfo("vault-code", "Vault Door");
        for (int part = 0; part < 3; part++) {
            level.setBlockAndUpdate(base.below(), Blocks.STONE.defaultBlockState());
            if (previouslySaved) {
                level.setBlockAndUpdate(base, ModBlocks.TALL_BIRCH_DOOR.get().defaultBlockState());
                var door = (TallDoorBlockEntity) level.getBlockEntity(base);
                // Older worlds only stored the code in the bottom section.
                var tag = new CompoundTag();
                tag.putString("keyCode", expected.keyCode());
                tag.putString("keyableName", expected.keyableName());
                door.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
            } else {
                ItemStack item = new ItemStack(ModItems.TALL_BIRCH_DOOR_ITEM.get());
                item.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, expected);
                var hit = new BlockHitResult(
                        Vec3.atCenterOf(base.below()),
                        Direction.UP, base.below(), false);
                // ItemStack.useOn includes NeoForge's deferred placement callbacks.
                helper.assertTrue(item.useOn(new UseOnContext(
                        level, player, InteractionHand.MAIN_HAND, item, hit)).consumesAction(), Component.literal("Coded door item must place successfully"));
            }
            if (toggle) {
                player.setItemInHand(InteractionHand.MAIN_HAND, key(expected.keyCode(), 1));
                helper.useBlock(new BlockPos(2, 3, 2), player);
                helper.assertTrue(level.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Matching key must open door"));
                helper.useBlock(new BlockPos(2, 3, 2), player);
                helper.assertTrue(!level.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Matching key must close door"));
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            helper.assertTrue(player.gameMode.destroyBlock(base.above(part)), Component.literal("Survival break must succeed"));
            for (int section = 0; section < 3; section++) {
                helper.assertTrue(level.getBlockState(base.above(section)).isAir(), Component.literal("All door sections must be removed"));
            }
            var drops = level.getEntitiesOfClass(ItemEntity.class,
                    new AABB(base).inflate(1, 3, 1));
            helper.assertTrue(drops.size() == 1 && drops.getFirst().getItem().getCount() == 1, Component.literal("Breaking section " + part + " must spawn exactly one door"));
            var drop = drops.getFirst();
            helper.assertTrue(expected.equals(drop.getItem().get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE)), Component.literal("Dropped section " + part + " must preserve code and name"));
            player.getInventory().clearContent();
            drop.setNoPickUpDelay();
            drop.playerTouch(player);
            helper.assertTrue(drop.isRemoved(), Component.literal("Door must be picked up"));
            helper.assertTrue(player.getInventory().getNonEquipmentItems().stream().anyMatch(stack ->
                            expected.equals(stack.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE))), Component.literal("Inventory door must preserve code and name"));
        }
        helper.succeed();
    }

    private static ItemStack key(String code, int count) {
        ItemStack key = new ItemStack(ModItems.DOOR_KEY.get(), count);
        key.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, new KeyableInfo(code, "Test key"));
        return key;
    }

    public static void anvilRenamingPreservesKeys(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.giveExperienceLevels(10);
        for (boolean filled : new boolean[] {false, true}) {
            ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
            if (filled) ring.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(key("front-door", 1))));
            var contents = ring.get(DataComponents.CONTAINER);
            var anvil = new AnvilMenu(1, player.getInventory());
            anvil.getSlot(0).set(ring);
            anvil.setItemName("House Keys");
            ItemStack renamed = anvil.getSlot(2).getItem().copy();
            helper.assertTrue(!renamed.isEmpty(), Component.literal("Anvil must offer a renamed ring"));
            helper.assertTrue(anvil.getSlot(2).mayPickup(player), Component.literal("Survival player must be able to take renamed ring"));
            helper.assertTrue(anvil.getCost() == 1, Component.literal("Fresh ring rename must cost one level"));
            helper.assertTrue(renamed.getHoverName().getString().equals("House Keys"), Component.literal("Custom name must display"));
            helper.assertTrue(contents.equals(renamed.get(DataComponents.CONTAINER)), Component.literal("Renaming must preserve all keys"));
            helper.assertTrue(renamed.hasNonDefault(DataComponents.CONTAINER) == filled, Component.literal("Renaming must preserve texture state"));
            var registries = helper.getLevel().registryAccess();
            ItemStack restored = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), renamed).getOrThrow()).getOrThrow();
            helper.assertTrue(restored.getHoverName().getString().equals("House Keys"), Component.literal("Name must survive saving"));
            if (filled) helper.assertTrue(KeyRingItem.matchesCode(restored, "front-door"), Component.literal("Renamed key ring must still unlock matching doors"));
            anvil.getSlot(0).set(restored);
            anvil.setItemName("");
            ItemStack cleared = anvil.getSlot(2).getItem();
            helper.assertTrue(!cleared.isEmpty() && !cleared.has(DataComponents.CUSTOM_NAME), Component.literal("Anvil must allow clearing a custom name"));
            helper.assertTrue(contents.equals(cleared.get(DataComponents.CONTAINER)), Component.literal("Clearing name must preserve keys"));
        }
        helper.succeed();
    }

    public static void textureStateTracksFirstAndLastKey(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inventory = player.getInventory();
        ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
        inventory.setItem(0, ring);
        var menu = new KeyRingMenu(1, inventory, 0);
        // The item model uses has_component(container, ignore_default=true).
        helper.assertTrue(!ring.hasNonDefault(DataComponents.CONTAINER), Component.literal("New ring must use the empty texture"));
        menu.setCarried(key("alpha", 1));
        menu.clicked(15, 0, ClickType.PICKUP, player);
        helper.assertTrue(ring.hasNonDefault(DataComponents.CONTAINER), Component.literal("A key in the last slot must select the filled texture"));
        var registries = helper.getLevel().registryAccess();
        ItemStack restored = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), ring).getOrThrow()).getOrThrow();
        helper.assertTrue(restored.hasNonDefault(DataComponents.CONTAINER), Component.literal("Filled texture state must survive saving"));
        menu.quickMoveStack(player, 15);
        helper.assertTrue(!ring.hasNonDefault(DataComponents.CONTAINER), Component.literal("Removing the last key must restore the empty texture"));
        helper.assertTrue(ring.get(DataComponents.CONTAINER).equals(ItemContainerContents.EMPTY), Component.literal("Empty slots must normalize to the default container"));
        helper.succeed();
    }

    public static void transfersCapacityAndPersistence(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inventory = player.getInventory();
        ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
        inventory.setItem(0, ring);
        var menu = new KeyRingMenu(1, inventory, 0);
        helper.assertTrue(!menu.getSlot(0).mayPlace(new ItemStack(ModItems.DOOR_KEY.get())), Component.literal("Blank keys must be rejected"));
        helper.assertTrue(!menu.getSlot(0).mayPlace(key("", 1)), Component.literal("Empty codes must be rejected"));
        helper.assertTrue(!menu.getSlot(0).mayPlace(ring), Component.literal("Rings cannot be nested"));
        helper.assertTrue(!menu.getSlot(0).mayPlace(new ItemStack(Items.STONE)), Component.literal("Non-keys must be rejected"));
        inventory.setItem(9, key("alpha", 20));
        menu.quickMoveStack(player, 16);
        helper.assertTrue(inventory.getItem(9).getCount() == 4, Component.literal("Only sixteen keys may be transferred"));
        for (int i = 0; i < 16; i++) helper.assertTrue(menu.getSlot(i).getItem().getCount() == 1, Component.literal("One key per slot"));
        menu.quickMoveStack(player, 16);
        helper.assertTrue(inventory.getItem(9).getCount() == 4, Component.literal("Full ring must reject more keys"));
        var registries = helper.getLevel().registryAccess();
        ItemStack restored = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), ring).getOrThrow()).getOrThrow();
        helper.assertTrue(restored.get(DataComponents.CONTAINER).nonEmptyStream().count() == 16, Component.literal("Keys must survive serialization"));
        helper.assertTrue(KeyRingItem.matchesCode(restored, "alpha"), Component.literal("Key codes must survive serialization"));
        menu.removed(player);
        menu = new KeyRingMenu(2, inventory, 0);
        menu.quickMoveStack(player, 0);
        helper.assertTrue(ring.get(DataComponents.CONTAINER).nonEmptyStream().count() == 15, Component.literal("Removal must persist immediately"));
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(ModItems.DOOR_KEY.get())) total += inventory.getItem(i).getCount();
        }
        helper.assertTrue(total == 5, Component.literal("Transfer back must preserve the exact number of keys"));
        helper.succeed();
    }

    public static void openRingCannotMove(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inventory = player.getInventory();
        for (int ringSlot : new int[] {0, 40}) {
            ItemStack ring = new ItemStack(ModItems.KEY_RING.get());
            inventory.setItem(ringSlot, ring);
            inventory.setItem(9, key("alpha", 1));
            var menu = new KeyRingMenu(1, inventory, ringSlot);
            menu.clicked(16, ringSlot, ClickType.SWAP, player);
            helper.assertTrue(inventory.getItem(ringSlot) == ring, Component.literal("Hotbar/offhand swap must not move open ring"));
            if (ringSlot == 0) {
                for (ClickType type : new ClickType[] {ClickType.PICKUP, ClickType.QUICK_MOVE, ClickType.THROW, ClickType.CLONE}) {
                    menu.clicked(43, 0, type, player);
                    helper.assertTrue(inventory.getItem(0) == ring, Component.literal("Open ring must remain in its inventory slot"));
                }
            }
            inventory.setItem(ringSlot, ring.copy());
            helper.assertTrue(!menu.stillValid(player), Component.literal("Replacing the ring must invalidate its menu"));
            menu.quickMoveStack(player, 16);
            helper.assertTrue(inventory.getItem(9).getCount() == 1, Component.literal("Invalid menu must not transfer keys"));
        }
        helper.succeed();
    }

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
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), ring);
        helper.useBlock(base.above(2), player);
        helper.assertTrue(helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("A later matching key must open the door exactly once"));
        helper.useBlock(base.above(), player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Ring must also close the door"));
        ring.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(key("wrong", 1))));
        helper.useBlock(base, player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Wrong key must not open door"));
        ring.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        helper.useBlock(base, player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Empty ring must not open locked door"));
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), key("match", 1));
        helper.useBlock(base, player);
        helper.assertTrue(helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Individual keys must still work"));
        door.setKeyableInfo(new KeyableInfo("", ""));
        player.getInventory().setItem(player.getInventory().getSelectedSlot(), ring);
        helper.useBlock(base, player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Empty ring must operate unlocked doors"));
        helper.succeed();
    }

    public static void placingAndBreakingCodedDoorItemPreservesKeyCode(GameTestHelper helper) {
        BlockPos stoneRelative = new BlockPos(2, 1, 2);
        helper.setBlock(stoneRelative, Blocks.STONE);
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stonePosAbs = helper.absolutePos(stoneRelative);

        ItemStack doorItem = new ItemStack(ModItems.TALL_BIRCH_DOOR_ITEM.get());
        doorItem.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, new KeyableInfo("vault-code", "Vault Door"));

        var hitResult = new BlockHitResult(
                Vec3.atCenterOf(stonePosAbs), Direction.UP, stonePosAbs, false);
        var useOnContext = new UseOnContext(level, player, InteractionHand.MAIN_HAND, doorItem, hitResult);
        var result = doorItem.useOn(useOnContext);
        helper.assertTrue(result.consumesAction(), Component.literal("Placement must succeed"));

        BlockPos basePosAbs = stonePosAbs.above();
        for (int part = 0; part < 3; part++) {
            var pos = basePosAbs.above(part);
            var be = level.getBlockEntity(pos);
            helper.assertTrue(be instanceof TallDoorBlockEntity, Component.literal("Each real-placed part must have a TallDoorBlockEntity: part " + part));
            helper.assertTrue(((TallDoorBlockEntity) be).getKeyCode().equals("vault-code"), Component.literal("Part " + part + " must carry the code after real placement, got '" + ((TallDoorBlockEntity) be).getKeyCode() + "'"));
        }

        for (int part = 0; part < 3; part++) {
            var pos = basePosAbs.above(part);
            var state = level.getBlockState(pos);
            var be = level.getBlockEntity(pos);
            List<ItemStack> drops = Block.getDrops(state, level, pos, be);
            helper.assertTrue(drops.size() == 1, Component.literal("Breaking real-placed part " + part + " must drop exactly one item"));
            KeyableInfo info = drops.get(0).get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE);
            helper.assertTrue(info != null && "vault-code".equals(info.keyCode()), Component.literal("Dropped item from real-placed part " + part + " must carry the key code, got " + info));
        }
        helper.succeed();
    }

    public static void breakingCodedDoorPreservesKeyCode(GameTestHelper helper) {
        BlockPos base = new BlockPos(2, 1, 2);
        helper.setBlock(base.below(), Blocks.STONE);
        for (int part = 0; part < 3; part++) {
            helper.setBlock(base.above(part), ModBlocks.TALL_BIRCH_DOOR.get().defaultBlockState().setValue(TallDoorBlock.PART, part));
        }
        var level = helper.getLevel();
        var basePosAbs = helper.absolutePos(base);
        var door = (TallDoorBlockEntity) level.getBlockEntity(basePosAbs);
        door.setKeyableInfo(new KeyableInfo("vault-code", "Vault Door"));

        for (int part = 0; part < 3; part++) {
            var pos = helper.absolutePos(base.above(part));
            var state = level.getBlockState(pos);
            var be = level.getBlockEntity(pos);
            helper.assertTrue(be instanceof TallDoorBlockEntity, Component.literal("Each part must keep a TallDoorBlockEntity: part " + part));
            helper.assertTrue(((TallDoorBlockEntity) be).getKeyCode().equals("vault-code"), Component.literal("Part " + part + " block entity must carry the synced key code"));
            List<ItemStack> drops = Block.getDrops(state, level, pos, be);
            helper.assertTrue(drops.size() == 1, Component.literal("Breaking part " + part + " must drop exactly one item"));
            ItemStack dropped = drops.get(0);
            KeyableInfo info = dropped.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE);
            helper.assertTrue(info != null && "vault-code".equals(info.keyCode()), Component.literal("Dropped item from part " + part + " must carry the key code, got " + info));
        }
        helper.succeed();
    }
}
