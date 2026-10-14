package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModBlocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Invisible, non-persistent mount used by seats and toilets. */
public class SeatEntity extends Entity {
    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0, 0, 0);
        if (!level().isClientSide
                && (getPassengers().isEmpty() || !supportsSeat())) {
            discard();
        }
    }

    private boolean supportsSeat() {
        var state = level().getBlockState(blockPosition());
        return state.is(ModBlocks.LEATHER_LOVE_SEAT.get())
                || state.is(ModBlocks.LEATHER_SOFA.get())
                || state.is(ModBlocks.LEATHER_CHAIR.get())
                || state.is(ModBlocks.TOILET.get());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
