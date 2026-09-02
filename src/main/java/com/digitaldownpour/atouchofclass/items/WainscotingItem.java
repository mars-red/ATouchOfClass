package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import net.minecraft.resources.ResourceLocation;

public class WainscotingItem extends WallOverlayItemBase {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ATouchofClass.MODID,
            "item/wainscoting_1"
    );

    public WainscotingItem(Properties properties) {
        super(properties, TEXTURE);
    }
}
