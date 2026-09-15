package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.items.ObservableItemStackHandler;
import com.digitaldownpour.atouchofclass.items.SlotChangeListener;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class KeyCutterMenu extends AbstractContainerMenu {
    private HashSet<String> keyableTypes = new HashSet<String>(Arrays.asList(
            ATouchofClass.MODID + ":door_key",
            ATouchofClass.MODID + ":tall_oak_door",
            ATouchofClass.MODID + ":tall_spruce_door",
            ATouchofClass.MODID + ":tall_jungle_door",
            ATouchofClass.MODID + ":tall_acacia_door",
            ATouchofClass.MODID + ":tall_dark_oak_door",
            ATouchofClass.MODID + ":tall_cherry_door",
            // TALL_DOOR_VARIANT_KEY_CUTTER_ENTRIES
            ATouchofClass.MODID + ":tall_mangrove_door",
            ATouchofClass.MODID + ":tall_birch_door"
    ));

    private final List<SlotChangeListener> listeners = new ArrayList<>();
    private final ObservableItemStackHandler handler;

    // client constructor
    public KeyCutterMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, new ItemStackHandler(1));
    }

    // server constructor
    public KeyCutterMenu(int containerId, Inventory playerInv, IItemHandler dataInventory) {
        super(ModMenus.KEY_CUTTER_MENU.get(), containerId);

        handler = new ObservableItemStackHandler(2);

        handler.addListener((slot, stack) -> {
            if (slot <= 1) {
                for (SlotChangeListener listener : listeners) {
                    listener.onSlotChanged(slot, stack);
                }
            }
        });

        // Add crafting slots
        addSlot(new SlotItemHandler(handler, 0, 62, 51)); // slot index, x, y
        addSlot(new SlotItemHandler(handler, 1, 101, 51)); // slot index, x, y

        new ItemStackHandler(){
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
            }
        };

        // Add player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 23 + col * 18, 84 + row * 18));
            }
        }

        // Add hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInv, i, 23 + i * 18, 142));
        }

    }

    public void addListener(SlotChangeListener listener) {
        listeners.add(listener);
    }

    public void craft(String keyCode, String keyableName) {
        ItemStack stack = slots.getFirst().getItem();
        stack.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, new KeyableInfo(keyCode, keyableName));
        slots.get(0).remove(stack.getCount());
        slots.get(1).set(stack);
    }

    public boolean areSlotsKeyable() {
        return keyableTypes.contains(slots.get(0).getItem().getItem().toString());
    }

    public boolean isOutputEmpty() {
        return !slots.get(1).hasItem();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot sourceSlot = slots.get(i);

        if (sourceSlot != null && sourceSlot.hasItem()) {
            ItemStack sourceStack = sourceSlot.getItem();
            movedStack = sourceStack.copy();

            switch (i) {
                case 0:
                case 1:
                    if (!moveItemStackTo(sourceStack, 2, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                    break;
                default:
                    if (!moveItemStackTo(sourceStack, 0, 1, true)) {
                        return ItemStack.EMPTY;
                    }
            }

            if (sourceStack.isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
            } else {
                sourceSlot.setChanged();
            }
        }

        return movedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
        //return this.blockEntity != null && this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity;
    }
}
