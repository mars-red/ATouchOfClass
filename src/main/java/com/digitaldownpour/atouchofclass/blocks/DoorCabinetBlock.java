package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Matching vertical cabinets form two-block door pairs, starting at the stack bottom. */
public class DoorCabinetBlock extends CabinetBlock {
    public static final BooleanProperty JOINED_ABOVE = BooleanProperty.create("joined_above");
    public static final BooleanProperty JOINED_BELOW = BooleanProperty.create("joined_below");

    public DoorCabinetBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(JOINED_ABOVE, false)
                .setValue(JOINED_BELOW, false));
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return connections(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return connections(state, level, pos);
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    private BlockState connections(BlockState state, LevelReader level, BlockPos pos) {
        // Derive pairing from positions, never from neighbors' potentially stale
        // join flags. Changed flags propagate upward through shape updates.
        int index = 0;
        BlockPos below = pos.below();
        while (joins(state, level.getBlockState(below))) {
            index++;
            below = below.below();
        }
        boolean upper = (index & 1) != 0;
        return state
                .setValue(JOINED_ABOVE, !upper && joins(state, level.getBlockState(pos.above())))
                .setValue(JOINED_BELOW, upper);
    }

    private boolean joins(BlockState state, BlockState neighbor) {
        return neighbor.is(this) && neighbor.getValue(FACING) == state.getValue(FACING);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(JOINED_ABOVE, JOINED_BELOW);
    }
}
