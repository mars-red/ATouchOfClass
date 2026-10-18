package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import com.digitaldownpour.atouchofclass.client.OverlayLightingRenderer;
import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;

@EventBusSubscriber(
        modid = ATouchofClass.MODID,
        value = Dist.CLIENT
)public class ModRenderers {
    private static final ContextKey<Frustum> OVERLAY_FRUSTUM =
            new ContextKey<>(ResourceLocation.fromNamespaceAndPath(
                    ATouchofClass.MODID, "overlay_frustum"));

    @SubscribeEvent
    public static void extractRenderState(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(OVERLAY_FRUSTUM, new Frustum(event.getFrustum()));
    }

    private static final OverlayLightingRenderer OVERLAYS = new OverlayLightingRenderer();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        // Run after block geometry so overlay sits on top

        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = event.getLevelRenderState().cameraRenderState.pos;

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        RenderType layer = RenderType.cutoutMipped();
        VertexConsumer vc = buffer.getBuffer(layer);
        Frustum frustum = event.getLevelRenderState().getRenderData(OVERLAY_FRUSTUM);

        for (Map.Entry<ChunkPos, Map<BlockPos, WallOverlayData>> chunkEntry
                : ClientWallOverlayData.getChunks().entrySet() ) {

            for (Map.Entry<BlockPos, WallOverlayData> entry
                    : chunkEntry.getValue().entrySet()) {
                BlockPos pos = entry.getKey();
                WallOverlayData data = entry.getValue();

                if (!data.hasAny()) continue;
                if (frustum != null && !frustum.isVisible(new AABB(pos).inflate(0.01))) continue;

                pose.pushPose();
                pose.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
                OVERLAYS.render(mc, pos, data, pose, vc);

                pose.popPose();
            }
        }
        buffer.endBatch(layer);
    }

}
