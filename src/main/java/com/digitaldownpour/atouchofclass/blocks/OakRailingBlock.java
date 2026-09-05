package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class OakRailingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<OakRailingBlock> CODEC = simpleCodec(OakRailingBlock::new);
    public enum Slope implements StringRepresentable {
        FLAT, UPPER_LEFT, LOWER_RIGHT, LOWER_LEFT, UPPER_RIGHT, TRANSITION_LEFT, TRANSITION_RIGHT;

        @Override
        public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }

        public Slope reversed() {
            return switch (this) {
                case UPPER_LEFT -> UPPER_RIGHT; case UPPER_RIGHT -> UPPER_LEFT;
                case LOWER_LEFT -> LOWER_RIGHT; case LOWER_RIGHT -> LOWER_LEFT;
                case TRANSITION_LEFT -> TRANSITION_RIGHT; case TRANSITION_RIGHT -> TRANSITION_LEFT;
                default -> FLAT;
            };
        }
    }
    public static final EnumProperty<Slope> SLOPE = EnumProperty.create("slope", Slope.class);
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    private static final Map<Slope, Map<Direction, VoxelShape>> SLOPED_SHAPES = new EnumMap<>(Slope.class);

    static {
        // North-facing: wall at z=16. Rail spans x=0..16 and y=12..16.
        // Stand-off centers repeat every 8 units, including across block boundaries.
        // Plate front z=15.25 minus rail back z=14.25 leaves a 1-unit gap.
        double[][] boxes = {
                {0, 12.5, 11.25, 16, 15.5, 14.25},
                {0, 15.5, 11.75, 16, 16, 13.75},
                {0, 12, 11.75, 16, 12.5, 13.75},
                {3, 10, 15.25, 5, 13, 16}, {11, 10, 15.25, 13, 13, 16},
                {3.5, 10.75, 12.75, 4.5, 11.75, 15.25},
                {11.5, 10.75, 12.75, 12.5, 11.75, 15.25},
                {3.5, 11.75, 12.75, 4.5, 12, 13.75},
                {11.5, 11.75, 12.75, 12.5, 12, 13.75}
        };
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape shape = Shapes.empty();
            int turns = switch (direction) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
            for (double[] box : boxes) {
                double x0 = box[0], z0 = box[2], x1 = box[3], z1 = box[5];
                for (int i = 0; i < turns; i++) {
                    double nextX0 = 16 - z1, nextX1 = 16 - z0;
                    z0 = x0; z1 = x1; x0 = nextX0; x1 = nextX1;
                }
                shape = Shapes.or(shape, Block.box(x0, box[1], z0, x1, box[4], z1));
            }
            SHAPES.put(direction, shape);
        }
        for (Slope slope : Slope.values()) {
            if (slope == Slope.FLAT) continue;
            Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                VoxelShape shape = Shapes.empty();
                for (double[] box : OakRailingGeometry.boxes(slope)) {
                    double x0 = box[0], x1 = box[3], z0 = box[2], z1 = box[5];

                    int turns = switch (facing) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
                    for (int i = 0; i < turns; i++) {
                        double nextX0 = 16 - z1, nextX1 = 16 - z0;
                        z0 = x0; z1 = x1; x0 = nextX0; x1 = nextX1;
                    }
                    shape = Shapes.joinUnoptimized(shape, Block.box(x0, box[1], z0, x1, box[4], z1),
                            net.minecraft.world.phys.shapes.BooleanOp.OR);
                }
                shapes.put(facing, shape.optimize());
            }
            SLOPED_SHAPES.put(slope, shapes);
        }
    }

    public OakRailingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SLOPE, Slope.FLAT));
    }

    @Override
    protected MapCodec<? extends OakRailingBlock> codec() { return CODEC; }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                BlockState state = defaultBlockState().setValue(FACING, direction.getOpposite());
                if (state.canSurvive(context.getLevel(), context.getClickedPos()))
                    return withSlope(state, context.getLevel(), context.getClickedPos());
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState() : withSlope(state, level, pos);
    }

    private Slope corner(BlockGetter level, BlockPos pos, Direction facing) {
        Direction right = facing.getClockWise();
        boolean left = matches(level, pos.relative(right.getOpposite()), facing);
        boolean r = matches(level, pos.relative(right), facing);
        boolean up = matches(level, pos.above(), facing);
        boolean down = matches(level, pos.below(), facing);
        // Only unambiguous two-edge bends form a slope. Straight runs stay flat.
        if (left == r || up == down) return Slope.FLAT;
        return up ? (left ? Slope.UPPER_LEFT : Slope.UPPER_RIGHT)
                : (left ? Slope.LOWER_LEFT : Slope.LOWER_RIGHT);
    }

    private Slope baseSlope(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING), right = facing.getClockWise();
        Slope slope = corner(level, pos, facing);
        if (slope == Slope.FLAT) {
            // A run endpoint inherits the complementary half of an adjacent bend.
            // Inspect physical connections, not the neighbor's state, to avoid feedback loops.
            Direction only = null;
            int count = 0;
            for (Direction d : new Direction[] {right, right.getOpposite(), Direction.UP, Direction.DOWN}) {
                if (matches(level, pos.relative(d), facing)) { only = d; count++; }
            }
            if (count == 1) {
                Slope other = corner(level, pos.relative(only), facing);
                if (only == right && other == Slope.UPPER_LEFT || only == Direction.DOWN && other == Slope.UPPER_LEFT)
                    slope = Slope.LOWER_RIGHT;
                else if (only == right.getOpposite() && other == Slope.LOWER_RIGHT || only == Direction.UP && other == Slope.LOWER_RIGHT)
                    slope = Slope.UPPER_LEFT;
                else if (only == right && other == Slope.LOWER_LEFT || only == Direction.UP && other == Slope.LOWER_LEFT)
                    slope = Slope.UPPER_RIGHT;
                else if (only == right.getOpposite() && other == Slope.UPPER_RIGHT || only == Direction.DOWN && other == Slope.UPPER_RIGHT)
                    slope = Slope.LOWER_LEFT;
            }
        }
        return slope;
    }

    private BlockState withSlope(BlockState state, BlockGetter level, BlockPos pos) {
        Slope slope = baseSlope(state, level, pos);
        Direction facing = state.getValue(FACING), right = facing.getClockWise();
        BlockPos leftPos = pos.relative(right.getOpposite()), rightPos = pos.relative(right);
        if (slope == Slope.FLAT && matches(level, leftPos, facing) && matches(level, rightPos, facing)
                && !matches(level, pos.above(), facing) && !matches(level, pos.below(), facing)) {
            // Derive connections from physical neighbors, not transition states, so
            // transitions cannot spread along an otherwise horizontal run.
            Slope left = baseSlope(level.getBlockState(leftPos), level, leftPos);
            Slope r = baseSlope(level.getBlockState(rightPos), level, rightPos);
            if ((left == Slope.UPPER_RIGHT || left == Slope.LOWER_RIGHT) && r == Slope.FLAT)
                slope = Slope.TRANSITION_LEFT;
            else if ((r == Slope.UPPER_LEFT || r == Slope.LOWER_LEFT) && left == Slope.FLAT)
                slope = Slope.TRANSITION_RIGHT;
        }
        return state.setValue(SLOPE, slope);
    }
    private boolean matches(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState other = level.getBlockState(pos);
        return other.is(this) && other.getValue(FACING) == facing;
    }

    private void refresh(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            BlockState updated = withSlope(state, level, pos);
            if (updated != state) level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && (!oldState.is(this) || oldState.getValue(FACING) != state.getValue(FACING))) {
            refresh(level, pos);

        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Slope slope = state.getValue(SLOPE);
        return slope == Slope.FLAT ? SHAPES.get(state.getValue(FACING))
                : SLOPED_SHAPES.get(slope).get(state.getValue(FACING));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = state.rotate(mirror.getRotation(state.getValue(FACING)));
        return mirror == Mirror.NONE ? mirrored : mirrored.setValue(SLOPE, state.getValue(SLOPE).reversed());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SLOPE);
    }
}
