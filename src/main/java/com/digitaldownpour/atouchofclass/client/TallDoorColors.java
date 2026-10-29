package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.ModBlocks;
import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class TallDoorColors {
    // Neutral gray multiplies the original grain by about 90%, without a hue shift.
    private static final int PANEL_COLOR = 0xFFE6E6E6;
    // Cabinet panels retain about 94% of the original texture brightness.
    private static final int CABINET_PANEL_COLOR = 0xFFF0F0F0;

    private TallDoorColors() {}

    @SubscribeEvent
    public static void registerColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.constant(PANEL_COLOR)),
                ModBlocks.TALL_OAK_DOOR.get(), ModBlocks.TALL_SPRUCE_DOOR.get(),
                ModBlocks.TALL_BIRCH_DOOR.get(), ModBlocks.TALL_JUNGLE_DOOR.get(),
                ModBlocks.TALL_ACACIA_DOOR.get(), ModBlocks.TALL_DARK_OAK_DOOR.get(),
                ModBlocks.TALL_MANGROVE_DOOR.get(), ModBlocks.TALL_CHERRY_DOOR.get(),
                ModBlocks.TALL_BAMBOO_DOOR.get(), ModBlocks.TALL_PALE_OAK_DOOR.get(),
                ModBlocks.TALL_CRIMSON_DOOR.get(), ModBlocks.TALL_WARPED_DOOR.get(),
                ModBlocks.TALL_POPLAR_DOOR.get());
        event.register(List.of(BlockTintSources.constant(CABINET_PANEL_COLOR)),
                ModBlocks.OAK_DOOR_CABINET.get(), ModBlocks.SPRUCE_DOOR_CABINET.get(),
                ModBlocks.BIRCH_DOOR_CABINET.get(), ModBlocks.JUNGLE_DOOR_CABINET.get(),
                ModBlocks.ACACIA_DOOR_CABINET.get(), ModBlocks.DARK_OAK_DOOR_CABINET.get(),
                ModBlocks.MANGROVE_DOOR_CABINET.get(), ModBlocks.CHERRY_DOOR_CABINET.get(),
                ModBlocks.BAMBOO_DOOR_CABINET.get(), ModBlocks.PALE_OAK_DOOR_CABINET.get(),
                ModBlocks.CRIMSON_DOOR_CABINET.get(), ModBlocks.WARPED_DOOR_CABINET.get(),
                ModBlocks.POPLAR_DOOR_CABINET.get());
    }
}
