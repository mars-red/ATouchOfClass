package com.digitaldownpour.atouchofclass.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BetterImageButton extends Button {
    protected final WidgetSprites sprites;
    protected boolean isPressed;
    protected boolean isEnabled = true;

    public BetterImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress) {
        this(x, y, width, height, sprites, onPress, CommonComponents.EMPTY);
    }

    public BetterImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, Component message) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.sprites = sprites;
    }

    public BetterImageButton(int width, int height, WidgetSprites sprites, Button.OnPress onPress, Component message) {
        this(0, 0, width, height, sprites, onPress, message);
    }

    public boolean isPressed() {
        return visible && isPressed;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isEnabled()) {
            isPressed = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isPressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(GuiGraphics p_283502_, int p_281473_, int p_283021_, float p_282518_) {
        ResourceLocation resourcelocation = this.sprites.get(this.isActive(), this.isPressed());
        p_283502_.blitSprite(RenderType::guiTextured, resourcelocation, this.getX(), this.getY(), this.width, this.height);

        if (!isEnabled()) {
            int x = getX();
            int y = getY();
            p_283502_.fill(x, y, x+width, y+height, 0xA8808080 );
        }
    }
}
