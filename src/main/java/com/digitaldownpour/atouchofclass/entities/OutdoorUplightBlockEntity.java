package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Render marker for the wall wash. The fixture's direction lives in its block state. */
public final class OutdoorUplightBlockEntity extends BlockEntity {
    public OutdoorUplightBlockEntity(BlockPos pos, BlockState state) {
        super(ModEntities.OUTDOOR_UPLIGHT_BE.get(), pos, state);
    }
}
