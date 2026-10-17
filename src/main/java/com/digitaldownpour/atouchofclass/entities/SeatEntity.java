package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModBlocks;
import com.digitaldownpour.atouchofclass.blocks.DiningChairBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
                || state.getBlock() instanceof DiningChairBlock
                || state.is(ModBlocks.TOILET.get());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos pos = blockPosition();
        var state = level().getBlockState(pos);
        if (!(state.getBlock() instanceof DiningChairBlock)) {
            return super.getDismountLocationForPassenger(passenger);
        }

        Direction front = state.getValue(DiningChairBlock.FACING);
        Direction right = front.getClockWise(), left = front.getCounterClockWise();
        BlockPos[] exits = {
                pos.relative(front), pos.relative(right), pos.relative(left), pos.relative(front.getOpposite()),
                pos.relative(front).relative(right), pos.relative(front).relative(left),
                pos.relative(front.getOpposite()).relative(right), pos.relative(front.getOpposite()).relative(left)
        };
        // Prefer a supported exit beside the chair, including a one-block step down.
        for (int dy : new int[]{0, -1}) {
            for (BlockPos exit : exits) {
                Vec3 location = DismountHelper.findSafeDismountLocation(passenger.getType(), level(), exit.offset(0, dy, 0), true);
                if (location != null) return location;
            }
        }

        // With all exits blocked, stand just above the seat. A small forward
        // offset keeps a standing player's body clear of the tall back posts.
        Vec3 aboveSeat = new Vec3(pos.getX() + 0.5 + front.getStepX() * 0.125,
                pos.getY() + 10.0 / 16 + 0.01,
                pos.getZ() + 0.5 + front.getStepZ() * 0.125);
        if (DismountHelper.canDismountTo(level(), aboveSeat, passenger, passenger.getPose())) return aboveSeat;

        // If headroom above the seat is also obstructed, try above the backrest.
        return new Vec3(pos.getX() + 0.5, pos.getY() + 1.5 + 0.01, pos.getZ() + 0.5);
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
