package com.digitaldownpour.atouchofclass.gui;

import com.digitaldownpour.atouchofclass.network.SelectElevatorFloorPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Plain client-only destination-control screen: not an AbstractContainerMenu
 * since there is no inventory/slot state to synchronize, just a list of
 * floor indices the server has already validated and derived. Selecting a
 * floor sends it back to the server, which re-validates everything fresh
 * before acting (see ElevatorFlow#handleFloorSelected) - this screen never
 * has to be trusted to only show valid choices.
 */
public class ElevatorFloorSelectScreen extends Screen {
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    private final BlockPos controlPos;
    private final List<Integer> floorIndices;
    private final int currentFloorIndex;

    public ElevatorFloorSelectScreen(BlockPos controlPos, List<Integer> floorIndices, int currentFloorIndex) {
        super(Component.translatable("gui.atouchofclass.elevator_floor_select.title"));
        this.controlPos = controlPos;
        this.floorIndices = floorIndices;
        this.currentFloorIndex = currentFloorIndex;
    }

    @Override
    protected void init() {
        super.init();
        int totalHeight = floorIndices.size() * BUTTON_SPACING;
        int startY = (height - totalHeight) / 2;
        int x = (width - BUTTON_WIDTH) / 2;
        for (int i = 0; i < floorIndices.size(); i++) {
            int floorIndex = floorIndices.get(i);
            int y = startY + i * BUTTON_SPACING;
            addRenderableWidget(Button.builder(
                            Component.translatable("gui.atouchofclass.elevator_floor_select.floor", floorIndex),
                            button -> selectFloor(floorIndex))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }
    }

    private void selectFloor(int floorIndex) {
        PacketDistributor.sendToServer(new SelectElevatorFloorPayload(controlPos, floorIndex));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, (height - floorIndices.size() * BUTTON_SPACING) / 2 - 20, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
