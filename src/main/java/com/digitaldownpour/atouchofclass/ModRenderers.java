package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.Map;

@EventBusSubscriber(
        modid = ATouchofClass.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.GAME
)public class ModRenderers {
    private static final ResourceLocation WALLPAPER_TEX =
            ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "block/my_wallpaper"); // <-- your texture

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Run after block geometry so overlay sits on top
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        RenderType layer = RenderType.cutoutMipped();
        VertexConsumer vc = buffer.getBuffer(layer);
        Frustum frustum = event.getFrustum();

        for (Map.Entry<ChunkPos, Map<BlockPos, WallOverlayData>> chunkEntry
                : ClientWallOverlayData.getChunks().entrySet() ) {

            for (Map.Entry<BlockPos, WallOverlayData> entry
                    : chunkEntry.getValue().entrySet()) {
                BlockPos pos = entry.getKey();
                WallOverlayData data = entry.getValue();

                if (!data.hasAny()) continue;
                if (!frustum.isVisible(new AABB(pos).inflate(0.01))) continue;

                pose.pushPose();
                pose.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
                Matrix4f mat = pose.last().pose();

                for (Direction dir : data.getFaces()) {
                    ResourceLocation tex = data.getFace(dir);
                    TextureAtlasSprite sprite = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(tex);
                    renderFaceQuad(vc, mat, dir, sprite, pos, mc);
                }

                pose.popPose();
            }
        }
        buffer.endBatch(layer);
    }

    private static void renderFaceQuad(VertexConsumer vc, Matrix4f mat, Direction dir,
                                       TextureAtlasSprite sprite, BlockPos pos, Minecraft mc) {
        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();

        // Slight offset outwards to prevent z-fighting
        float inset = 0.001f;

        int packedLight = LevelRenderer.getLightColor(
                mc.level,
                pos.relative(dir)
        );

        int overlay = OverlayTexture.NO_OVERLAY;

        switch (dir) {
            case NORTH -> {
                float z = -inset;
                vc.addVertex(mat, 1, 0, z).setUv(u1, v1).setColor(255, 255, 255, 255).setOverlay(overlay).setLight(packedLight).setNormal(0, 0, -1);
                vc.addVertex(mat, 0, 0, z).setUv(u0, v1).setColor(255, 255, 255, 255).setOverlay(overlay).setLight(packedLight).setNormal(0, 0, -1);
                vc.addVertex(mat, 0, 1, z).setUv(u0, v0).setColor(255, 255, 255, 255).setOverlay(overlay).setLight(packedLight).setNormal(0, 0, -1);
                vc.addVertex(mat, 1, 1, z).setUv(u1, v0).setColor(255, 255, 255, 255).setOverlay(overlay).setLight(packedLight).setNormal(0, 0, -1);
            }
            case SOUTH -> {
                float z = 1 + inset;
                vc.addVertex(mat, 0, 0, z).setUv(u1, v1).setColor(255, 255, 255, 255).setOverlay(overlay).setLight(packedLight).setNormal(0, 0, 1);
                vc.addVertex(mat, 1, 0, z).setUv(u0, v1).setColor(255, 255, 255, 255).setOverlay(overlay).setLight(packedLight).setNormal(0, 0, 1);
                vc.addVertex(mat, 1, 1, z).setUv(u0, v0).setColor(255, 255, 255, 255).setOverlay(overlay).setLight(packedLight).setNormal(0, 0, 1);
                vc.addVertex(mat, 0, 1, z).setUv(u1, v0).setColor(255, 255, 255, 255).setOverlay(overlay).setLight(packedLight).setNormal(0, 0, 1);
            }
            case EAST -> {
                float x = 1 + inset;

                vc.addVertex(mat, x, 0, 1)
                        .setColor(255, 255, 255, 255)
                        .setUv(u1, v1)
                        .setOverlay(overlay)
                        .setLight(packedLight)
                        .setNormal(1, 0, 0);

                vc.addVertex(mat, x, 0, 0)
                        .setColor(255, 255, 255, 255)
                        .setUv(u0, v1)
                        .setOverlay(overlay)
                        .setLight(packedLight)
                        .setNormal(1, 0, 0);

                vc.addVertex(mat, x, 1, 0)
                        .setColor(255, 255, 255, 255)
                        .setUv(u0, v0)
                        .setOverlay(overlay)
                        .setLight(packedLight)
                        .setNormal(1, 0, 0);

                vc.addVertex(mat, x, 1, 1)
                        .setColor(255, 255, 255, 255)
                        .setUv(u1, v0)
                        .setOverlay(overlay)
                        .setLight(packedLight)
                        .setNormal(1, 0, 0);
            }

            case WEST -> {
                float x = -inset;

                vc.addVertex(mat, x, 0, 0)
                        .setColor(255, 255, 255, 255)
                        .setUv(u1, v1)
                        .setOverlay(overlay)
                        .setLight(packedLight)
                        .setNormal(-1, 0, 0);

                vc.addVertex(mat, x, 0, 1)
                        .setColor(255, 255, 255, 255)
                        .setUv(u0, v1)
                        .setOverlay(overlay)
                        .setLight(packedLight)
                        .setNormal(-1, 0, 0);

                vc.addVertex(mat, x, 1, 1)
                        .setColor(255, 255, 255, 255)
                        .setUv(u0, v0)
                        .setOverlay(overlay)
                        .setLight(packedLight)
                        .setNormal(-1, 0, 0);

                vc.addVertex(mat, x, 1, 0)
                        .setColor(255, 255, 255, 255)
                        .setUv(u1, v0)
                        .setOverlay(overlay)
                        .setLight(packedLight)
                        .setNormal(-1, 0, 0);
            }        }
    }
}