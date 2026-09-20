package com.digitaldownpour.atouchofclass.items;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

/** A decorative block-face overlay with configurable surface placement. */
public class WallFinishItem extends BlockOverlayItemBase {
    private final Placement placement;

    public WallFinishItem(Properties properties, ResourceLocation texture, Placement placement) {
        super(properties, texture);
        this.placement = placement;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (!placement.allows(context.getClickedFace())) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    public enum Placement {
        WALLS_ONLY,
        WALLS_AND_TOP,
        ALL_FACES;

        private boolean allows(Direction face) {
            return switch (this) {
                case WALLS_ONLY -> face.getAxis().isHorizontal();
                case WALLS_AND_TOP -> face != Direction.DOWN;
                case ALL_FACES -> true;
            };
        }
    }
}
