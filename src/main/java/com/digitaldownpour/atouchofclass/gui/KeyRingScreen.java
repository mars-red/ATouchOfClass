package com.digitaldownpour.atouchofclass.gui;

import com.digitaldownpour.atouchofclass.KeyRingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class KeyRingScreen extends AbstractContainerScreen<KeyRingMenu> {
    public KeyRingScreen(KeyRingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 272, 114);
        inventoryLabelX = 104;
        inventoryLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF373737);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFFFFFFFF);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, 0xFFC6C6C6);
        for (var slot : menu.slots) {
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFFFFFFFF);
            graphics.fill(x - 1, y - 1, x + 16, y + 16, 0xFF373737);
            graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
        }
        graphics.text(font, Component.translatable("container.atouchofclass.key_ring.hint"),
                leftPos + 8, topPos + 100, 0xFF404040, false);
    }

}
