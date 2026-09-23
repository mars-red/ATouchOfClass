package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.entities.DresserBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A two-wide dresser whose upper sections fill one quarter of their block spaces. */
public class DresserBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public enum Side implements StringRepresentable {
        LEFT("left"), RIGHT("right");

        private final String name;

        Side(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final MapCodec<DresserBlock> CODEC = simpleCodec(DresserBlock::new);
    public static final EnumProperty<Side> SIDE = EnumProperty.create("side", Side.class);
    public static final BooleanProperty UPPER = BooleanProperty.create("upper");
    private static final VoxelShape[][] SHAPES = createShapes();

    public DresserBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SIDE, Side.LEFT)
                .setValue(UPPER, false));
    }

    @Override
    protected MapCodec<? extends DresserBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState base = defaultBlockState().setValue(FACING, facing);
        BlockPos anchor = context.getClickedPos();
        for (boolean upper : new boolean[]{false, true}) {
            for (Side side : Side.values()) {
                BlockPos target = partPos(anchor, facing, side, upper);
                BlockState part = base.setValue(SIDE, side).setValue(UPPER, upper);
                if (target.getY() >= context.getLevel().getMaxY()
                        || !context.getLevel().getBlockState(target).canBeReplaced(context)
                        || !context.getLevel().isUnobstructed(part, target, CollisionContext.empty())) {
                    return null;
                }
                if (!upper && !context.getLevel().getBlockState(target.below())
                        .isFaceSturdy(context.getLevel(), target.below(), Direction.UP)) {
                    return null;
                }
            }
        }
        return base;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) return;
        Direction facing = state.getValue(FACING);
        level.setBlock(partPos(pos, facing, Side.RIGHT, false),
                state.setValue(SIDE, Side.RIGHT), Block.UPDATE_ALL);
        level.setBlock(partPos(pos, facing, Side.LEFT, true),
                state.setValue(UPPER, true), Block.UPDATE_ALL);
        level.setBlock(partPos(pos, facing, Side.RIGHT, true),
                state.setValue(SIDE, Side.RIGHT).setValue(UPPER, true), Block.UPDATE_ALL);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Side side = state.getValue(SIDE);
        if (state.getValue(UPPER)) {
            return matches(level.getBlockState(pos.below()), facing, side, false);
        }
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }
        return side == Side.LEFT || matches(
                level.getBlockState(pos.relative(facing.getCounterClockWise())),
                facing, Side.LEFT, false);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel) {
            blockEntity(level, pos, state).ifPresent(dresser -> {
                player.openMenu(dresser);
                player.awardStat(Stats.OPEN_CHEST);
            });
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
        if (!replacement.is(this)) {
            BlockPos anchor = anchorPos(pos, state);
            if (!level.isClientSide && pos.equals(anchor)) {
                Containers.dropContentsOnDestroy(state, replacement, level, pos);
            }
            if (!level.isClientSide) {
                Direction facing = state.getValue(FACING);
                for (boolean upper : new boolean[]{false, true}) {
                    for (Side side : Side.values()) {
                        BlockPos otherPos = partPos(anchor, facing, side, upper);
                        if (otherPos.equals(pos)) continue;
                        BlockState other = level.getBlockState(otherPos);
                        if (matches(other, facing, side, upper)) {
                            level.destroyBlock(otherPos, false);
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, replacement, moved);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isAnchor(state) ? new DresserBlockEntity(pos, state) : null;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        blockEntity(level, pos, state).ifPresent(DresserBlockEntity::recheckOpen);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return blockEntity(level, pos, state)
                .map(AbstractContainerMenu::getRedstoneSignalFromContainer)
                .orElse(0);
    }

    private Optional<DresserBlockEntity> blockEntity(Level level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(anchorPos(pos, state)) instanceof DresserBlockEntity dresser
                ? Optional.of(dresser) : Optional.empty();
    }

    private static boolean isAnchor(BlockState state) {
        return state.getValue(SIDE) == Side.LEFT && !state.getValue(UPPER);
    }

    private static BlockPos anchorPos(BlockPos pos, BlockState state) {
        BlockPos lower = state.getValue(UPPER) ? pos.below() : pos;
        return state.getValue(SIDE) == Side.RIGHT
                ? lower.relative(state.getValue(FACING).getCounterClockWise()) : lower;
    }

    public static BlockPos partPos(BlockPos anchor, Direction facing, Side side, boolean upper) {
        BlockPos lower = side == Side.RIGHT ? anchor.relative(facing.getClockWise()) : anchor;
        return upper ? lower.above() : lower;
    }

    private boolean matches(BlockState state, Direction facing, Side side, boolean upper) {
        return state.is(this) && state.getValue(FACING) == facing
                && state.getValue(SIDE) == side && state.getValue(UPPER) == upper;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int index = state.getValue(SIDE).ordinal() * 2 + (state.getValue(UPPER) ? 1 : 0);
        int turns = switch (state.getValue(FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        return SHAPES[index][turns];
    }

    private static VoxelShape[][] createShapes() {
        VoxelShape[][] shapes = new VoxelShape[4][4];
        for (Side side : Side.values()) {
            for (int upper = 0; upper < 2; upper++) {
                VoxelShape base = upper == 1
                        ? Block.box(0, 0, 1, 16, 4, 16)
                        : Block.box(0, 0, 1, 16, 16, 16);
                int index = side.ordinal() * 2 + upper;
                for (int turns = 0; turns < 4; turns++) {
                    shapes[index][turns] = rotateShape(base, turns);
                }
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
        builder.add(FACING, SIDE, UPPER);
    }
}
