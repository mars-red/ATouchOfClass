package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModEntities;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;

/** Three-wide leather sofa with one independently usable seat per section. */
public class SofaBlock extends HorizontalDirectionalBlock {
    public enum Section implements StringRepresentable {
        LEFT("left", -1), MIDDLE("middle", 0), RIGHT("right", 1);

        private final String name;
        private final int offset;

        Section(String name, int offset) {
            this.name = name;
            this.offset = offset;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final MapCodec<SofaBlock> CODEC = simpleCodec(SofaBlock::new);
    public static final EnumProperty<Section> SECTION = EnumProperty.create("section", Section.class);
    private static final VoxelShape[][] SHAPES = createShapes();

    public SofaBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SECTION, Section.MIDDLE));
    }

    @Override
    protected MapCodec<? extends SofaBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState()
                .setValue(FACING, facing)
                .setValue(SECTION, Section.MIDDLE);
        BlockPos center = context.getClickedPos();
        for (Section section : Section.values()) {
            BlockPos target = partPos(center, facing, section);
            BlockState part = state.setValue(SECTION, section);
            if (!context.getLevel().getBlockState(target).canBeReplaced(context)
                    || !context.getLevel().isUnobstructed(part, target, CollisionContext.empty())
                    || !context.getLevel().getBlockState(target.below())
                            .isFaceSturdy(context.getLevel(), target.below(), Direction.UP)) {
                return null;
            }
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            for (Section section : Section.values()) {
                if (section == Section.MIDDLE) continue;
                level.setBlock(partPos(pos, facing, section),
                        state.setValue(SECTION, section), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }
        Section section = state.getValue(SECTION);
        if (section == Section.MIDDLE) return true;
        Direction facing = state.getValue(FACING);
        BlockPos center = pos.relative(facing.getCounterClockWise(), section.offset);
        return matches(level.getBlockState(center), facing, Section.MIDDLE);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockState replacement = level.getBlockState(pos);
        if (!level.isClientSide() && !replacement.is(this)) {
            Direction facing = state.getValue(FACING);
            BlockPos center = centerPos(pos, state);
            removeSeats(level, center, facing);
            for (Section section : Section.values()) {
                BlockPos otherPos = partPos(center, facing, section);
                if (otherPos.equals(pos)) continue;
                BlockState other = level.getBlockState(otherPos);
                if (matches(other, facing, section)) {
                    level.destroyBlock(otherPos, false);
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isPassenger()) return InteractionResult.CONSUME;

        Vec3 seatPosition = new Vec3(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
        AABB search = new AABB(seatPosition, seatPosition).inflate(0.2);
        List<SeatEntity> nearby = level.getEntitiesOfClass(SeatEntity.class, search);
        SeatEntity seat = nearby.stream().findFirst().orElse(null);
        if (seat != null && !seat.getPassengers().isEmpty()) return InteractionResult.CONSUME;
        if (seat == null) {
            seat = new SeatEntity(ModEntities.LOVE_SEAT_SEAT.get(), level);
            seat.setPos(seatPosition);
            if (!level.addFreshEntity(seat)) return InteractionResult.FAIL;
        }

        if (!seat.mountFacing(player, state.getValue(FACING))) return InteractionResult.FAIL;
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void removeSeats(Level level, BlockPos center, Direction facing) {
        for (Section section : Section.values()) {
            BlockPos part = partPos(center, facing, section);
            level.getEntitiesOfClass(SeatEntity.class, new AABB(part).inflate(0.25))
                    .forEach(SeatEntity::discard);
        }
    }

    public static BlockPos partPos(BlockPos center, Direction facing, Section section) {
        return center.relative(facing.getClockWise(), section.offset);
    }

    private static BlockPos centerPos(BlockPos pos, BlockState state) {
        Section section = state.getValue(SECTION);
        return pos.relative(state.getValue(FACING).getCounterClockWise(), section.offset);
    }

    private boolean matches(BlockState state, Direction facing, Section section) {
        return state.is(this) && state.getValue(FACING) == facing && state.getValue(SECTION) == section;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int section = state.getValue(SECTION).ordinal();
        int turns = switch (state.getValue(FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        return SHAPES[section][turns];
    }

    private static VoxelShape[][] createShapes() {
        VoxelShape[][] shapes = new VoxelShape[Section.values().length][4];
        for (Section section : Section.values()) {
            VoxelShape base = Shapes.or(
                    Block.box(0, 0, 1, 16, 8, 16),
                    Block.box(0, 8, 10, 16, 16, 16));
            if (section == Section.LEFT) {
                base = Shapes.or(base, Block.box(0, 4, 1, 4, 13, 16));
            } else if (section == Section.RIGHT) {
                base = Shapes.or(base, Block.box(12, 4, 1, 16, 13, 16));
            }
            for (int turns = 0; turns < 4; turns++) {
                shapes[section.ordinal()][turns] = rotateShape(base, turns);
            }
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
        builder.add(FACING, SECTION);
    }
}
