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
import java.util.Locale;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.BooleanOp;

public class OakRailingBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<OakRailingBlock> CODEC = simpleCodec(OakRailingBlock::new);
    public enum Slope implements StringRepresentable {
        FLAT, UPPER_LEFT, LOWER_RIGHT, LOWER_LEFT, UPPER_RIGHT, TRANSITION_LEFT, TRANSITION_RIGHT,
        BOTTOM_LEFT, BOTTOM_RIGHT, INNER_LEFT, INNER_RIGHT, OUTER_LEFT, OUTER_RIGHT;

        @Override
        public String getSerializedName() { return name().toLowerCase(Locale.ROOT); }

        public Slope reversed() {
            return switch (this) {
                case UPPER_LEFT -> UPPER_RIGHT; case UPPER_RIGHT -> UPPER_LEFT;
                case LOWER_LEFT -> LOWER_RIGHT; case LOWER_RIGHT -> LOWER_LEFT;
                case TRANSITION_LEFT -> TRANSITION_RIGHT; case TRANSITION_RIGHT -> TRANSITION_LEFT;
                case BOTTOM_LEFT -> BOTTOM_RIGHT; case BOTTOM_RIGHT -> BOTTOM_LEFT;
                case INNER_LEFT -> INNER_RIGHT; case INNER_RIGHT -> INNER_LEFT;
                case OUTER_LEFT -> OUTER_RIGHT; case OUTER_RIGHT -> OUTER_LEFT;
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
                            BooleanOp.OR);
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
        // Clicking a level rail's end continues its orientation into the outside
        // corner cell, even though that cell has no wall directly behind it.
        Direction clicked = context.getClickedFace();
        BlockPos sourcePos = context.getClickedPos().relative(clicked.getOpposite());
        BlockState source = context.getLevel().getBlockState(sourcePos);
        if (clicked.getAxis().isHorizontal() && source.is(this) && levelRail(source)
                && clicked.getAxis() != source.getValue(FACING).getAxis()) {
            BlockState state = defaultBlockState().setValue(FACING, source.getValue(FACING));
            if (state.canSurvive(context.getLevel(), context.getClickedPos()))
                return withSlope(state, context.getLevel(), context.getClickedPos());
        }
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
        return directWall(level, pos, state.getValue(FACING))
                || outsideSide(level, pos, state.getValue(FACING)) != null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState() : withSlope(state, level, pos);
    }

    private static boolean levelRail(BlockState state) {
        Slope slope = state.getValue(SLOPE);
        return slope == Slope.FLAT || slope == Slope.INNER_LEFT || slope == Slope.INNER_RIGHT
                || slope == Slope.OUTER_LEFT || slope == Slope.OUTER_RIGHT;
    }

    private boolean directWall(BlockGetter level, BlockPos pos, Direction facing) {
        BlockPos wall = pos.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    private boolean anchoredRail(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.is(this) && state.getValue(FACING) == facing && levelRail(state)
                && directWall(level, pos, facing);
    }

    @Nullable
    private Direction outsideSide(BlockGetter level, BlockPos pos, Direction facing) {
        if (directWall(level, pos, facing)) return null;
        for (Direction side : new Direction[] {facing.getCounterClockWise(), facing.getClockWise()}) {
            BlockPos wall = pos.relative(side).relative(facing.getOpposite());
            BlockState support = level.getBlockState(wall);
            if (!support.isFaceSturdy(level, wall, facing) || !support.isFaceSturdy(level, wall, side.getOpposite())) continue;
            if (anchoredRail(level, pos.relative(side), facing)
                    || anchoredRail(level, pos.relative(facing.getOpposite()), side.getOpposite())) return side;
        }
        return null;
    }

    private Slope wallCorner(BlockGetter level, BlockPos pos, Direction facing) {
        Direction side = outsideSide(level, pos, facing);
        if (side != null) return side == facing.getCounterClockWise() ? Slope.OUTER_LEFT : Slope.OUTER_RIGHT;
        // A perpendicular rail in front enters this cell from the second wall.
        BlockState front = level.getBlockState(pos.relative(facing));
        if (front.is(this) && levelRail(front)) {
            Direction other = front.getValue(FACING);
            if (other.getAxis() != facing.getAxis() && directWall(level, pos, other)
                    && directWall(level, pos.relative(facing), other)) {
                return other == facing.getClockWise() ? Slope.INNER_LEFT : Slope.INNER_RIGHT;
            }
        }
        return Slope.FLAT;
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
        if (slope == Slope.FLAT && (matches(level, leftPos, facing) || newel(level,leftPos))
                && (matches(level, rightPos, facing) || newel(level,rightPos))
                && !matches(level, pos.above(), facing) && !matches(level, pos.below(), facing)) {
            // Derive connections from physical neighbors, not transition states, so
            // transitions cannot spread along an otherwise horizontal run.
            Slope left = matches(level,leftPos,facing)?baseSlope(level.getBlockState(leftPos), level, leftPos):Slope.FLAT;
            Slope r = matches(level,rightPos,facing)?baseSlope(level.getBlockState(rightPos), level, rightPos):Slope.FLAT;
            if (left == Slope.LOWER_RIGHT && r == Slope.FLAT)
                slope = Slope.TRANSITION_LEFT;
            else if (r == Slope.LOWER_LEFT && left == Slope.FLAT)
                slope = Slope.TRANSITION_RIGHT;
        }
        // At the foot of a run, level out the first rising block itself.
        // A descending transition in the preceding flat block creates a dip.
        if (slope == Slope.UPPER_LEFT && flatNeighbor(level, leftPos, facing))
            slope = Slope.BOTTOM_LEFT;
        else if (slope == Slope.UPPER_RIGHT && flatNeighbor(level, rightPos, facing))
            slope = Slope.BOTTOM_RIGHT;
        // The top diagonal can finish directly against a newel without an
        // intervening level railing. Reuse the existing 45-degree transition.
        if (!matches(level,pos.above(),facing)) {
            if (slope == Slope.UPPER_LEFT && newel(level,rightPos)) slope=Slope.TRANSITION_LEFT;
            else if (slope == Slope.UPPER_RIGHT && newel(level,leftPos)) slope=Slope.TRANSITION_RIGHT;
        }
        if (slope == Slope.FLAT) slope = wallCorner(level, pos, facing);
        return state.setValue(SLOPE, slope);
    }
    private boolean flatNeighbor(BlockGetter level, BlockPos pos, Direction facing) {
        return matches(level, pos, facing)
                && baseSlope(level.getBlockState(pos), level, pos) == Slope.FLAT;
    }
    private boolean newel(BlockGetter level,BlockPos pos) {
        BlockState other=level.getBlockState(pos);
        return other.getBlock() instanceof OakNewelPostBlock
                && other.getValue(OakNewelPostBlock.HALF)==DoubleBlockHalf.LOWER;
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
        if (!level.isClientSide() && (!oldState.is(this) || oldState.getValue(FACING) != state.getValue(FACING))) {
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
