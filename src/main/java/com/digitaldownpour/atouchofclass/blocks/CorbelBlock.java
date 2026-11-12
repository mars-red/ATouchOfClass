package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public final class CorbelBlock extends HorizontalDirectionalBlock {
    // The north-facing profile matches block/corbel, with the rear against z=16.
    // The shallow side carving stays within the stepped collision profile.
    private static final Map<Direction, VoxelShape> SHAPES =
            HorizontalFurnitureBlock.createDirectionalShapes(new double[][]{
                    {4, 0, 13, 12, 2, 16},
                    {4, 2, 12, 12, 4, 16},
                    {4, 4, 11, 12, 6, 16},
                    {4, 6, 9, 12, 8, 16},
                    {4, 8, 7, 12, 10, 16},
                    {4, 10, 5, 12, 13, 16},
                    {4, 13, 2, 12, 16, 16}
            });

    public CorbelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return face.getAxis().isHorizontal() ? defaultBlockState().setValue(FACING, face) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }
}
