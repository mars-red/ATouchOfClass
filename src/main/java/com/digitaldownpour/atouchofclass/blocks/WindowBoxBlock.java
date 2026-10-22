package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Top-aligned, dirt-filled wall boxes. Only matching wood and facing join. */
public class WindowBoxBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty JOINED_LEFT = BooleanProperty.create("joined_left");
    public static final BooleanProperty JOINED_RIGHT = BooleanProperty.create("joined_right");

    public WindowBoxBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(JOINED_LEFT, false).setValue(JOINED_RIGHT, false));
    }

    private boolean joins(BlockState state, BlockState neighbor) {
        return neighbor.is(this) && neighbor.getValue(FACING) == state.getValue(FACING);
    }

    private BlockState connections(BlockState state, BlockGetter level, BlockPos pos) {
        Direction left = state.getValue(FACING).getClockWise();
        return state.setValue(JOINED_LEFT, joins(state, level.getBlockState(pos.relative(left))))
                .setValue(JOINED_RIGHT, joins(state, level.getBlockState(pos.relative(left.getOpposite()))));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        BlockState target = context.getLevel().getBlockState(pos.relative(facing.getOpposite()));
        // Clicking an end extends the trough with its existing mounting orientation.
        if (target.is(this) && facing.getAxis().isHorizontal()
                && facing.getAxis() != target.getValue(FACING).getAxis()) {
            facing = target.getValue(FACING);
        }
        if (!facing.getAxis().isHorizontal()) return null;
        return connections(defaultBlockState().setValue(FACING, facing), context.getLevel(), pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return connections(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // The selection/collision envelope stays entirely in the upper half of the block.
        return switch (state.getValue(FACING)) {
            case NORTH -> box(0, 8, 6, 16, 16, 16);
            case EAST -> box(0, 8, 0, 10, 16, 16);
            case SOUTH -> box(0, 8, 0, 16, 16, 10);
            case WEST -> box(6, 8, 0, 16, 16, 16);
            default -> throw new IllegalStateException("Window boxes must face horizontally");
        };
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .setValue(JOINED_LEFT, state.getValue(JOINED_RIGHT))
                .setValue(JOINED_RIGHT, state.getValue(JOINED_LEFT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, JOINED_LEFT, JOINED_RIGHT);
    }
}
