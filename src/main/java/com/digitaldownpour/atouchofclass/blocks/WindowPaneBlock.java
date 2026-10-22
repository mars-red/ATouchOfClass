package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The same framed glazing as a transom, centered in the block and one unit deep. */
public class WindowPaneBlock extends TransomBlock {
    private static final VoxelShape NORTH_SOUTH = box(0, 0, 7.5, 16, 16, 8.5);
    private static final VoxelShape EAST_WEST = box(7.5, 0, 0, 8.5, 16, 16);

    public WindowPaneBlock(Properties properties) { super(properties); }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
    }
}
