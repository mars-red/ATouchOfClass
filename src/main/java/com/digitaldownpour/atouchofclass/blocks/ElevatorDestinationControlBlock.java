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
import net.minecraft.world.phys.BlockHitResult;

/**
 * Interior destination control. Must sit directly atop an ElevatorFloorBlock
 * inside the cab's own footprint - which is precisely what makes it reachable
 * only from inside: a floor-mounted control is only in reach of someone
 * already standing on that floor tile, i.e. inside the cab. No separate
 * "interior-only" geometry restriction is needed beyond that placement rule.
 * Occupies a full 16x16x16 cube - only the FACING face (toward the player
 * at placement time) shows the control panel texture; the other five faces
 * are plain brushed steel, matching the rest of the shaft's construction.
 */
public class ElevatorDestinationControlBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ElevatorDestinationControlBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        com.digitaldownpour.atouchofclass.elevator.ElevatorFlow.handleDestinationControlUse(level, pos, player);
        return InteractionResult.SUCCESS;
    }
}
