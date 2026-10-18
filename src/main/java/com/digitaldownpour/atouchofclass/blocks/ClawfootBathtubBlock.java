package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;

/** A nine-section, 1.5-by-2.5-block clawfoot bathtub with a synchronized fill state. */
public class ClawfootBathtubBlock extends HorizontalDirectionalBlock {
    public enum Part implements StringRepresentable {
        FOOT("foot", 0),
        MIDDLE("middle", 1),
        HEAD("head", 2);

        private final String name;
        private final int offset;

        Part(String name, int offset) {
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
        LEFT("left", -1),
        CENTER("center", 0),
        RIGHT("right", 1);

        private final String name;
        private final int offset;

        Side(String name, int offset) {
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

    public static final MapCodec<ClawfootBathtubBlock> CODEC = simpleCodec(ClawfootBathtubBlock::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final EnumProperty<Side> SIDE = EnumProperty.create("side", Side.class);
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    private static final Map<Part, Map<Side, Map<Direction, VoxelShape>>> SHAPES = new EnumMap<>(Part.class);

    static {
        registerShapes(Part.FOOT, Side.LEFT, new double[][] {
                {14, 3, 0, 16, 5, 14}, {13, 4, 0, 16, 12, 14},
                {14, 4, 14, 16, 12, 16}, {13, 0, 10, 16, 4, 14}
        });
        registerShapes(Part.FOOT, Side.CENTER, new double[][] {
                {0, 3, 0, 16, 5, 14}, {0, 4, 12, 16, 12, 16}
        });
        registerShapes(Part.FOOT, Side.RIGHT, new double[][] {
                {0, 3, 0, 2, 5, 14}, {0, 4, 0, 3, 12, 14},
                {0, 4, 14, 2, 12, 16}, {0, 0, 10, 3, 4, 14}
        });
        registerShapes(Part.MIDDLE, Side.LEFT, new double[][] {
                {14, 3, 0, 16, 5, 16}, {13, 4, 0, 16, 12, 16}
        });
        registerShapes(Part.MIDDLE, Side.CENTER, new double[][] {
                {0, 3, 0, 16, 5, 16}
        });
        registerShapes(Part.MIDDLE, Side.RIGHT, new double[][] {
                {0, 3, 0, 2, 5, 16}, {0, 4, 0, 3, 12, 16}
        });
        registerShapes(Part.HEAD, Side.LEFT, new double[][] {
                {14, 3, 8, 16, 5, 16}, {13, 4, 10, 16, 12, 16},
                {14, 4, 8, 16, 16, 11}, {13, 0, 9, 16, 4, 13}
        });
        registerShapes(Part.HEAD, Side.CENTER, new double[][] {
                {0, 3, 8, 16, 5, 16}, {0, 4, 8, 16, 16, 11}
        });
        registerShapes(Part.HEAD, Side.RIGHT, new double[][] {
                {0, 3, 8, 2, 5, 16}, {0, 4, 10, 3, 12, 16},
                {0, 4, 8, 2, 16, 11}, {0, 0, 9, 3, 4, 13}
        });
    }

    private static void registerShapes(Part part, Side side, double[][] boxes) {
        Map<Direction, VoxelShape> directionalShapes = new EnumMap<>(Direction.class);
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
            directionalShapes.put(direction, shape);
        }
        SHAPES.computeIfAbsent(part, ignored -> new EnumMap<>(Side.class)).put(side, directionalShapes);
    }

    public ClawfootBathtubBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.FOOT)
                .setValue(SIDE, Side.CENTER)
                .setValue(FILLED, false));
    }

    @Override
    protected MapCodec<? extends ClawfootBathtubBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockState footState = defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, Part.FOOT)
                .setValue(SIDE, Side.CENTER)
                .setValue(FILLED, false);

        for (Part part : Part.values()) {
            for (Side side : Side.values()) {
                BlockPos partPos = partPos(context.getClickedPos(), facing, part, side);
                BlockState partState = footState.setValue(PART, part).setValue(SIDE, side);
                if (!context.getLevel().getBlockState(partPos).canBeReplaced(context)
                        || !context.getLevel().getBlockState(partPos.below()).isFaceSturdy(
                                context.getLevel(), partPos.below(), Direction.UP)
                        || !context.getLevel().isUnobstructed(partState, partPos, CollisionContext.empty())) {
                    return null;
                }
            }
        }
        return footState;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && state.getValue(PART) == Part.FOOT
                && state.getValue(SIDE) == Side.CENTER) {
            Direction facing = state.getValue(FACING);
            for (Part part : Part.values()) {
                for (Side side : new Side[] {Side.CENTER, Side.LEFT, Side.RIGHT}) {
                    if (part == Part.FOOT && side == Side.CENTER) continue;
                    level.setBlock(partPos(pos, facing, part, side),
                            state.setValue(PART, part).setValue(SIDE, side), Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }
        Part part = state.getValue(PART);
        Side side = state.getValue(SIDE);
        Direction facing = state.getValue(FACING);
        if (side != Side.CENTER) {
            Direction towardCenter = side == Side.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
            BlockState center = level.getBlockState(pos.relative(towardCenter));
            if (!center.is(this)
                    || center.getValue(PART) != part
                    || center.getValue(SIDE) != Side.CENTER
                    || center.getValue(FACING) != facing) {
                return false;
            }
        }
        if (part == Part.FOOT) return true;
        BlockState previous = level.getBlockState(pos.relative(facing.getOpposite()));
        Part expected = part == Part.MIDDLE ? Part.FOOT : Part.MIDDLE;
        return previous.is(this)
                && previous.getValue(PART) == expected
                && previous.getValue(SIDE) == side
                && previous.getValue(FACING) == facing;
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
            BlockPos footPos = footCenterPos(state, pos);
            for (Part part : Part.values()) {
                for (Side side : Side.values()) {
                    BlockPos otherPos = partPos(footPos, facing, part, side);
                    if (otherPos.equals(pos)) continue;
                    BlockState otherState = level.getBlockState(otherPos);
                    if (otherState.is(this)
                            && otherState.getValue(FACING) == facing
                            && otherState.getValue(PART) == part
                            && otherState.getValue(SIDE) == side) {
                        level.destroyBlock(otherPos, false);
                    }
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        toggleFilled(state, level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        toggleFilled(state, level, pos);
        return InteractionResult.SUCCESS;
    }

    private void toggleFilled(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            BlockPos footPos = footCenterPos(state, pos);
            boolean filled = !state.getValue(FILLED);
            for (Part part : Part.values()) {
                for (Side side : Side.values()) {
                    BlockPos partPos = partPos(footPos, facing, part, side);
                    BlockState partState = level.getBlockState(partPos);
                    if (partState.is(this)
                            && partState.getValue(FACING) == facing
                            && partState.getValue(PART) == part
                            && partState.getValue(SIDE) == side) {
                        level.setBlock(partPos, partState.setValue(FILLED, filled), Block.UPDATE_CLIENTS);
                    }
                }
            }
            level.playSound(null, footPos,
                    filled ? ModSounds.BATHTUB_FILL.value() : ModSounds.BATHTUB_DRAIN.value(),
                    SoundSource.BLOCKS, 0.8F, 1.0F);
        }

    }

    private static BlockPos footCenterPos(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return pos.relative(facing.getOpposite(), state.getValue(PART).offset())
                .relative(facing.getCounterClockWise(), state.getValue(SIDE).offset());
    }

    private static BlockPos partPos(BlockPos footCenterPos, Direction facing, Part part, Side side) {
        return footCenterPos.relative(facing, part.offset())
                .relative(facing.getClockWise(), side.offset());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(PART)).get(state.getValue(SIDE)).get(state.getValue(FACING));
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
        builder.add(FACING, PART, SIDE, FILLED);
    }
}
