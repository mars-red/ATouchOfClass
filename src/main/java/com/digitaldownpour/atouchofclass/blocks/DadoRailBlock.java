package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/** Horizontal wall trim, aligned to either edge of the block space. */
public class DadoRailBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    public DadoRailBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(HALF, Half.BOTTOM).setValue(SHAPE, StairsShape.STRAIGHT));
    }

    private static Direction right(BlockState state) {
        return state.getValue(FACING).getCounterClockWise();
    }

    private static boolean isLeft(StairsShape shape) {
        return shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT;
    }

    private static boolean isInner(StairsShape shape) {
        return shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT;
    }

    protected @org.jetbrains.annotations.Nullable Direction connectionFacing(BlockState state, BlockState neighbor) {
        return neighbor.is(this) && neighbor.getValue(HALF) == state.getValue(HALF) ? neighbor.getValue(FACING) : null;
    }

    private boolean canTurn(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        BlockState neighbor = level.getBlockState(pos.relative(side));
        return connectionFacing(state, neighbor) != state.getValue(FACING);
    }

    private StairsShape cornerShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction right = right(state);
        // As with stairs, a perpendicular piece beyond the back creates an outside corner;
        // one in front creates an inside return. Continuations take precedence over turns.
        BlockState behind = level.getBlockState(pos.relative(facing.getOpposite()));
        Direction other = connectionFacing(state, behind);
        if (other != null) {
            if ((other == right || other == right.getOpposite()) && canTurn(state, level, pos, other)) {
                return other == right ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
            }
        }
        BlockState front = level.getBlockState(pos.relative(facing));
        other = connectionFacing(state, front);
        // A vertical casing on the far edge of the block in front can receive
        // an inside return. Its near edge needs no return across that block.
        if (other == null && front.getBlock().getClass() == CasingBlock.class) {
            Direction casingFacing = front.getValue(CasingBlock.FACING);
            CasingBlock.Edge edge = front.getValue(CasingBlock.EDGE);
            if (casingFacing.getAxis().isHorizontal() && casingFacing.getAxis() != facing.getAxis()
                    && (edge == CasingBlock.Edge.LEFT || edge == CasingBlock.Edge.RIGHT)
                    && CasingBlock.edgeDirection(casingFacing, edge) == facing) {
                other = casingFacing;
            }
        }
        if (other != null) {
            if ((other == right || other == right.getOpposite()) && canTurn(state, level, pos, other.getOpposite())) {
                return other == right ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
            }
        }
        return StairsShape.STRAIGHT;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace().getAxis().isVertical()) return null;
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace())
                .setValue(HALF, context.getClickLocation().y - context.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
        return state.setValue(SHAPE, cornerShape(state, context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SHAPE);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, net.minecraft.util.RandomSource random) {
        return state.setValue(SHAPE, cornerShape(state, level, pos));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState result = state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
        StairsShape shape = state.getValue(SHAPE);
        if (shape == StairsShape.STRAIGHT) return result;
        Direction side = isLeft(shape) ? right(state).getOpposite() : right(state);
        boolean left = mirror.mirror(side) == right(result).getOpposite();
        return result.setValue(SHAPE, isInner(shape)
                ? (left ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT)
                : (left ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        double y = state.getValue(HALF) == Half.TOP ? 12 : 0;
        VoxelShape result = Shapes.empty();
        double[][] tiers = {{0, .5, 1.5}, {.5, 1, 2}, {1, 3, 1.5}, {3, 3.5, 2}, {3.5, 4, 1.5}};
        for (double[] tier : tiers) {
            double d = tier[2];
            VoxelShape strip = switch (state.getValue(FACING)) {
                case SOUTH -> Block.box(0, y+tier[0], 0, 16, y+tier[1], d);
                case NORTH -> Block.box(0, y+tier[0], 16-d, 16, y+tier[1], 16);
                case EAST -> Block.box(0, y+tier[0], 0, d, y+tier[1], 16);
                default -> Block.box(16-d, y+tier[0], 0, 16, y+tier[1], 16);
            };
            StairsShape shape = state.getValue(SHAPE);
            if (shape != StairsShape.STRAIGHT) {
                Direction side = isLeft(shape) ? right(state).getOpposite() : right(state);
                VoxelShape turn = switch (side) {
                    case NORTH -> Block.box(0,y+tier[0],0,16,y+tier[1],d);
                    case SOUTH -> Block.box(0,y+tier[0],16-d,16,y+tier[1],16);
                    case WEST -> Block.box(0,y+tier[0],0,d,y+tier[1],16);
                    default -> Block.box(16-d,y+tier[0],0,16,y+tier[1],16);
                };
                strip = isInner(shape) ? Shapes.or(strip, turn) : Shapes.join(strip, turn, BooleanOp.AND);
            }
            result = Shapes.or(result, strip);
        }
        return result;
    }
}
