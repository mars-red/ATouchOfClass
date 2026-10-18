package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.entities.NightstandBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A full-height, single-block nightstand with a 27-slot chest inventory. */
public class NightstandBlock extends HorizontalFurnitureBlock implements EntityBlock {
    public static final MapCodec<NightstandBlock> CODEC = simpleCodec(NightstandBlock::new);

    /** Geometry for a north-facing nightstand; the shared base class rotates it. */
    private static final double[][] NORTH_FACING_BOXES = {
            // Recessed feet.
            {2, 0, 2, 5, 2, 5},
            {11, 0, 2, 14, 2, 5},
            {2, 0, 11, 5, 2, 14},
            {11, 0, 11, 14, 2, 14},
            // Cabinet body and two raised drawer fronts.
            {1, 2, 1, 15, 14, 15},
            {2, 3, 0.65, 14, 7.85, 1.25},
            {2, 8.4, 0.65, 14, 13.25, 1.25},
            // Full-height top with one-unit clipped corners.
            {1, 14, 0, 15, 16, 16},
            {0, 14, 1, 1, 16, 15},
            {15, 14, 1, 16, 16, 15}
    };
    private static final VoxelShape TOP_SUPPORT = Block.box(0, 15, 0, 16, 16, 16);

    public NightstandBlock(Properties properties) {
        super(properties, NORTH_FACING_BOXES);
    }

    @Override
    protected MapCodec<? extends NightstandBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NightstandBlockEntity(pos, state);
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
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return TOP_SUPPORT;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel
                && level.getBlockEntity(pos) instanceof NightstandBlockEntity nightstand) {
            player.openMenu(nightstand);
            player.awardStat(Stats.OPEN_CHEST);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof NightstandBlockEntity nightstand) {
            nightstand.recheckOpen();
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
