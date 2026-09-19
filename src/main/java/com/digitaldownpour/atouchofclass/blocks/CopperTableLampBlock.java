package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A one-block-tall table lamp matching the copper floor lamp. */
public class CopperTableLampBlock extends Block {
    public static final MapCodec<CopperTableLampBlock> CODEC = simpleCodec(CopperTableLampBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(4, 0, 4, 12, 3, 12),
            Block.box(7, 3, 7, 9, 9, 9),
            Block.box(2, 8, 2, 14, 10, 14),
            Block.box(3, 10, 3, 13, 12, 13),
            Block.box(4.5, 12, 4.5, 11.5, 14, 11.5),
            Block.box(5.5, 14, 5.5, 10.5, 16, 10.5));

    public CopperTableLampBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends CopperTableLampBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return context.getClickedFace() == Direction.UP
                && state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.below();
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return SHAPE;
    }
}
