package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.entities.RefrigeratorBlockEntity;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A two-wide, 2.75-block-tall refrigerator backed by one 54-slot container. */
public class RefrigeratorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public enum Layer implements StringRepresentable {
        LOWER("lower", 0),
        MIDDLE("middle", 1),
        UPPER("upper", 2);

        private final String name;
        private final int offset;

        Layer(String name, int offset) {
            this.name = name;
            this.offset = offset;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public int offset() {
            return offset;
        }
    }

    public enum Side implements StringRepresentable {
        LEFT("left"),
        RIGHT("right");

        private final String name;

        Side(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final MapCodec<RefrigeratorBlock> CODEC = simpleCodec(RefrigeratorBlock::new);
    public static final EnumProperty<Layer> LAYER = EnumProperty.create("layer", Layer.class);
    public static final EnumProperty<Side> SIDE = EnumProperty.create("side", Side.class);

    private static final VoxelShape FULL_SHAPE = Shapes.block();
    private static final VoxelShape UPPER_SHAPE = Block.box(0, 0, 0, 16, 12, 16);
    private static final ThreadLocal<Boolean> REMOVING_STRUCTURE = ThreadLocal.withInitial(() -> false);

    public RefrigeratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LAYER, Layer.LOWER)
                .setValue(SIDE, Side.LEFT));
    }

    @Override
    protected MapCodec<? extends RefrigeratorBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState baseState = defaultBlockState().setValue(FACING, facing);

        for (Layer layer : Layer.values()) {
            for (Side side : Side.values()) {
                BlockPos partPos = partPos(context.getClickedPos(), facing, layer, side);
                BlockState partState = baseState.setValue(LAYER, layer).setValue(SIDE, side);
                if (!context.getLevel().getBlockState(partPos).canBeReplaced(context)
                        || !context.getLevel().isUnobstructed(partState, partPos, CollisionContext.empty())) {
                    return null;
                }
                if (layer == Layer.LOWER && !context.getLevel().getBlockState(partPos.below())
                        .isFaceSturdy(context.getLevel(), partPos.below(), Direction.UP)) {
                    return null;
                }
            }
        }
        return baseState.setValue(LAYER, Layer.LOWER).setValue(SIDE, Side.LEFT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && isMaster(state)) {
            Direction facing = state.getValue(FACING);
            for (Layer layer : Layer.values()) {
                for (Side side : Side.values()) {
                    if (layer == Layer.LOWER && side == Side.LEFT) continue;
                    level.setBlock(partPos(pos, facing, layer, side),
                            state.setValue(LAYER, layer).setValue(SIDE, side), Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Layer layer = state.getValue(LAYER);
        Side side = state.getValue(SIDE);

        if (layer == Layer.LOWER
                && !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }
        if (side == Side.RIGHT) {
            BlockState left = level.getBlockState(pos.relative(facing.getClockWise()));
            if (!matches(left, facing, layer, Side.LEFT)) return false;
        }
        if (layer != Layer.LOWER) {
            Layer belowLayer = layer == Layer.UPPER ? Layer.MIDDLE : Layer.LOWER;
            return matches(level.getBlockState(pos.below()), facing, belowLayer, side);
        }
        return true;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (REMOVING_STRUCTURE.get()) {
            return state;
        }
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel) {
            BlockPos masterPos = masterPos(state, pos);
            if (level.getBlockEntity(masterPos) instanceof RefrigeratorBlockEntity refrigerator) {
                player.openMenu(refrigerator);
                player.awardStat(Stats.OPEN_CHEST);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockState replacement = level.getBlockState(pos);
        if (!level.isClientSide() && !replacement.is(this) && !REMOVING_STRUCTURE.get()) {
            REMOVING_STRUCTURE.set(true);
            try {
                Direction facing = state.getValue(FACING);
                BlockPos masterPos = masterPos(state, pos);
                if (level.getBlockEntity(masterPos) instanceof RefrigeratorBlockEntity refrigerator) {
                    Containers.dropContents(level, masterPos, refrigerator);
                    refrigerator.clearContent();
                }
                for (Layer layer : Layer.values()) {
                    for (Side side : Side.values()) {
                        BlockPos otherPos = partPos(masterPos, facing, layer, side);
                        if (otherPos.equals(pos)) continue;
                        BlockState otherState = level.getBlockState(otherPos);
                        if (matches(otherState, facing, layer, side)) {
                            level.destroyBlock(otherPos, false);
                        }
                    }
                }
            } finally {
                REMOVING_STRUCTURE.set(false);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isMaster(state) ? new RefrigeratorBlockEntity(pos, state) : null;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isMaster(state) && level.getBlockEntity(pos) instanceof RefrigeratorBlockEntity refrigerator) {
            refrigerator.recheckOpen();
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockPos masterPos = masterPos(state, pos);
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(masterPos));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(LAYER) == Layer.UPPER ? UPPER_SHAPE : FULL_SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LAYER, SIDE);
    }

    private static boolean isMaster(BlockState state) {
        return state.getValue(LAYER) == Layer.LOWER && state.getValue(SIDE) == Side.LEFT;
    }

    private boolean matches(BlockState state, Direction facing, Layer layer, Side side) {
        return state.is(this)
                && state.getValue(FACING) == facing
                && state.getValue(LAYER) == layer
                && state.getValue(SIDE) == side;
    }

    private static BlockPos masterPos(BlockState state, BlockPos pos) {
        BlockPos lowerPos = pos.below(state.getValue(LAYER).offset());
        return state.getValue(SIDE) == Side.RIGHT
                ? lowerPos.relative(state.getValue(FACING).getClockWise())
                : lowerPos;
    }

    public static BlockPos partPos(BlockPos masterPos, Direction facing, Layer layer, Side side) {
        BlockPos partPos = masterPos.above(layer.offset());
        return side == Side.RIGHT ? partPos.relative(facing.getCounterClockWise()) : partPos;
    }
}
