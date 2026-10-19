package com.digitaldownpour.atouchofclass.gui;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.KeyCutterMenu;
import com.digitaldownpour.atouchofclass.network.SyncKeyCutterMenuPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class KeyCutterMenuScreen extends AbstractContainerScreen<KeyCutterMenu> {
    private static final Identifier BACKGROUND_LOCATION = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "textures/gui/container/key_cutter_background.png");
    private EditBox keyCodeInput;
    private EditBox keyableNameInput;
    private BetterImageButton setCodeButton;

    public KeyCutterMenuScreen(KeyCutterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 191, 172);

        menu.addListener((slot, stack)->{
            if (menu.areSlotsKeyable() && menu.isOutputEmpty()) {
                setCodeButton.enable();
            }
            else {
                setCodeButton.disable();
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        keyCodeInput = new EditBox(
                font,
                leftPos + 60,
                topPos + 15,
                70,
                16,
                Component.literal("key code")
        );

        keyCodeInput.setMaxLength(10);
        keyCodeInput.setBordered(true);
        keyCodeInput.setVisible(true);
        keyCodeInput.setTextColor(0xFFFFFFFF);
        keyCodeInput.setValue("");

        addRenderableWidget(keyCodeInput);

        keyableNameInput = new EditBox(
                font,
                leftPos + 60,
                topPos + 32,
                100,
                16,
                Component.literal("item name")
        );

        addRenderableWidget(keyableNameInput);

        setCodeButton = new BetterImageButton(
                leftPos + 82,
                topPos + 52,
                16,
                16,
                new WidgetSprites(
                        Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "key_cutter_button_sprite"),
                        Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "key_cutter_button_sprite_pressed")
                ),
                button -> craft(),
                Component.literal("set key code")
        );

        setCodeButton.disable();
        addRenderableWidget(setCodeButton);
    }

    private void craft() {
        ClientPacketDistributor.sendToServer(new SyncKeyCutterMenuPayload(keyCodeInput.getValue(), keyableNameInput.getValue()));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);

        graphics.text(this.font, "Key code:", keyCodeInput.getX() - leftPos - 50, keyCodeInput.getY() - topPos + 5, 0xFFF0F0F0);
        graphics.text(this.font, "Name:", keyableNameInput.getX() - leftPos - 28, keyableNameInput.getY() - topPos + 5, 0xFFF0F0F0);
    }


    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        super.extractSlot(graphics, slot, mouseX, mouseY);
        // Draw in the same translated coordinate space as the slot contents.
        // submitOutline defers drawing and does not retain this translation.
        int x = slot.x - 1;
        int y = slot.y - 1;
        int color = 0xFF303030;
        graphics.fill(x, y, x + 19, y + 1, color);
        graphics.fill(x, y + 18, x + 19, y + 19, color);
        graphics.fill(x, y + 1, x + 1, y + 18, color);
        graphics.fill(x + 18, y + 1, x + 19, y + 18, color);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BACKGROUND_LOCATION,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight,
                256, 256
        );
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            return super.keyPressed(event);
        }

        if (keyCodeInput.isFocused()) {
            return keyPressedHelper(keyCodeInput, event);
        }

        if (keyableNameInput.isFocused()) {
            return keyPressedHelper(keyableNameInput, event);
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (charTypedHelper(keyCodeInput, event)) {
            return true;
        }

        if (charTypedHelper(keyableNameInput, event)) {
            return true;
        }

        return super.charTyped(event);
    }

    private boolean charTypedHelper(EditBox input, CharacterEvent event) {
        return input.isFocused() && input.charTyped(event);
    }

    private boolean keyPressedHelper(EditBox input, KeyEvent event) {
        // Allow text input including inventory key
        return input.keyPressed(event)
                || input.canConsumeInput()
                || super.keyPressed(event);
    }

}
