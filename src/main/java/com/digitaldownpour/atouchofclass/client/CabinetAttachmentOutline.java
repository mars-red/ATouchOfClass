package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class CabinetAttachmentOutline {
    private CabinetAttachmentOutline() {
    }

    @SubscribeEvent
    public static void extractOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (!(event.getLevel().getBlockEntity(event.getBlockPos()) instanceof CabinetBlockEntity cabinet)) {
            return;
        }
        var attachment = cabinet.getAttachment();
        if (attachment.isAir()) {
            return;
        }

        // The hit still belongs to the cabinet. Outline the visible attachment if it covers that hit.
        var attachmentPos = event.getBlockPos().above();
        var shape = attachment.getShape(event.getLevel(), attachmentPos, event.getCollisionContext());
        if (shape.clip(event.getCamera().position(), event.getHitResult().getLocation(), attachmentPos) == null) {
            return;
        }

        var gameRenderer = Minecraft.getInstance().gameRenderer;
        float width = gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        var lines = gameRenderer.useImprovedTransparency()
                ? RenderTypes.linesTranslucentNoDepthWrite() : RenderTypes.linesTranslucent();
        event.addCustomRenderer((state, collector, poseStack, levelState) -> {
            var camera = levelState.cameraRenderState.pos;
            poseStack.pushPose();
            poseStack.translate(attachmentPos.getX() - camera.x, attachmentPos.getY() - camera.y,
                    attachmentPos.getZ() - camera.z);
            if (state.highContrast()) {
                collector.submitShapeOutline(poseStack, shape, RenderTypes.secondaryBlockOutline(), 0xFF000000, 7, true);
                collector.submitShapeOutline(poseStack, shape, RenderTypes.linesDepthBias(), -11010079, width, true);
            } else {
                collector.submitShapeOutline(poseStack, shape, lines, ARGB.black(102), width, true);
            }
            poseStack.popPose();
            return true;
        });
    }
}
