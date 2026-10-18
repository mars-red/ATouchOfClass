package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;

/**
 * A three-block-tall sliding elevator door, centered in the block's depth
 * (unlike the flush-mounted TallDoorBlock). Doors are never toggled directly
 * by player interaction - only the elevator system (call button, destination
 * control, lock manager) drives the SLIDE_STEP property, via ElevatorAnimator.
 * Part 0 (bottom) must sit directly on an ElevatorFloorBlock, which is what
 * lets the rest of the elevator system discover which cab a door belongs to.
 *
 * <p>Doors placed side by side along the wall (perpendicular to FACING) form
 * one double-sliding-door opening: the MIRROR property is recomputed
 * reactively from the contiguous run of door columns whenever one is placed
 * or removed, so the low-coordinate half of the run retracts toward the low
 * side and the high-coordinate half retracts toward the high side - each
 * leaf visually slides away from the shared center line instead of all
 * leaves retracting the same direction.</p>
 */
public class ElevatorDoorBlock extends Block {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2); // 0 = bottom, 1 = middle, 2 = top
    public static final IntegerProperty SLIDE_STEP = IntegerProperty.create("slide_step", 0, 4);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty MIRROR = BooleanProperty.create("mirror");

    public static final int MAX_SLIDE_STEP = 4;
    private static final double THICKNESS_MIN = 6;
    private static final double THICKNESS_MAX = 10;

    public ElevatorDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(PART, 0)
                .setValue(SLIDE_STEP, 0)
                .setValue(FACING, Direction.NORTH)
                .setValue(MIRROR, false));
    }

    private static VoxelShape shapeFor(Direction facing, int slideStep, boolean mirror) {
        double openFraction = slideStep / (double) MAX_SLIDE_STEP;
        double visibleWidth = 16 * (1 - openFraction);
        if (visibleWidth <= 0) {
            return Shapes.empty();
        }
        double lo = mirror ? 16 - visibleWidth : 0;
        double hi = mirror ? 16 : visibleWidth;
        return switch (facing) {
            case NORTH, SOUTH -> Block.box(lo, 0, THICKNESS_MIN, hi, 16, THICKNESS_MAX);
            case EAST, WEST -> Block.box(THICKNESS_MIN, 0, lo, THICKNESS_MAX, 16, hi);
            default -> Shapes.block();
        };
    }

    /** The horizontal axis along which side-by-side door columns of the same facing line up. */
    private static Direction perpAxis(Direction facing) {
        return switch (facing) {
            case EAST, WEST -> Direction.SOUTH;
            default -> Direction.EAST;
        };
    }

    private static boolean isDoorBase(Level level, BlockPos pos, Direction facing) {
        BlockState s = level.getBlockState(pos);
        return s.getBlock() instanceof ElevatorDoorBlock && s.getValue(PART) == 0 && s.getValue(FACING) == facing;
    }

    /** Finds the contiguous run of PART-0 door columns (same facing) touching basePos, in ascending order. */
    private static List<BlockPos> findRun(Level level, BlockPos basePos, Direction facing) {
        Direction axis = perpAxis(facing);
        List<BlockPos> run = new ArrayList<>();
        run.add(basePos);
        BlockPos cursor = basePos;
        while (true) {
            BlockPos next = cursor.relative(axis.getOpposite());
            if (!isDoorBase(level, next, facing)) {
                break;
            }
            run.add(0, next);
            cursor = next;
        }
        cursor = basePos;
        while (true) {
            BlockPos next = cursor.relative(axis);
            if (!isDoorBase(level, next, facing)) {
                break;
            }
            run.add(next);
            cursor = next;
        }
        return run;
    }

    /** Recomputes MIRROR for every column in the run touching basePos: low half unmirrored, high half mirrored. */
    private static void refreshMirrorForRun(Level level, BlockPos basePos, Direction facing) {
        List<BlockPos> run = findRun(level, basePos, facing);
        int size = run.size();
        for (int i = 0; i < size; i++) {
            applyMirror(level, run.get(i), i >= size / 2);
        }
    }

    private static void applyMirror(Level level, BlockPos basePos, boolean mirror) {
        for (int part = 0; part < 3; part++) {
            BlockPos p = basePos.above(part);
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof ElevatorDoorBlock && s.getValue(MIRROR) != mirror) {
                level.setBlock(p, s.setValue(MIRROR, mirror), 3);
            }
        }
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING), state.getValue(SLIDE_STEP), state.getValue(MIRROR));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING), state.getValue(SLIDE_STEP), state.getValue(MIRROR));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, SLIDE_STEP, FACING, MIRROR);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return super.getStateForPlacement(context)
                .setValue(FACING, facing)
                .setValue(SLIDE_STEP, 0)
                .setValue(PART, 0)
                .setValue(MIRROR, false);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide() && state.getValue(PART) == 0) {
            Direction facing = state.getValue(FACING);
            level.setBlock(pos.above(), state.setValue(PART, 1).setValue(FACING, facing), 3);
            level.setBlock(pos.above(2), state.setValue(PART, 2).setValue(FACING, facing), 3);
            refreshMirrorForRun(level, pos, facing);
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        BlockState newState = level.getBlockState(pos);
        if (!level.isClientSide() && state.getBlock() != newState.getBlock()) {
            int part = state.getValue(PART);
            BlockPos basePos = pos.below(part);
            for (int i = 0; i < 3; i++) {
                if (i == part) continue;
                BlockPos p = basePos.above(i);
                BlockState other = level.getBlockState(p);
                if (other.getBlock() == this && other.getValue(PART) == i) {
                    level.destroyBlock(p, false);
                }
            }
            if (part == 0) {
                Direction facing = state.getValue(FACING);
                Direction axis = perpAxis(facing);
                BlockPos lowNeighbor = basePos.relative(axis.getOpposite());
                BlockPos highNeighbor = basePos.relative(axis);
                if (isDoorBase(level, lowNeighbor, facing)) {
                    refreshMirrorForRun(level, lowNeighbor, facing);
                }
                if (isDoorBase(level, highNeighbor, facing)) {
                    refreshMirrorForRun(level, highNeighbor, facing);
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }


    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int part = state.getValue(PART);
        if (part == 0) {
            return level.getBlockState(pos.below()).getBlock() instanceof ElevatorFloorBlock;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof ElevatorDoorBlock && below.getValue(PART) == part - 1;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Doors are driven exclusively by the elevator system (call button /
        // destination control), never by direct player interaction.
        return InteractionResult.PASS;
    }

    /** The three absolute positions (bottom, middle, top) of a door starting at basePos (part 0). */
    public static List<BlockPos> partsOf(BlockPos basePos) {
        return List.of(basePos, basePos.above(), basePos.above(2));
    }

    public static int getSlideStep(Level level, BlockPos basePos) {
        BlockState state = level.getBlockState(basePos);
        return state.getBlock() instanceof ElevatorDoorBlock ? state.getValue(SLIDE_STEP) : 0;
    }

    public static void setSlideStep(Level level, BlockPos basePos, int step) {
        for (BlockPos p : partsOf(basePos)) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof ElevatorDoorBlock) {
                level.setBlock(p, s.setValue(SLIDE_STEP, step), 2);
            }
        }
    }
}
