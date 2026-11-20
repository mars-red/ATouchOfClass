package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Render marker for the procedural flames. All state lives in the block state. */
public final class FirePitBlockEntity extends BlockEntity {
    public FirePitBlockEntity(BlockPos pos, BlockState state) {
        super(ModEntities.FIRE_PIT_BE.get(), pos, state);
    }
}
