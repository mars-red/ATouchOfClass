package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import com.digitaldownpour.atouchofclass.elevator.ElevatorLockManager;
import com.digitaldownpour.atouchofclass.elevator.ElevatorUtil;
import net.minecraft.server.level.ServerLevel;

/**
 * Exterior (or waiting-area) call button. The lower half must sit directly
 * atop an ElevatorFloorBlock; this is what lets it discover which
 * shaft/floor it belongs to, purely by recomputation - nothing is stored
 * beyond the visible STATUS light, which is itself just a reactive
 * projection of shaft validity and lock state, never authoritative on its
 * own. The block is two tiles tall: PART=0 is a plain brushed-steel base
 * and PART=1 (directly above it) carries the FACING status light texture,
 * matching the rest of the shaft's construction on its other five faces.
 */
public class ElevatorCallButtonBlock extends Block {
    public enum Status implements StringRepresentable {
        IDLE("idle"), BUSY("busy"), INVALID("invalid");
        private final String name;
        Status(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public static final EnumProperty<Status> STATUS = EnumProperty.create("status", Status.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 0 = lower (base) part, 1 = upper part carrying the status light. */
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 1);

    public ElevatorCallButtonBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(STATUS, Status.IDLE)
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATUS, FACING, PART);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxY() || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(PART, 0);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(PART) == 0) {
            return level.getBlockState(pos.below()).getBlock() instanceof ElevatorFloorBlock;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof ElevatorCallButtonBlock && below.getValue(PART) == 0;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide() && state.getValue(PART) == 0 && !oldState.is(state.getBlock())) {
            level.setBlock(pos.above(), state.setValue(PART, 1), 3);
            refreshStatus(level, pos);
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        BlockState newState = level.getBlockState(pos);
        if (!level.isClientSide() && state.getBlock() != newState.getBlock()) {
            BlockPos otherPos = state.getValue(PART) == 0 ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.getBlock() == this) {
                level.removeBlock(otherPos, isMoving);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (!level.isClientSide()) {
            refreshStatus(level, pos);
        }
    }

    /** Recomputes and applies the button's status light to both parts without side effects on the lock itself. */
    public static void refreshStatus(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ElevatorCallButtonBlock)) {
            return;
        }
        BlockPos basePos = state.getValue(PART) == 0 ? pos : pos.below();
        Status status = computeStatus(level, basePos);
        applyStatus(level, basePos, status);
        applyStatus(level, basePos.above(), status);
    }

    private static void applyStatus(Level level, BlockPos pos, Status status) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ElevatorCallButtonBlock && state.getValue(STATUS) != status) {
            level.setBlock(pos, state.setValue(STATUS, status), 3);
        }
    }

    private static Status computeStatus(Level level, BlockPos basePos) {
        Optional<ElevatorUtil.Footprint> footprint = ElevatorUtil.floodFillFloor(level, basePos.below());
        if (footprint.isEmpty()) {
            return Status.INVALID;
        }
        Optional<ElevatorUtil.ShaftColumn> shaft = ElevatorUtil.shaftForFootprint(level, footprint.get());
        if (shaft.isEmpty()) {
            return Status.INVALID;
        }
        ElevatorLockManager.ShaftKey key = ElevatorLockManager.ShaftKey.of(shaft.get());
        return ElevatorLockManager.get(key).isPresent() ? Status.BUSY : Status.IDLE;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos basePos = state.getValue(PART) == 0 ? pos : pos.below();
        com.digitaldownpour.atouchofclass.elevator.ElevatorFlow.handleCallButtonPress(level, basePos);
        return InteractionResult.SUCCESS;
    }
}
