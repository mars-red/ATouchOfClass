package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.context.UseOnContext;

/** A wall overlay whose corner is selected from the clicked half of the face. */
public class StoneQuoinItem extends WallOverlayItemBase {
    private static final Identifier LEFT = Identifier.fromNamespaceAndPath(
            ATouchofClass.MODID, "block/stone_quoin_left");
    private static final Identifier RIGHT = Identifier.fromNamespaceAndPath(
            ATouchofClass.MODID, "block/stone_quoin_right");

    public StoneQuoinItem(Properties properties) {
        super(properties, LEFT);
        registerOverlayTexture(RIGHT);
    }

    @Override
    protected Identifier getOverlayTexture(UseOnContext context) {
        var hit = context.getClickLocation();
        var pos = context.getClickedPos();
        // Match the horizontal UV coordinate used by the overlay renderer.
        double horizontal = switch (context.getClickedFace()) {
            case NORTH -> hit.x - pos.getX();
            case SOUTH -> 1 - (hit.x - pos.getX());
            case EAST -> hit.z - pos.getZ();
            case WEST -> 1 - (hit.z - pos.getZ());
            default -> throw new IllegalArgumentException("Quoins require a side face");
        };
        return horizontal < 0.5 ? LEFT : RIGHT;
    }
}
