package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;

/** A two-unit-tall countertop occupying the bottom of its block space. */
public class CountertopBlock extends HorizontalFurnitureBlock {
    public static final MapCodec<CountertopBlock> CODEC = simpleCodec(CountertopBlock::new);
    private static final double[][] NORTH_FACING_BOXES = {
            {0, 0, 0, 16, 2, 16}
    };

    public CountertopBlock(Properties properties) {
        super(properties, NORTH_FACING_BOXES);
    }

    @Override
    protected MapCodec<? extends CountertopBlock> codec() {
        return CODEC;
    }
}
