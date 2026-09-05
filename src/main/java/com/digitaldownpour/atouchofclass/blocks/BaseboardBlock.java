package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.Direction;

/** Bottom-aligned millwork using the same corner connections as crown moulding. */
public class BaseboardBlock extends CrownMouldingBlock {
    public BaseboardBlock(Properties properties) {
        super(properties, Direction.DOWN, new int[][] {{2, 0, 4}});
    }
}
