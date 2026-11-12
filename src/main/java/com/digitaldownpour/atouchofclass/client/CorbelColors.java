package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.ModBlocks;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class CorbelColors {
    private CorbelColors() {}

    @SubscribeEvent
    public static void registerColors(RegisterColorHandlersEvent.BlockTintSources event) {
        // Tint index 0 matches the inventory model and reduces only the carved surfaces to 87% brightness.
        var carving = List.of(BlockTintSources.constant(0xFFDEDEDE));
        ModBlocks.WOOD_CORBELS.values().forEach(block -> event.register(carving, block.get()));
        event.register(carving, ModBlocks.PLASTER_CORBEL.get());
    }
}
