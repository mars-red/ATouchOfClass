package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import com.digitaldownpour.atouchofclass.client.OverlayLightingRenderer;
import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.Map;

@EventBusSubscriber(
        modid = ATouchofClass.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.GAME
)public class ModRenderers {
    private static final OverlayLightingRenderer OVERLAYS = new OverlayLightingRenderer();

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
                OVERLAYS.render(mc, pos, data, pose, vc);

                pose.popPose();
            }
        }
        buffer.endBatch(layer);
    }

}
