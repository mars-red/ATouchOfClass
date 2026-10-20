package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/** A shower panel whose translucent perimeter disappears against matching, aligned panels. */
public class GlassShowerSurroundBlock extends ThreeHighGlassPanelBlock {
    public static final BooleanProperty LEFT_CONNECTED = BooleanProperty.create("left_connected");
    public static final BooleanProperty RIGHT_CONNECTED = BooleanProperty.create("right_connected");
    public static final BooleanProperty UP_CONNECTED = BooleanProperty.create("up_connected");
    public static final BooleanProperty DOWN_CONNECTED = BooleanProperty.create("down_connected");

    public GlassShowerSurroundBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(LEFT_CONNECTED, false)
                .setValue(RIGHT_CONNECTED, false)
                .setValue(UP_CONNECTED, false)
                .setValue(DOWN_CONNECTED, false));
    }


    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : connections(state, context.getLevel(), context.getClickedPos());
    }

    private BlockState connections(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        return state
                .setValue(LEFT_CONNECTED, connects(state, level.getBlockState(pos.relative(left))))
                .setValue(RIGHT_CONNECTED, connects(state, level.getBlockState(pos.relative(right))))
                .setValue(UP_CONNECTED, connects(state, level.getBlockState(pos.above())))
                .setValue(DOWN_CONNECTED, connects(state, level.getBlockState(pos.below())));
    }

    private boolean connects(BlockState state, BlockState neighbor) {
        return neighbor.is(this) && neighbor.getValue(FACING) == state.getValue(FACING);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && state.getValue(PART) == 0) {
            for (int i = 0; i < 3; i++) {
                BlockPos partPos = pos.above(i);
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(this)) {
                    level.setBlock(partPos, connections(partState, level, partPos), Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return connections(state, level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT_CONNECTED, RIGHT_CONNECTED, UP_CONNECTED, DOWN_CONNECTED);
    }
}
