package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import net.minecraft.resources.Identifier;

public class WainscotingItem extends WallOverlayItemBase {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            ATouchofClass.MODID,
            "item/wainscoting_2"
    );

    public WainscotingItem(Properties properties) {
        super(properties, TEXTURE);
    }
}
