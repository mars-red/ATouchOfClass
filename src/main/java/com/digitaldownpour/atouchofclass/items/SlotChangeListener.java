package com.digitaldownpour.atouchofclass.items;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface SlotChangeListener {
    void onSlotChanged(int slot, ItemStack stack);
}
