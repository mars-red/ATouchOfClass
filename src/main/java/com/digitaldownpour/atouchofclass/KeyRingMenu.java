package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.items.KeyRingItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

@SuppressWarnings("resource") // Minecraft owns and closes these shared levels.
public class KeyRingMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final int ringSlot;
    private final ItemStack ring;
    private boolean keysLoaded;
    private final SimpleContainer keys = new SimpleContainer(KeyRingItem.CAPACITY) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (keysLoaded) persistKeys();
        }
    };

    public KeyRingMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readVarInt());
    }

    public KeyRingMenu(int id, Inventory inventory, int ringSlot) {
        super(ModMenus.KEY_RING_MENU.get(), id);
        this.inventory = inventory;
        this.ringSlot = ringSlot;
        this.ring = inventory.getItem(ringSlot);
        NonNullList<ItemStack> contents = NonNullList.withSize(KeyRingItem.CAPACITY, ItemStack.EMPTY);
        ring.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(contents);
        for (int i = 0; i < contents.size(); i++) {
            keys.setItem(i, contents.get(i));
            addSlot(new Slot(keys, i, 8 + (i % 4) * 18, 18 + (i / 4) * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return KeyRingItem.isCodedKey(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        // Persist each edit, including partial transfers, before the ring can leave the inventory.
        keysLoaded = true;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addInventorySlot(col + row * 9 + 9, 104 + col * 18, 18 + row * 18);
        }
        for (int col = 0; col < 9; col++) addInventorySlot(col, 104 + col * 18, 76);
    }

    private void persistKeys() {
        if (!inventory.player.level().isClientSide() && stillValid(inventory.player)) {
            NonNullList<ItemStack> updated = NonNullList.withSize(KeyRingItem.CAPACITY, ItemStack.EMPTY);
            for (int i = 0; i < updated.size(); i++) updated.set(i, keys.getItem(i));
            ring.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(updated));
            inventory.setChanged();
        }
    }

    private void addInventorySlot(int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            @Override
            public boolean mayPickup(Player player) {
                return index != ringSlot;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return index != ringSlot;
            }
        });
    }

    @Override
    public void clicked(int slot, int button, ContainerInput type, Player player) {
        if (!stillValid(player) || (type == ContainerInput.SWAP && button == ringSlot)) return;
        // Also block creative cloning of the live ring while its contents are being edited.
        if (slot >= 0 && slot < slots.size() && slots.get(slot).container == inventory
                && slots.get(slot).getContainerSlot() == ringSlot) return;
        super.clicked(slot, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        if (index < KeyRingItem.CAPACITY) {
            if (!moveItemStackTo(stack, KeyRingItem.CAPACITY, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!KeyRingItem.isCodedKey(stack)) return ItemStack.EMPTY;
            while (!stack.isEmpty()) {
                // Vanilla moves into one empty slot per call; each ring slot holds one key.
                if (!moveItemStackTo(stack, 0, KeyRingItem.CAPACITY, false)) break;
            }
            if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) source.setByPlayer(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive() && (player.level().isClientSide()
                || (inventory.getItem(ringSlot) == ring && ring.is(ModItems.KEY_RING.get())));
    }
}
