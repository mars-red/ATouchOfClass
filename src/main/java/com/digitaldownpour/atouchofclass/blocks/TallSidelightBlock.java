package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TallSidelightBlock extends Block {
    public static final VoxelShape Z_COLLISION_SHAPE = Block.box((double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 16.0F, (double) 16.0F, (double) 4.0F);
    public static final VoxelShape X_COLLISION_SHAPE = Block.box((double) 0.0F, (double) 0.0F, (double) 0.0F, (double) 4.0F, (double) 16.0F, (double) 16.0F);

    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2); // 0 = bottom, 1 = middle, 2 = top
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TallSidelightBlock(Properties blockProps) {
        super(blockProps);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, 0)
                .setValue(FACING, net.minecraft.core.Direction.NORTH)
        );
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ((Direction)state.getValue(FACING)).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();

        return super.getStateForPlacement(context)
                .setValue(FACING, facing)
                .setValue(PART, 0);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isClientSide && state.getValue(PART) == 0) {
            Direction facing = state.getValue(FACING);
            // Place middle part above
            world.setBlock(
                    pos.above(),
                    state.setValue(PART, 1)
                            .setValue(FACING, state.getValue(FACING)),
                    3);
            // Place top part above middle
            world.setBlock(
                    pos.above(2),
                    state.setValue(PART, 2)
                            .setValue(FACING, state.getValue(FACING)),
                    3);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!world.isClientSide && state.getBlock() != newState.getBlock()) {
            int part = state.getValue(PART);
            BlockPos basePos = pos.below(part); // Calculate bottom position

            for (int i = 0; i < 3; i++) {
                BlockPos p = basePos.above(i);
                if (world.getBlockState(p).getBlock() == this) {
                    world.destroyBlock(p, false);
                }
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int part = state.getValue(PART);
        if (part == 0) {
            return level.getBlockState(pos.below()).isSolid();
        } else {
            BlockState belowState = level.getBlockState(pos.below());
            return belowState.getBlock() instanceof TallDoorBlock
                    && belowState.getValue(PART) == part - 1;
        }
    }
}
