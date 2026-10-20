package com.digitaldownpour.atouchofclass.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

public class BetterImageButton extends Button {
    protected final WidgetSprites sprites;
    protected boolean isPressed;

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
        return active;
    }

    public void enable() {
        active = true;
    }

    public void disable() {
        active = false;
        isPressed = false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        isPressed = handled && active;
        return handled;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        isPressed = false;
        return super.mouseReleased(event);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor p_283502_, int p_281473_, int p_283021_, float p_282518_) {
        Identifier sprite = this.sprites.get(this.isActive(), this.isPressed());
        p_283502_.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height);

        if (!isEnabled()) {
            int x = getX();
            int y = getY();
            p_283502_.fill(x, y, x+width, y+height, 0xA8808080 );
        }
    }
}
