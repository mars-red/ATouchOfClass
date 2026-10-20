package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;
import java.util.Arrays;

/** Same-block connections: perimeter aprons and legs only at convex/concave corners. */
public class DiningTableBlock extends Block {
    // Clockwise order also defines rotation and mirror permutations.
    public static final BooleanProperty[] EDGES = {
            BooleanProperty.create("north"), BooleanProperty.create("east"),
            BooleanProperty.create("south"), BooleanProperty.create("west")};
    public static final BooleanProperty[] LEGS = {
            BooleanProperty.create("north_east"), BooleanProperty.create("south_east"),
            BooleanProperty.create("south_west"), BooleanProperty.create("north_west")};
    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final VoxelShape[] SHAPES = createShapes();
    private static final VoxelShape TOP_SUPPORT = box(0, 15, 0, 16, 16, 16);

    public DiningTableBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any();
        for (BooleanProperty edge : EDGES) state = state.setValue(edge, false);
        for (BooleanProperty leg : LEGS) state = state.setValue(leg, true);
        registerDefaultState(state);
    }


    private BlockState connected(BlockState state, BlockGetter level, BlockPos pos) {
        for (int i = 0; i < 4; i++) {
            Direction a = DIRECTIONS[i], b = DIRECTIONS[(i + 1) % 4];
            boolean first = level.getBlockState(pos.relative(a)).is(this);
            boolean second = level.getBlockState(pos.relative(b)).is(this);
            boolean diagonal = level.getBlockState(pos.relative(a).relative(b)).is(this);
            state = state.setValue(EDGES[i], first)
                    .setValue(LEGS[i], (!first && !second) || (first && second && !diagonal));
        }
        return state;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connected(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return state.canSurvive(level, pos) ? connected(state, level, pos) : Blocks.AIR.defaultBlockState();
    }

    private void refresh(Level level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (current.is(this)) {
            BlockState next = connected(current, level, pos);
            if (next != current) level.setBlock(pos, next, UPDATE_ALL);
        }
    }

    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this)) {
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) refresh(level, pos.offset(x, 0, z));
        }
    }

    @Override protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockState replacement = level.getBlockState(pos);
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
        if (!level.isClientSide() && !replacement.is(this)) {
            // Diagonal changes do not receive vanilla cardinal-neighbor shape updates.
            // Defer until the replacement is installed, including support-loss removals.
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                BlockPos neighbor = pos.offset(x, 0, z);
                if (level.getBlockState(neighbor).is(this)) level.scheduleTick(neighbor, this, 1);
            }
        }
    }

    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { refresh(level, pos); }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int index = 0;
        for (int i = 0; i < 4; i++) {
            if (state.getValue(EDGES[i])) index |= 1 << i;
            if (state.getValue(LEGS[i])) index |= 1 << (i + 4);
        }
        return SHAPES[index];
    }

    @Override protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) { return TOP_SUPPORT; }

    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState result = state;
        for (int i = 0; i < 4; i++) {
            Direction target = rotation.rotate(DIRECTIONS[i]);
            int j = Arrays.asList(DIRECTIONS).indexOf(target);
            result = result.setValue(EDGES[j], state.getValue(EDGES[i])).setValue(LEGS[j], state.getValue(LEGS[i]));
        }
        return result;
    }

    @Override protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        BlockState result = state;
        for (int i = 0; i < 4; i++) {
            int edge = Arrays.asList(DIRECTIONS).indexOf(mirror.mirror(DIRECTIONS[i]));
            // Reflection reverses the winding of each pair of corner directions.
            int corner = Arrays.asList(DIRECTIONS).indexOf(mirror.mirror(DIRECTIONS[(i + 1) % 4]));
            result = result.setValue(EDGES[edge], state.getValue(EDGES[i])).setValue(LEGS[corner], state.getValue(LEGS[i]));
        }
        return result;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EDGES).add(LEGS);
    }

    private static VoxelShape[] createShapes() {
        VoxelShape[] shapes = new VoxelShape[256];
        for (int mask = 0; mask < 256; mask++) {
            boolean n = (mask & 1) != 0, e = (mask & 2) != 0, s = (mask & 4) != 0, w = (mask & 8) != 0;
            VoxelShape shape = Shapes.or(box(1, 13, 0, 15, 16, 16), box(0, 13, 1, 1, 16, 15), box(15, 13, 1, 16, 16, 15));
            if (n || w) shape = Shapes.or(shape, box(0, 13, 0, 1, 16, 1));
            if (n || e) shape = Shapes.or(shape, box(15, 13, 0, 16, 16, 1));
            if (s || w) shape = Shapes.or(shape, box(0, 13, 15, 1, 16, 16));
            if (s || e) shape = Shapes.or(shape, box(15, 13, 15, 16, 16, 16));
            if (!n) shape = Shapes.or(shape, box(w ? 0 : 3.25, 11, 2.25, e ? 16 : 12.75, 13, 3.25));
            if (!s) shape = Shapes.or(shape, box(w ? 0 : 3.25, 11, 12.75, e ? 16 : 12.75, 13, 13.75));
            if (!w) shape = Shapes.or(shape, box(2.25, 11, n ? 0 : 3.25, 3.25, 13, s ? 16 : 12.75));
            if (!e) shape = Shapes.or(shape, box(12.75, 11, n ? 0 : 3.25, 13.75, 13, s ? 16 : 12.75));
            for (int i = 0; i < 4; i++) if ((mask & (1 << (i + 4))) != 0) {
                int x = i < 2 ? 12 : 2, z = (i == 0 || i == 3) ? 2 : 12;
                shape = Shapes.or(shape, box(x, 0, z, x + 2, 13, z + 2));
            }
            shapes[mask] = shape.optimize();
        }
        return shapes;
    }
}
