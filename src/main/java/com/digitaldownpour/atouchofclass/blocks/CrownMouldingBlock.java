package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class CrownMouldingBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    private final Map<BlockState, VoxelShape> crownShapes = new HashMap<>();

    public CrownMouldingBlock(Properties properties) {
        this(properties, Direction.UP, new int[][] {{5, 0, 2}, {3, 2, 6}, {2, 6, 8}, {3, 8, 9}});
    }

    protected CrownMouldingBlock(Properties properties, Direction top, int[][] tiers) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(SHAPE, StairsShape.STRAIGHT));
        for (BlockState state : stateDefinition.getPossibleStates()) {
            Direction facing = state.getValue(FACING);
            StairsShape shape = state.getValue(SHAPE);
            Direction side = isLeft(shape) ? right(state).getOpposite() : right(state);
            VoxelShape result = Shapes.empty();
            for (int[] tier : tiers) {
                VoxelShape strip = strip(facing.getOpposite(), top, tier[0], tier[1], tier[2]);
                if (shape != StairsShape.STRAIGHT) {
                    VoxelShape returnStrip = strip(side, top, tier[0], tier[1], tier[2]);
                    strip = isInner(shape) ? Shapes.or(strip, returnStrip)
                            : Shapes.join(strip, returnStrip, BooleanOp.AND);
                }
                result = Shapes.or(result, strip);
            }
            crownShapes.put(state, result);
        }
    }

    private static VoxelShape strip(Direction back, Direction top, int depth, int start, int end) {
        double[] min = {0, 0, 0};
        double[] max = {16, 16, 16};
        int depthAxis = axis(back);
        if (back.getAxisDirection() == Direction.AxisDirection.POSITIVE) min[depthAxis] = 16 - depth;
        else max[depthAxis] = depth;
        int heightAxis = axis(top);
        if (top.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            min[heightAxis] = 16 - end;
            max[heightAxis] = 16 - start;
        } else {
            min[heightAxis] = start;
            max[heightAxis] = end;
        }
        return Block.box(min[0], min[1], min[2], max[0], max[1], max[2]);
    }

    private static int axis(Direction direction) {
        return switch (direction.getAxis()) { case X -> 0; case Y -> 1; case Z -> 2; };
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

    private boolean aligned(BlockState neighbor, BlockState state) {
        return neighbor.is(this);
    }

    private boolean canTurn(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        BlockState neighbor = level.getBlockState(pos.relative(side));
        return !aligned(neighbor, state) || neighbor.getValue(FACING) != state.getValue(FACING);
    }

    private StairsShape cornerShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction right = right(state);
        // As with stairs, a perpendicular piece beyond the back creates an outside corner;
        // one in front creates an inside return. Continuations take precedence over turns.
        BlockState behind = level.getBlockState(pos.relative(facing.getOpposite()));
        if (aligned(behind, state)) {
            Direction other = behind.getValue(FACING);
            if ((other == right || other == right.getOpposite()) && canTurn(state, level, pos, other)) {
                return other == right ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
            }
        }
        BlockState front = level.getBlockState(pos.relative(facing));
        if (aligned(front, state)) {
            Direction other = front.getValue(FACING);
            if ((other == right || other == right.getOpposite()) && canTurn(state, level, pos, other.getOpposite())) {
                return other == right ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
            }
        }
        return StairsShape.STRAIGHT;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace().getAxis().isVertical()) return null;
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.setValue(SHAPE, cornerShape(state, context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHAPE);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return crownShapes.get(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return crownShapes.get(state);
    }
}
