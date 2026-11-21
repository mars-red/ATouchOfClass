package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AdirondackChairBlock extends OutdoorChairBlock {
    private static final VoxelShape NORTH = Shapes.or(
            box(1, 0, 1, 2.5, 10, 4), box(13.5, 0, 1, 15, 10, 4),
            box(0, 10, 0, 3.5, 11.25, 13.5), box(12.5, 10, 0, 16, 11.25, 13.5),
            // Stepped bounds follow the seat and the two continuous rails down to the rear feet.
            box(1, 3.3, 2, 15, 7.45, 5), box(1, 2.35, 5, 15, 6.45, 8),
            box(1, 1.35, 8, 15, 5.5, 11),
            box(2.5, 2.9, 11, 13.5, 4.5, 12.55),
            box(1, 0, 11, 2.75, 3.45, 16), box(13.25, 0, 11, 15, 3.45, 16),
            // The reclined back leans away from the seat and narrows at the crown.
            box(2.5, 3.5, 10, 13.5, 10, 13.3),
            box(2.5, 10, 11.9, 13.5, 15.5, 14.8),
            box(4.7, 15.5, 13.4, 11.3, 17.8, 15.4),
            box(6.9, 17.8, 14, 9.1, 19.3, 15.8));

    public AdirondackChairBlock(Properties properties) {
        super(properties, NORTH, 5.47 / 16);
    }
}
