package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.client.flame.FlameLoop;
import com.digitaldownpour.atouchofclass.client.flame.FlamePalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/** Lazily bakes the shared flame animation into one vertically stacked texture strip. */
public final class FirePitFlames {
    public static final int FRAME_WIDTH = 16;
    public static final int FRAME_HEIGHT = 8;
    public static final int FRAME_COUNT = 32;
    private static final int BLEND_FRAMES = 8;
    private static final long SEED = 0x5EED_F17EL;
    private static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "dynamic/fire_pit_flames");

    private static RenderType renderType;

    private FirePitFlames() {}

    public static RenderType renderType() {
        if (renderType == null) {
            bake();
            renderType = RenderTypes.eyes(TEXTURE_ID);
        }
        return renderType;
    }

    private static void bake() {
        FlameLoop loop = FlameLoop.bake(FRAME_WIDTH, FRAME_HEIGHT, FRAME_COUNT, BLEND_FRAMES, SEED);
        DynamicTexture texture = new DynamicTexture(TEXTURE_ID::toString, FRAME_WIDTH, FRAME_HEIGHT * FRAME_COUNT, true);
        var pixels = texture.getPixels();
        for (int f = 0; f < FRAME_COUNT; f++) {
            for (int y = 0; y < FRAME_HEIGHT; y++) {
                for (int x = 0; x < FRAME_WIDTH; x++) {
                    pixels.setPixel(x, f * FRAME_HEIGHT + y, FlamePalette.argb(loop.heat(f, x, y)));
                }
            }
        }
        texture.upload();
        Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, texture);
    }
}
