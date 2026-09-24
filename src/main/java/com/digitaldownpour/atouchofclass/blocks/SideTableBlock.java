package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A one-block decorative table with a full-height top and no storage. */
public class SideTableBlock extends Block {
    public static final MapCodec<SideTableBlock> CODEC = simpleCodec(SideTableBlock::new);

    /**
     * Shared by every stripped-wood variant. The three-piece tabletop leaves
     * small corner notches, approximating the preview's subtly rounded top.
     */
    private static final VoxelShape SHAPE = Shapes.or(
            // Four legs.
            Block.box(2, 0, 2, 4, 13, 4),
            Block.box(12, 0, 2, 14, 13, 4),
            Block.box(2, 0, 12, 4, 13, 14),
            Block.box(12, 0, 12, 14, 13, 14),
            // Shallow apron.
            Block.box(3.25, 11, 2.25, 12.75, 13, 3.25),
            Block.box(3.25, 11, 12.75, 12.75, 13, 13.75),
            Block.box(2.25, 11, 3.25, 3.25, 13, 12.75),
            Block.box(12.75, 11, 3.25, 13.75, 13, 12.75),
            // Sixteen-unit-high tabletop with one-unit clipped corners.
            Block.box(1, 13, 0, 15, 16, 16),
            Block.box(0, 13, 1, 1, 16, 15),
            Block.box(15, 13, 1, 16, 16, 15)
    ).optimize();
    private static final VoxelShape TOP_SUPPORT = Block.box(0, 15, 0, 16, 16, 16);

    public SideTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SideTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return TOP_SUPPORT;
    }
}
