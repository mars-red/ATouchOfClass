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
    }
}
