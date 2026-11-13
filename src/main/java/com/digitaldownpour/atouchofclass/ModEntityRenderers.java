package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.client.MergedFixtureRenderer;
import com.digitaldownpour.atouchofclass.client.RefrigeratorRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class ModEntityRenderers {
    private ModEntityRenderers() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModEntities.MERGED_FIXTURE_BE.get(), context -> new MergedFixtureRenderer<>(context, false));
        event.registerBlockEntityRenderer(ModEntities.CABINET_BE.get(), context -> new MergedFixtureRenderer<>(context, true));
        event.registerBlockEntityRenderer(ModEntities.REFRIGERATOR_BE.get(), context -> new RefrigeratorRenderer());
        event.registerEntityRenderer(ModEntities.LOVE_SEAT_SEAT.get(), NoopRenderer::new);
    }
}
