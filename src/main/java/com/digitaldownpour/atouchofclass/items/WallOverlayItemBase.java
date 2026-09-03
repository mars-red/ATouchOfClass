package com.digitaldownpour.atouchofclass.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public abstract class WallOverlayItemBase extends BlockOverlayItemBase {
    protected WallOverlayItemBase(Properties properties, ResourceLocation overlayTexture) {
        super(properties, overlayTexture);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        // Wall overlays can only be placed on the four horizontal faces.
        if (context.getClickedFace().getAxis().isVertical()) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }
}
