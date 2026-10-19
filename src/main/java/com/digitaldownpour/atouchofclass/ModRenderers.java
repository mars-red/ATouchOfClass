package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.client.OverlayLightingRenderer;
import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class ModRenderers {
    private static final ContextKey<List<OverlayBlock>> OVERLAY_BLOCKS = new ContextKey<>(
            Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "overlay_blocks"));
    private static final OverlayLightingRenderer OVERLAYS = new OverlayLightingRenderer();

    private record OverlayBlock(BlockPos pos, List<OverlayLightingRenderer.RenderFace> faces) {}

    @SubscribeEvent
    public static void extractRenderState(ExtractLevelRenderStateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        List<OverlayBlock> blocks = new ArrayList<>();
        for (var chunk : ClientWallOverlayData.getChunks().values()) {
            for (var entry : chunk.entrySet()) {
                BlockPos pos = entry.getKey();
                if (!entry.getValue().hasAny() || !event.getFrustum().isVisible(new AABB(pos).inflate(0.01))) continue;
                var faces = OVERLAYS.extract(mc, pos, entry.getValue());
                if (!faces.isEmpty()) blocks.add(new OverlayBlock(pos.immutable(), faces));
            }
        }
        event.getRenderState().setRenderData(OVERLAY_BLOCKS, List.copyOf(blocks));
    }

    @SubscribeEvent
    public static void submitGeometry(SubmitCustomGeometryEvent event) {
        var blocks = event.getLevelRenderState().getRenderData(OVERLAY_BLOCKS);
        if (blocks == null || blocks.isEmpty()) return;
        var camera = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack pose = event.getPoseStack();
        for (OverlayBlock block : blocks) {
            pose.pushPose();
            try {
                pose.translate(block.pos().getX() - camera.x, block.pos().getY() - camera.y,
                        block.pos().getZ() - camera.z);
                event.getSubmitNodeCollector().submitCustomGeometry(pose, RenderTypes.cutoutMovingBlock(),
                        (transform, consumer) -> {
                            for (var face : block.faces()) face.draw(transform.pose(), consumer);
                        });
            } finally {
                pose.popPose();
            }
        }
    }
}
