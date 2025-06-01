package com.digitaldownpour.atouchofclass.gui;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.KeyCutterMenu;
import com.digitaldownpour.atouchofclass.network.SyncKeyCutterMenuPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

public class KeyCutterMenuScreen extends AbstractContainerScreen<KeyCutterMenu> {
    private static final ResourceLocation BACKGROUND_LOCATION = ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "textures/gui/container/key_cutter_background.png");
    private EditBox keyCodeInput;
    private EditBox keyableNameInput;
    private BetterImageButton setCodeButton;

    public KeyCutterMenuScreen(KeyCutterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        imageWidth = 191;
        imageHeight = 172;

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
        keyCodeInput.setTextColor(0xFFFFFF);
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
                        ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "key_cutter_button_sprite"),
                        ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "key_cutter_button_sprite_pressed")
                ),
                new Button.OnPress() {
                    @Override
                    public void onPress(Button button) {
                        craft();
                    }
                },
                Component.literal("set key code")
        );

        setCodeButton.disable();
        addRenderableWidget(setCodeButton);
    }

    private void craft() {
        PacketDistributor.sendToServer(new SyncKeyCutterMenuPayload(keyCodeInput.getValue(), keyableNameInput.getValue()));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(this.font, "Key code:", keyCodeInput.getX() - leftPos - 50, keyCodeInput.getY() - topPos + 5, 0xFFF0F0F0);
        guiGraphics.drawString(this.font, "Name:", keyableNameInput.getX() - leftPos - 28, keyableNameInput.getY() - topPos + 5, 0xFFF0F0F0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        super.renderSlot(guiGraphics, slot);
        guiGraphics.renderOutline(slot.x - 1, slot.y - 1, 19, 19, 0xFF303030);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(
                RenderType::guiTextured,
                BACKGROUND_LOCATION,
                this.leftPos, this.topPos,
                0, 0,
                this.imageWidth, this.imageHeight,
                256, 256
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCodeInput.isFocused()) {
            return keyPressedHelper(keyCodeInput, keyCode, scanCode, modifiers);
        }

        if (keyableNameInput.isFocused()) {
            return keyPressedHelper(keyableNameInput, keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (charTypedHelper(keyCodeInput, codePoint, modifiers)) {
            return true;
        }

        if (charTypedHelper(keyableNameInput, codePoint, modifiers)) {
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    private boolean charTypedHelper(EditBox input, char codePoint, int modifiers) {
        return input.isFocused() && input.charTyped(codePoint, modifiers);
    }

    private boolean keyPressedHelper(EditBox input, int keyCode, int scanCode, int modifiers) {
        // Allow text input including inventory key
        return input.keyPressed(keyCode, scanCode, modifiers)
                || input.canConsumeInput()
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

}
