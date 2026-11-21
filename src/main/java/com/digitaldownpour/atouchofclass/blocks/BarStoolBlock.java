package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModBlocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BarStoolBlock extends DyeableChairBlock {
    private static final VoxelShape NORTH = Shapes.or(
            box(2, 0, 2, 3.5, 10.5, 3.5),
            box(12.5, 0, 2, 14, 10.5, 3.5),
            box(2, 0, 12, 3.5, 10.5, 13.5),
            box(12.5, 0, 12, 14, 10.5, 13.5),
            box(3.5, 3, 2.25, 12.5, 4.25, 3.25),
            box(3.5, 3, 12.25, 12.5, 4.25, 13.25),
            box(2.25, 4, 3.5, 3.25, 5, 12),
            box(12.75, 4, 3.5, 13.75, 5, 12),
            box(3.5, 9.25, 2, 12.5, 10.5, 3),
            box(3.5, 9.25, 12.5, 12.5, 10.5, 13.5),
            box(2, 9.25, 3.5, 3, 10.5, 12),
            box(13, 9.25, 3.5, 14, 10.5, 12),
            box(1.75, 10.5, 1.5, 14.25, 12, 13.75),
            // Stepped bounds follow the slightly reclined back cushion.
            box(2, 12, 11.75, 14, 14.25, 14.25),
            box(2, 14.25, 12.2, 14, 16.5, 14.75),
            box(2, 16.5, 12.7, 14, 19.1, 15.25));

    public BarStoolBlock(Properties properties, DyeColor color) {
        super(properties, NORTH, 12.0 / 16, color);
    }

    @Override
    protected BarStoolBlock variant(DyeColor dye) {
        return ModBlocks.BAR_STOOLS.get(dye).get();
    }
}
