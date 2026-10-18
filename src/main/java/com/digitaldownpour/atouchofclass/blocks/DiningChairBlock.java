package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModEntities;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;

/** One-block dining chair with a tall back and a forward-facing usable seat. */
public class DiningChairBlock extends ChairBlock {
    public static final MapCodec<DiningChairBlock> CODEC = simpleCodec(DiningChairBlock::new);

    public DiningChairBlock(Properties properties) { super(properties); }

    @Override protected MapCodec<? extends DiningChairBlock> codec() { return CODEC; }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!super.canSurvive(state, level, pos) || pos.above().getY() >= level.getMaxY()) return false;
        // The single-block chair's back extends half a block into the space above.
        VoxelShape above = level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).move(0, 1, 0);
        return !Shapes.joinIsNotEmpty(DiningChairGeometry.shape(state.getValue(FACING)), above, BooleanOp.AND);
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DiningChairGeometry.shape(state.getValue(FACING));
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isPassenger() || player.isShiftKeyDown()) return InteractionResult.CONSUME;
        // Shared mount attachment is 0.55 blocks: 0.075 + 0.55 = the 10/16 seat top.
        Vec3 location = new Vec3(pos.getX() + 0.5, pos.getY() + 0.075, pos.getZ() + 0.5);
        var seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(location, location).inflate(0.2));
        SeatEntity seat = seats.stream().findFirst().orElse(null);
        if (seat != null && !seat.getPassengers().isEmpty()) return InteractionResult.CONSUME;
        float yaw = state.getValue(FACING).toYRot();
        if (seat == null) {
            seat = new SeatEntity(ModEntities.LOVE_SEAT_SEAT.get(), level);
            seat.setPos(location);
            seat.setYRot(yaw);
            if (!level.addFreshEntity(seat)) return InteractionResult.FAIL;
        }
        if (!seat.mountFacing(player, state.getValue(FACING))) return InteractionResult.FAIL;
        return InteractionResult.SUCCESS_SERVER;
    }
}
