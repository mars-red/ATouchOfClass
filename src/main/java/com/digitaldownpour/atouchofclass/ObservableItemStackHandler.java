package com.digitaldownpour.atouchofclass;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class ObservableItemStackHandler extends ItemStackHandler {
    private final List<SlotChangeListener> listeners = new ArrayList<>();

    public ObservableItemStackHandler(int size) {
        super(size);
    }

    public void addListener(SlotChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void onContentsChanged(int slot) {
        ItemStack changedStack = getStackInSlot(slot);
        for (SlotChangeListener listener : listeners) {
            listener.onSlotChanged(slot, changedStack);
        }
    }
}