package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import javax.annotation.Nullable;

public class CopperChandelierBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 7, 1, 15, 11, 15), Block.box(3.5, 4, 3.5, 12.5, 7, 12.5),
            Block.box(6, 1, 6, 10, 4, 10), Block.box(7, 11, 7, 9, 16, 9));
    public CopperChandelierBlock(Properties properties) { super(properties); }
    @Nullable
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return context.getClickedFace() == Direction.DOWN && defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? defaultBlockState() : null;
    }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState support = level.getBlockState(above);
        // Fine chains do not have a sturdy full face; accept their vertical axis explicitly.
        if (support.getBlock() instanceof CopperChandelierChainBlock || support.getBlock() instanceof ChainBlock) {
            return support.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y;
        }
        return Block.canSupportCenter(level, above, Direction.DOWN);
    }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction == Direction.UP && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
