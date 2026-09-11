package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.world.level.block.Block;

/**
 * A player-placed floor tile. Connected floor blocks (via 4-directional
 * flood fill) form a cab's or call point's footprint of arbitrary shape.
 * Whether a given footprint touches exactly one shaft column, and which
 * derived floor index it represents, is always recomputed on demand (see
 * ElevatorUtil) rather than stored here - placing or removing a floor block
 * therefore never requires any invalidation bookkeeping.
 */
public class ElevatorFloorBlock extends Block {
    public ElevatorFloorBlock(Properties properties) {
        super(properties);
    }
}
