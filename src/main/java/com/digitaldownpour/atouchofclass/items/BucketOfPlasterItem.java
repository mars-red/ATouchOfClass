package com.digitaldownpour.atouchofclass.items;

import net.minecraft.resources.ResourceLocation;

public class BucketOfPlasterItem extends BlockOverlayItemBase {
    private static final ResourceLocation PLASTER_OVERLAY = ResourceLocation.fromNamespaceAndPath(
            "minecraft",
            "block/white_concrete"
    );

    public BucketOfPlasterItem(Properties properties) {
        super(properties, PLASTER_OVERLAY);
    }
}
