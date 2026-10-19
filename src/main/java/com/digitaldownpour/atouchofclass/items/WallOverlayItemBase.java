package com.digitaldownpour.atouchofclass.items;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public abstract class WallOverlayItemBase extends BlockOverlayItemBase {
    protected WallOverlayItemBase(Properties properties, Identifier overlayTexture) {
        super(properties, overlayTexture);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Wall overlays can only be placed on the four horizontal faces.
        if (context.getClickedFace().getAxis().isVertical()) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }
}
