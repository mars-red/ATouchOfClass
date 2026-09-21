package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModEntities;
import com.digitaldownpour.atouchofclass.entities.OvenBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** A stainless-steel oven with the behavior and recipes of a vanilla furnace. */
public class OvenBlock extends AbstractFurnaceBlock {
    public static final MapCodec<OvenBlock> CODEC = simpleCodec(OvenBlock::new);

    public OvenBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends AbstractFurnaceBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OvenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createFurnaceTicker(level, type, ModEntities.OVEN_BE.get());
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof OvenBlockEntity oven) {
            player.openMenu((MenuProvider) oven);
            player.awardStat(Stats.INTERACT_WITH_FURNACE);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        double centerX = pos.getX() + 0.5;
        double frontY = pos.getY() + 0.2 + random.nextDouble() * 0.4;
        double centerZ = pos.getZ() + 0.5;
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(centerX, pos.getY(), centerZ, SoundEvents.FURNACE_FIRE_CRACKLE,
                    SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }

        Direction facing = state.getValue(FACING);
        double across = random.nextDouble() * 0.6 - 0.3;
        double particleX = facing.getAxis() == Direction.Axis.X
                ? centerX + facing.getStepX() * 0.52 : centerX + across;
        double particleZ = facing.getAxis() == Direction.Axis.Z
                ? centerZ + facing.getStepZ() * 0.52 : centerZ + across;
        level.addParticle(ParticleTypes.SMOKE, particleX, frontY, particleZ, 0.0, 0.0, 0.0);
    }
}
