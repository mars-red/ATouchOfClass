package com.digitaldownpour.atouchofclass.blocks;


/** A two-unit-tall countertop occupying the bottom of its block space. */
public class CountertopBlock extends HorizontalFurnitureBlock {
    private static final double[][] NORTH_FACING_BOXES = {
            {0, 0, 0, 16, 2, 16}
    };

    public CountertopBlock(Properties properties) {
        super(properties, NORTH_FACING_BOXES);
    }

}
