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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PatioChairBlock extends ChairBlock {
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
    private static final VoxelShape EAST = rotateShape(NORTH, 1);
    private static final VoxelShape SOUTH = rotateShape(NORTH, 2);
    private static final VoxelShape WEST = rotateShape(NORTH, 3);

    private final DyeColor color;

    public PatioChairBlock(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null && state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        if (!super.canSurvive(state, level, pos) || above.getY() >= level.getMaxY()) return false;
        VoxelShape overhead = level.getBlockState(above).getCollisionShape(level, above).move(0, 1, 0);
        return !Shapes.joinIsNotEmpty(shape(state), overhead, BooleanOp.AND);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    private static VoxelShape shape(BlockState state) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    protected boolean keepsSeat(BlockState replacement) {
        return replacement.getBlock() instanceof PatioChairBlock;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        // The seat attachment is 0.55 blocks above the mount's origin.
        return mountSeat(state, level, pos, player, 10.0 / 16 - 0.55);
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
