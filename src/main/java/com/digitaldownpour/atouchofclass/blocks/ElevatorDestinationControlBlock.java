package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;

/**
 * Interior destination control. The lower half must sit directly atop an
 * ElevatorFloorBlock inside the cab's own footprint - which is precisely
 * what makes it reachable only from inside: a floor-mounted control is only
 * in reach of someone already standing on that floor tile, i.e. inside the
 * cab. No separate "interior-only" geometry restriction is needed beyond
 * that placement rule. The block is two tiles tall: PART=0 is a plain
 * brushed-steel base and PART=1 (directly above it) carries the FACING
 * control panel texture, matching the rest of the shaft's construction on
 * its other five faces.
 */
public class ElevatorDestinationControlBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 0 = lower (base) part, 1 = upper part carrying the control panel texture. */
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 1);

    public ElevatorDestinationControlBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxY() || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        BlockState placement = super.getStateForPlacement(context);
        if (placement == null) return null;
        return placement
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(PART, 0);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(PART) == 0) {
            return level.getBlockState(pos.below()).getBlock() instanceof ElevatorFloorBlock;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof ElevatorDestinationControlBlock && below.getValue(PART) == 0;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide() && state.getValue(PART) == 0 && !oldState.is(state.getBlock())) {
            level.setBlock(pos.above(), state.setValue(PART, 1), 3);
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
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos basePos = state.getValue(PART) == 0 ? pos : pos.below();
        com.digitaldownpour.atouchofclass.elevator.ElevatorFlow.handleDestinationControlUse(level, basePos, player);
        return InteractionResult.SUCCESS;
    }
}
