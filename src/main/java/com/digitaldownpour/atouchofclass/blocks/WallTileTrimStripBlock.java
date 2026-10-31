package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

/** Straight metal trim, half a model unit deep and one unit wide. */
public class WallTileTrimStripBlock extends CasingBlock {
    public WallTileTrimStripBlock(Properties properties) {
        super(properties, 0.5, 1);
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return context.getClickedFace().getAxis().isHorizontal() ? super.getStateForPlacement(context) : null;
    }
}
