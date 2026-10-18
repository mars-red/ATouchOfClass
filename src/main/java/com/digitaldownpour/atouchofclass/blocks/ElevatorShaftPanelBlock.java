package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Tiles vertically to form an elevator shaft. Shaft membership and floor
 * order are never stored on the block itself: they are recomputed on demand
 * by scanning contiguous panel blocks (see ElevatorUtil) - that discovery
 * logic is keyed purely on block type and position, so it is unaffected by
 * FACING. FACING here is purely cosmetic: only the face pointed toward the
 * player at placement time shows the panel texture, the other five faces
 * show plain brushed steel, matching the call button/destination control.
 */
public class ElevatorShaftPanelBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ElevatorShaftPanelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placement = super.getStateForPlacement(context);
        if (placement == null) return null;
        return placement
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
