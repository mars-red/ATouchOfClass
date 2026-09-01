package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.entities.models.WallPanelEntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModEntityModels {
    public static final ModelLayerLocation WALL_PANEL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "wall_panel_entity"),
            "main"
    );

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WALL_PANEL_LAYER, WallPanelEntityModel::createBodyLayer);
    }
}
