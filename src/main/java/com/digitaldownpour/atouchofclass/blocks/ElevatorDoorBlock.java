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
 * <p>The clicked half of the placement block selects the side the door
 * retracts toward. This choice is preserved when neighboring doors change,
 * so each leaf of a double door can retract toward its outer edge.</p>
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
        // Models rotate 180 degrees for SOUTH/WEST, reversing the mirrored edge.
        boolean retractHigh = mirror ^ (facing == Direction.SOUTH || facing == Direction.WEST);
        double lo = retractHigh ? 16 - visibleWidth : 0;
        double hi = retractHigh ? 16 : visibleWidth;
        return switch (facing) {
            case NORTH, SOUTH -> Block.box(lo, 0, THICKNESS_MIN, hi, 16, THICKNESS_MAX);
            case EAST, WEST -> Block.box(THICKNESS_MIN, 0, lo, THICKNESS_MAX, 16, hi);
            default -> Shapes.block();
        };
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
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState placement = super.getStateForPlacement(context);
        if (placement == null) return null;
        return placement
                .setValue(FACING, facing)
                .setValue(SLIDE_STEP, 0)
                .setValue(PART, 0)
                .setValue(MIRROR, mirrorForPlacement(context, facing));
    }

    private static boolean mirrorForPlacement(BlockPlaceContext context, Direction facing) {
        BlockPos pos = context.getClickedPos();
        double click = facing.getAxis() == Direction.Axis.Z
                ? context.getClickLocation().x - pos.getX()
                : context.getClickLocation().z - pos.getZ();
        boolean retractHigh = click > 0.5;
        // MIRROR is model-local; convert the clicked world edge through its rotation.
        return retractHigh ^ (facing == Direction.SOUTH || facing == Direction.WEST);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide() && state.getValue(PART) == 0) {
            Direction facing = state.getValue(FACING);
            level.setBlock(pos.above(), state.setValue(PART, 1).setValue(FACING, facing), 3);
            level.setBlock(pos.above(2), state.setValue(PART, 2).setValue(FACING, facing), 3);
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
