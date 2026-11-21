package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PatioChairBlock extends OutdoorChairBlock {
    private static final VoxelShape NORTH = Shapes.or(
            box(0, 0, 0, 2, 13, 2), box(14, 0, 0, 16, 13, 2),
            box(0, 0, 14, 2, 13, 16), box(14, 0, 14, 16, 13, 16),
            box(0, 11.5, 2, 2, 13, 14), box(14, 11.5, 2, 16, 13, 14),
            box(0, 2, 2, 2, 3.5, 14), box(14, 2, 2, 16, 3.5, 14),
            box(1, 6, 0.5, 15, 7.5, 15.5), box(2, 7.5, 0.5, 14, 10, 15),
            // Stepped collision bounds for the slightly reclined back cushion and its support.
            box(2, 10, 11.25, 14, 14, 16),
            box(2, 14, 11.75, 14, 17, 16),
            box(2, 17, 12.2, 14, 20, 16));

    private final DyeColor color;

    public PatioChairBlock(Properties properties, DyeColor color) {
        super(properties, NORTH, 10.0 / 16);
        this.color = color;
    }

    @Override
    protected boolean keepsSeat(BlockState replacement) {
        return replacement.getBlock() instanceof PatioChairBlock;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        DyeColor dye = stack.get(DataComponents.DYE);
        if (dye == null) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (color != DyeColor.WHITE || dye == DyeColor.WHITE) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            BlockState dyed = ModBlocks.PATIO_CHAIRS.get(dye).get().defaultBlockState()
                    .setValue(FACING, state.getValue(FACING));
            if (!level.setBlockAndUpdate(pos, dyed)) return InteractionResult.FAIL;
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
