package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModBlocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PatioChairBlock extends DyeableChairBlock {
    private static final VoxelShape NORTH = Shapes.or(
            box(0, 0, 0, 2, 13, 2), box(14, 0, 0, 16, 13, 2),
            box(0, 0, 14, 2, 13, 16), box(14, 0, 14, 16, 13, 16),
            box(0, 11.5, 2, 2, 13, 14), box(14, 11.5, 2, 16, 13, 14),
            box(0, 2, 2, 2, 3.5, 14), box(14, 2, 2, 16, 3.5, 14),
            box(1, 6, 0.5, 15, 7.5, 15.5), box(2, 7.5, 0.5, 14, 10, 15),
            // Stepped collision bounds for the slightly reclined back cushion and its support.
            box(2, 10, 11.25, 14, 14, 16),
            box(2, 14, 11.75, 14, 17, 16),
            box(2, 17, 12.2, 14, 20, 16));

    public PatioChairBlock(Properties properties, DyeColor color) {
        super(properties, NORTH, 10.0 / 16, color);
    }

    @Override
    protected PatioChairBlock variant(DyeColor dye) {
        return ModBlocks.PATIO_CHAIRS.get(dye).get();
    }
}
