package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;

/** A two-unit-tall countertop with a stepped oval opening biased toward its front. */
public class SinkBlock extends HorizontalFurnitureBlock {
    public static final MapCodec<SinkBlock> CODEC = simpleCodec(SinkBlock::new);

    private static final double[][] NORTH_FACING_BOXES = {
            {0, 0, 0, 16, 2, 2},
            {0, 0, 2, 6, 2, 3},
            {10, 0, 2, 16, 2, 3},
            {0, 0, 3, 4, 2, 4},
            {12, 0, 3, 16, 2, 4},
            {0, 0, 4, 3, 2, 7},
            {13, 0, 4, 16, 2, 7},
            {0, 0, 7, 4, 2, 8},
            {12, 0, 7, 16, 2, 8},
            {0, 0, 8, 6, 2, 9},
            {10, 0, 8, 16, 2, 9},
            {0, 0, 9, 16, 2, 16},

            // Solid lower layer beneath the apparent opening.
            {6, 0, 2, 10, 1, 3},
            {4, 0, 3, 12, 1, 4},
            {3, 0, 4, 13, 1, 7},
            {4, 0, 7, 12, 1, 8},
            {6, 0, 8, 10, 1, 9},

            // Compact faucet behind the opening.
            {6.5, 2, 9.25, 9.5, 2.4, 10.25},
            {7.4, 2.4, 9.55, 8.6, 5, 10.45},
            {7.4, 4.4, 7, 8.6, 5.2, 10.2},
            {7.4, 3.8, 6.8, 8.6, 5.2, 7.6}
    };

    public SinkBlock(Properties properties) {
        super(properties, NORTH_FACING_BOXES);
    }

    @Override
    protected MapCodec<? extends SinkBlock> codec() {
        return CODEC;
    }
}
