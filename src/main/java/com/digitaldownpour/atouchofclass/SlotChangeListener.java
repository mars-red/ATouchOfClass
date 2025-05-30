package com.digitaldownpour.atouchofclass;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface SlotChangeListener {
    void onSlotChanged(int slot, ItemStack stack);
}
