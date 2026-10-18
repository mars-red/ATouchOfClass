package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;

/** Bottom-aligned millwork using the same corner connections as crown moulding. */
public class BaseboardBlock extends CrownMouldingBlock {
    public BaseboardBlock(Properties properties) {
        super(properties, Direction.DOWN, new int[][] {{2, 0, 4}});
    }

    /** Whether the straight strip or corner return follows this wall face. */
    public static boolean followsFace(BlockState state, Direction facing) {
        Direction primary = state.getValue(FACING);
        if (primary == facing) return true;
        StairsShape shape = state.getValue(SHAPE);
        return switch (shape) {
            case INNER_LEFT, OUTER_LEFT -> primary.getCounterClockWise() == facing;
            case INNER_RIGHT, OUTER_RIGHT -> primary.getClockWise() == facing;
            default -> false;
        };
    }

    @Override
    protected @org.jetbrains.annotations.Nullable Direction connectionFacing(BlockState neighbor) {
        if ((neighbor.getBlock() instanceof PlinthBlock
                && (neighbor.getValue(CasingBlock.EDGE) == CasingBlock.Edge.LEFT
                || neighbor.getValue(CasingBlock.EDGE) == CasingBlock.Edge.RIGHT))
                || (neighbor.getBlock() instanceof WindowApronBlock && neighbor.getValue(WindowApronBlock.BASEBOARD))) {
            Direction facing = neighbor.getValue(CasingBlock.FACING);
            return facing.getAxis().isHorizontal() ? facing : null;
        }
        return super.connectionFacing(neighbor);
    }
}
