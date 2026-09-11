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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

import com.digitaldownpour.atouchofclass.elevator.ElevatorLockManager;
import com.digitaldownpour.atouchofclass.elevator.ElevatorUtil;

/**
 * Exterior (or waiting-area) call button. Must sit directly atop an
 * ElevatorFloorBlock; this is what lets it discover which shaft/floor it
 * belongs to, purely by recomputation - nothing is stored beyond the
 * visible STATUS light, which is itself just a reactive projection of
 * shaft validity and lock state, never authoritative on its own. Occupies a
 * full 16x16x16 cube - only the FACING face (toward the player at
 * placement time) shows the status light texture; the other five faces are
 * plain brushed steel, matching the rest of the shaft's construction.
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

    public ElevatorCallButtonBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STATUS, Status.IDLE).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATUS, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).getBlock() instanceof ElevatorFloorBlock;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            refreshStatus(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (!level.isClientSide) {
            refreshStatus(level, pos);
        }
    }

    /** Recomputes and applies the button's status light without side effects on the lock itself. */
    public static void refreshStatus(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ElevatorCallButtonBlock)) {
            return;
        }
        Status status = computeStatus(level, pos);
        if (state.getValue(STATUS) != status) {
            level.setBlock(pos, state.setValue(STATUS, status), 3);
        }
    }

    private static Status computeStatus(Level level, BlockPos pos) {
        Optional<ElevatorUtil.Footprint> footprint = ElevatorUtil.floodFillFloor(level, pos.below());
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        com.digitaldownpour.atouchofclass.elevator.ElevatorFlow.handleCallButtonPress(level, pos);
        return InteractionResult.SUCCESS;
    }
}
