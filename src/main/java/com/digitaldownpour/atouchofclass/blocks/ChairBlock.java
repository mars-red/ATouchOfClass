package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModEntities;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** One-block leather chair with a usable seat. */
public class ChairBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ChairBlock> CODEC = simpleCodec(ChairBlock::new);
    private static final VoxelShape[] SHAPES = createShapes();

    public ChairBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends ChairBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
        if (!level.isClientSide && !replacement.is(this)) {
            level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.25))
                    .forEach(SeatEntity::discard);
        }
        super.onRemove(state, level, pos, replacement, moved);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (player.isPassenger()) return InteractionResult.CONSUME;

        Vec3 seatPosition = new Vec3(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
        AABB search = new AABB(seatPosition, seatPosition).inflate(0.2);
        List<SeatEntity> nearby = level.getEntitiesOfClass(SeatEntity.class, search);
        SeatEntity seat = nearby.stream().findFirst().orElse(null);
        if (seat != null && !seat.getPassengers().isEmpty()) return InteractionResult.CONSUME;
        if (seat == null) {
            seat = new SeatEntity(ModEntities.LOVE_SEAT_SEAT.get(), level);
            seat.setPos(seatPosition);
            level.addFreshEntity(seat);
        }

        player.setYRot(state.getValue(FACING).toYRot());
        player.startRiding(seat, true);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int turns = switch (state.getValue(FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        return SHAPES[turns];
    }

    private static VoxelShape[] createShapes() {
        VoxelShape base = Shapes.or(
                Block.box(0, 0, 1, 16, 8, 16),
                Block.box(0, 4, 1, 4, 13, 16),
                Block.box(12, 4, 1, 16, 13, 16),
                Block.box(0, 8, 10, 16, 16, 16));
        VoxelShape[] shapes = new VoxelShape[4];
        for (int turns = 0; turns < 4; turns++) {
            shapes[turns] = rotateShape(base, turns);
        }
        return shapes;
    }

    private static VoxelShape rotateShape(VoxelShape base, int turns) {
        VoxelShape[] result = {Shapes.empty()};
        base.forAllBoxes((x0, y0, z0, x1, y1, z1) -> {
            for (int turn = 0; turn < turns; turn++) {
                double nextX0 = 1 - z1;
                double nextX1 = 1 - z0;
                z0 = x0;
                z1 = x1;
                x0 = nextX0;
                x1 = nextX1;
            }
            result[0] = Shapes.or(result[0], Shapes.box(x0, y0, z0, x1, y1, z1));
        });
        return result[0].optimize();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
