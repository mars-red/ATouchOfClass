package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.items.ObservableItemStackHandler;
import com.digitaldownpour.atouchofclass.items.SlotChangeListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("resource") // Minecraft owns and closes these shared levels.
public class KeyCutterMenu extends AbstractContainerMenu {
    private final HashSet<String> keyableTypes = new HashSet<>(Arrays.asList(
            ATouchofClass.MODID + ":door_key",
            ATouchofClass.MODID + ":tall_oak_door",
            ATouchofClass.MODID + ":tall_spruce_door",
            ATouchofClass.MODID + ":tall_jungle_door",
            ATouchofClass.MODID + ":tall_acacia_door",
            ATouchofClass.MODID + ":tall_dark_oak_door",
            ATouchofClass.MODID + ":tall_cherry_door",
            ATouchofClass.MODID + ":tall_bamboo_door",
            ATouchofClass.MODID + ":tall_pale_oak_door",
            ATouchofClass.MODID + ":tall_crimson_door",
            ATouchofClass.MODID + ":tall_warped_door",
            ATouchofClass.MODID + ":tall_poplar_door",
            // TALL_DOOR_VARIANT_KEY_CUTTER_ENTRIES
            ATouchofClass.MODID + ":tall_mangrove_door",
            ATouchofClass.MODID + ":tall_birch_door"
    ));

    private final List<SlotChangeListener> listeners = new ArrayList<>();
    private final ObservableItemStackHandler handler;

    // client constructor
    public KeyCutterMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, new SimpleContainer(1));
    }

    // server constructor
    public KeyCutterMenu(int containerId, Inventory playerInv, Container dataInventory) {
        super(ModMenus.KEY_CUTTER_MENU.get(), containerId);
        PatioDoorVariants.VALUES.forEach(definition -> keyableTypes.add(ATouchofClass.MODID + ":" + definition.id()));

        handler = new ObservableItemStackHandler(2);

        handler.addListener((slot, stack) -> {
            if (slot <= 1) {
                for (SlotChangeListener listener : listeners) {
                    listener.onSlotChanged(slot, stack);
                }
            }
        });

        // Add crafting slots
        addSlot(new Slot(handler, 0, 62, 51)); // slot index, x, y
        addSlot(new Slot(handler, 1, 101, 51)); // slot index, x, y

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

    public boolean craft(String keyCode, String keyableName) {
        if (!areSlotsKeyable() || !isOutputEmpty()) {
            return false;
        }

        ItemStack stack = slots.getFirst().getItem().copy();
        stack.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, new KeyableInfo(keyCode, keyableName));
        slots.getFirst().remove(stack.getCount());
        slots.get(1).set(stack);
        return true;
    }

    public boolean areSlotsKeyable() {
        return keyableTypes.contains(slots.getFirst().getItem().getItem().toString());
    }

    public boolean isOutputEmpty() {
        return !slots.get(1).hasItem();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot sourceSlot = slots.get(i);

        if (sourceSlot.hasItem()) {
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
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            SimpleContainer remainingItems = new SimpleContainer(handler.getContainerSize());
            for (int slot = 0; slot < handler.getContainerSize(); slot++) {
                remainingItems.setItem(slot, handler.removeItemNoUpdate(slot));
            }
            // Vanilla cleanup returns items to the inventory and drops any overflow.
            clearContainer(player, remainingItems);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
        //return this.blockEntity != null && this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity;
    }
}
