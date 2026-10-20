package com.digitaldownpour.atouchofclass.gui;

import com.digitaldownpour.atouchofclass.network.SelectElevatorFloorPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

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
    @SuppressWarnings("FieldCanBeLocal") // Retained as screen state for the current floor.
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
            // floorIndices is ascending by floor (lowest first); render bottom-to-top so the
            // on-screen button order mirrors the physical floor stack (highest floor on top).
            int y = startY + (floorIndices.size() - 1 - i) * BUTTON_SPACING;
            addRenderableWidget(Button.builder(
                            Component.translatable("gui.atouchofclass.elevator_floor_select.floor", floorIndex + 1),
                            button -> selectFloor(floorIndex))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }
    }

    private void selectFloor(int floorIndex) {
        ClientPacketDistributor.sendToServer(new SelectElevatorFloorPayload(controlPos, floorIndex));
        onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, (height - floorIndices.size() * BUTTON_SPACING) / 2 - 20, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
