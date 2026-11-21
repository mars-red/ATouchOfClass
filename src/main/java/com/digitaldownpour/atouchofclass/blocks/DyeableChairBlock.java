package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

abstract class DyeableChairBlock extends OutdoorChairBlock {
    private final DyeColor color;

    protected DyeableChairBlock(Properties properties, VoxelShape shape, double seatHeight, DyeColor color) {
        super(properties, shape, seatHeight);
        this.color = color;
    }

    protected abstract DyeableChairBlock variant(DyeColor dye);

    @Override
    protected boolean keepsSeat(BlockState replacement) {
        return replacement.getBlock().getClass() == getClass();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        DyeColor dye = stack.get(DataComponents.DYE);
        if (dye == null) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (color != DyeColor.WHITE || dye == DyeColor.WHITE) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            BlockState dyed = variant(dye).defaultBlockState()
                    .setValue(FACING, state.getValue(FACING));
            if (!level.setBlockAndUpdate(pos, dyed)) return InteractionResult.FAIL;
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
