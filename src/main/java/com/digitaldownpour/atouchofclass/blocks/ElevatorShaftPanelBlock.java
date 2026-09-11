package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.world.level.block.Block;

/**
 * Tiles vertically to form an elevator shaft. Shaft membership and floor
 * order are never stored on the block itself: they are recomputed on demand
 * by scanning contiguous panel blocks (see ElevatorUtil). This block is
 * intentionally a plain full block with no state or behavior of its own -
 * a shaft is simply "a straight vertical stack of these", nothing more.
 */
public class ElevatorShaftPanelBlock extends Block {
    public ElevatorShaftPanelBlock(Properties properties) {
        super(properties);
    }
}
