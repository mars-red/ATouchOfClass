package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class OutdoorChairBlock extends ChairBlock {
    private final VoxelShape north;
    private final VoxelShape east;
    private final VoxelShape south;
    private final VoxelShape west;
    private final double seatHeight;

    protected OutdoorChairBlock(Properties properties, VoxelShape north, double seatHeight) {
        super(properties);
        this.north = north;
        this.east = rotateShape(north, 1);
        this.south = rotateShape(north, 2);
        this.west = rotateShape(north, 3);
        this.seatHeight = seatHeight;
    }

    public double seatHeight() {
        return seatHeight;
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

    private VoxelShape shape(BlockState state) {
        return switch (state.getValue(FACING)) {
            case EAST -> east;
            case SOUTH -> south;
            case WEST -> west;
            default -> north;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        // Keep the mount origin inside this block even if the seat is below the default attachment.
        return mountSeat(state, level, pos, player, Math.max(0, seatHeight - 0.55));
    }
}
