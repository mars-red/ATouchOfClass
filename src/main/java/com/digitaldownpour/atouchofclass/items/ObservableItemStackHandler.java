package com.digitaldownpour.atouchofclass.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

import java.util.ArrayList;
import java.util.List;

public class ObservableItemStackHandler extends SimpleContainer {
    private final List<SlotChangeListener> listeners = new ArrayList<>();

    public ObservableItemStackHandler(int size) {
        super(size);
    }

    public void addListener(SlotChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack changedStack = getItem(slot);
            for (SlotChangeListener listener : listeners) {
                listener.onSlotChanged(slot, changedStack);
            }
        }
    }
}
