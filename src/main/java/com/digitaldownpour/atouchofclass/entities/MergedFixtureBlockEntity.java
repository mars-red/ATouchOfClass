package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModEntities;
import com.digitaldownpour.atouchofclass.blocks.InductionCooktopBlock;
import com.digitaldownpour.atouchofclass.blocks.KitchenSinkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/** Render marker for a merged fixture. The pairing is entirely in the block states. */
public final class MergedFixtureBlockEntity extends BlockEntity {
    public MergedFixtureBlockEntity(BlockPos pos, BlockState state) {
        super(ModEntities.MERGED_FIXTURE_BE.get(), pos, state);
    }

    public static ChestType part(BlockState state) {
        if (state.getBlock() instanceof InductionCooktopBlock) {
            return state.getValue(InductionCooktopBlock.TYPE);
        }
        if (state.getBlock() instanceof KitchenSinkBlock) {
            return state.getValue(KitchenSinkBlock.TYPE);
        }
        return ChestType.SINGLE;
    }
}
