package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModEntities;
import com.digitaldownpour.atouchofclass.ModSounds;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;

/** A compact porcelain toilet with a usable seat and a toggleable lid. */
public class ToiletBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ToiletBlock> CODEC = simpleCodec(ToiletBlock::new);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        double[][] boxes = {
                {4, 0, 4, 12, 1, 13},
                {5, 1, 5, 11, 6, 12},
                {2, 6, 1, 14, 10, 13},
                {2, 8, 12, 14, 16, 16}
        };
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape shape = Shapes.empty();
            int turns = switch (direction) {
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
                default -> 0;
            };
            for (double[] box : boxes) {
                double x0 = box[0], z0 = box[2], x1 = box[3], z1 = box[5];
                for (int i = 0; i < turns; i++) {
                    double nextX0 = 16 - z1;
                    double nextX1 = 16 - z0;
                    z0 = x0;
                    z1 = x1;
                    x0 = nextX0;
                    x1 = nextX1;
                }
                shape = Shapes.or(shape, Block.box(x0, box[1], z0, x1, box[4], z1));
            }
            SHAPES.put(direction, shape);
        }
    }

    public ToiletBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    protected MapCodec<? extends ToiletBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(OPEN, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        return interact(state, level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        return interact(state, level, pos, player);
    }

    private InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player) {
        if (player.isSecondaryUseActive()) {
            toggleLid(state, level, pos, player);
        return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isPassenger()) return InteractionResult.CONSUME;

        Direction facing = state.getValue(FACING);
        Vec3 seatPosition = new Vec3(pos.getX() + 0.5 + facing.getStepX() / 16.0,
                pos.getY() + 0.225, pos.getZ() + 0.5 + facing.getStepZ() / 16.0);
        AABB search = new AABB(seatPosition, seatPosition).inflate(0.2);
        SeatEntity seat = level.getEntitiesOfClass(SeatEntity.class, search).stream().findFirst().orElse(null);
        if (seat != null && !seat.getPassengers().isEmpty()) return InteractionResult.CONSUME;
        if (seat == null) {
            seat = new SeatEntity(ModEntities.LOVE_SEAT_SEAT.get(), level);
            seat.setPos(seatPosition);
            if (!level.addFreshEntity(seat)) return InteractionResult.FAIL;
        }

        if (!player.startRiding(seat, true, true)) {
            seat.discard();
            return InteractionResult.FAIL;
        }
        float yaw = facing.toYRot();
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setYBodyRot(yaw);
        player.setOldRot();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundPlayerRotationPacket(yaw, false, player.getXRot(), false));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockState replacement = level.getBlockState(pos);
        if (!level.isClientSide() && !replacement.is(this)) {
            level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.25))
                    .forEach(SeatEntity::discard);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    private void toggleLid(BlockState state, Level level, BlockPos pos, Player player) {
        boolean opening = !state.getValue(OPEN);
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(OPEN, opening), Block.UPDATE_ALL);
            level.playSound(null, pos,
                    ModSounds.TOILET_LID.value(),
                    SoundSource.BLOCKS, 0.6F, opening ? 1.15F : 1.0F);
        }

    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }
}
