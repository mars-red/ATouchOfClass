package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;

/** A two wide, two and a half long bed assembled from a two-by-three-by-two grid. */
public class FourPostBedBlock extends HorizontalDirectionalBlock {
    public enum Section implements StringRepresentable {
        HEAD("head", 2), MIDDLE("middle", 1), FOOT("foot", 0);

        private final String name;
        private final int offset;

        Section(String name, int offset) {
            this.name = name;
            this.offset = offset;
        }

        @Override public String getSerializedName() { return name; }
        public int offset() { return offset; }
    }

    public enum Side implements StringRepresentable {
        LEFT("left", 0), RIGHT("right", 1);

        private final String name;
        private final int offset;

        Side(String name, int offset) {
            this.name = name;
            this.offset = offset;
        }

        @Override public String getSerializedName() { return name; }
        public int offset() { return offset; }
    }

    public static final MapCodec<FourPostBedBlock> CODEC = simpleCodec(FourPostBedBlock::new);
    public static final EnumProperty<Section> SECTION = EnumProperty.create("section", Section.class);
    public static final EnumProperty<Side> SIDE = EnumProperty.create("side", Side.class);
    public static final BooleanProperty UPPER = BooleanProperty.create("upper");
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    private static final Section[] PLACEMENT_ORDER = {Section.FOOT, Section.MIDDLE, Section.HEAD};
    private static final VoxelShape[][] SHAPES = createShapes();

    public FourPostBedBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SECTION, Section.FOOT)
                .setValue(SIDE, Side.LEFT)
                .setValue(UPPER, false)
                .setValue(OCCUPIED, false));
    }

    @Override
    protected MapCodec<? extends FourPostBedBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The model's facing runs from head toward foot. The player stands at
        // the foot and looks toward the head, so these directions are opposite.
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        BlockPos anchor = context.getClickedPos();
        for (int height = 0; height < 2; height++) {
            for (Section section : Section.values()) {
                for (Side side : Side.values()) {
                    BlockPos target = partPos(anchor, facing, section, side, height == 1);
                    BlockState partState = state.setValue(SECTION, section)
                            .setValue(SIDE, side).setValue(UPPER, height == 1);
                    if (target.getY() >= context.getLevel().getMaxY()
                            || !context.getLevel().getBlockState(target).canBeReplaced(context)
                            || !context.getLevel().isUnobstructed(partState, target, CollisionContext.empty())) {
                        return null;
                    }
                    if (height == 0 && !context.getLevel().getBlockState(target.below())
                            .isFaceSturdy(context.getLevel(), target.below(), Direction.UP)) {
                        return null;
                    }
                }
            }
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide()) return;
        Direction facing = state.getValue(FACING);
        for (int height = 0; height < 2; height++) {
            for (Section section : PLACEMENT_ORDER) {
                for (Side side : Side.values()) {
                    if (height == 0 && section == Section.FOOT && side == Side.LEFT) continue;
                    level.setBlock(partPos(pos, facing, section, side, height == 1),
                            state.setValue(SECTION, section).setValue(SIDE, side)
                                    .setValue(UPPER, height == 1), Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(UPPER)) {
            BlockState lower = level.getBlockState(pos.below());
            return lower.is(this) && !lower.getValue(UPPER)
                    && lower.getValue(SECTION) == state.getValue(SECTION)
                    && lower.getValue(SIDE) == state.getValue(SIDE)
                    && lower.getValue(FACING) == facing;
        }
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        if (state.getValue(SIDE) == Side.RIGHT) {
            BlockState left = level.getBlockState(pos.relative(facing.getCounterClockWise()));
            if (!matches(left, facing, state.getValue(SECTION), Side.LEFT, false)) return false;
        }
        Section section = state.getValue(SECTION);
        if (section == Section.FOOT) return true;
        Section previous = section == Section.MIDDLE ? Section.FOOT : Section.MIDDLE;
        return matches(level.getBlockState(pos.relative(facing)),
                facing, previous, state.getValue(SIDE), false);
    }

    private boolean matches(BlockState state, Direction facing, Section section, Side side, boolean upper) {
        return state.is(this) && state.getValue(FACING) == facing
                && state.getValue(SECTION) == section && state.getValue(SIDE) == side
                && state.getValue(UPPER) == upper;
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
            BlockPos anchor = pos.relative(facing, state.getValue(SECTION).offset())
                    .relative(facing.getCounterClockWise(), state.getValue(SIDE).offset())
                    .below(state.getValue(UPPER) ? 1 : 0);
            for (int height = 0; height < 2; height++) {
                for (Section section : Section.values()) {
                    for (Side side : Side.values()) {
                        BlockPos otherPos = partPos(anchor, facing, section, side, height == 1);
                        if (otherPos.equals(pos)) continue;
                        BlockState other = level.getBlockState(otherPos);
                        if (matches(other, facing, section, side, height == 1)) {
                            level.destroyBlock(otherPos, false);
                        }
                    }
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    private static BlockPos partPos(BlockPos anchor, Direction facing, Section section, Side side, boolean upper) {
        return anchor.relative(facing.getOpposite(), section.offset())
                .relative(facing.getClockWise(), side.offset()).above(upper ? 1 : 0);
    }

    /** Only the two middle mattress blocks are sleeping positions. */
    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, LivingEntity sleeper) {
        return state.getValue(SECTION) == Section.MIDDLE && !state.getValue(UPPER);
    }

    @Override
    public Direction getBedDirection(BlockState state, LevelReader level, BlockPos pos) {
        // Vanilla beds point from foot to head; the model's FACING is reversed.
        return state.getValue(FACING).getOpposite();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS_SERVER;

        Direction facing = state.getValue(FACING);
        Side side = state.getValue(SIDE);
        BlockPos sleepPos = pos.below(state.getValue(UPPER) ? 1 : 0)
                .relative(facing, state.getValue(SECTION).offset() - Section.MIDDLE.offset());
        BlockState mattress = level.getBlockState(sleepPos);
        if (!matches(mattress, facing, Section.MIDDLE, side, false)) {
            return InteractionResult.CONSUME;
        }

        if (!BedBlock.canSetSpawn(level)) {
            level.removeBlock(sleepPos, false);
            Vec3 center = sleepPos.getCenter();
            level.explode(null, level.damageSources().badRespawnPointExplosion(center), null,
                    center, 5.0F, true, Level.ExplosionInteraction.BLOCK);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (mattress.getValue(OCCUPIED)) {
            player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            return InteractionResult.SUCCESS_SERVER;
        }

        player.startSleepInBed(sleepPos).ifRight(ignored ->
                com.digitaldownpour.atouchofclass.FourPostBedSleepEvents.alignSleeper(player)).ifLeft(problem -> {
            if (problem.getMessage() != null) {
                player.displayClientMessage(problem.getMessage(), true);
            }
        });
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type,
                                                                     LevelReader level, BlockPos pos, float orientation) {
        if (state.getValue(SECTION) != Section.MIDDLE || state.getValue(UPPER)) {
            return Optional.empty();
        }
        if (level instanceof Level world && !BedBlock.canSetSpawn(world)) {
            return Optional.empty();
        }

        Direction facing = state.getValue(FACING);
        Side sleepingSide = state.getValue(SIDE);
        BlockPos anchor = pos.relative(facing, Section.MIDDLE.offset())
                .relative(facing.getCounterClockWise(), sleepingSide.offset());
        List<BlockPos> candidates = new ArrayList<>(10);
        for (Side side : new Side[]{sleepingSide, sleepingSide == Side.LEFT ? Side.RIGHT : Side.LEFT}) {
            Direction outside = side == Side.LEFT ? facing.getCounterClockWise() : facing.getClockWise();
            for (Section section : Section.values()) {
                candidates.add(partPos(anchor, facing, section, side, false).relative(outside));
            }
        }
        for (Side side : Side.values()) {
            candidates.add(partPos(anchor, facing, Section.FOOT, side, false).relative(facing));
            candidates.add(partPos(anchor, facing, Section.HEAD, side, false).relative(facing.getOpposite()));
        }
        for (boolean onlySafe : new boolean[]{true, false}) {
            for (BlockPos candidate : candidates) {
                Vec3 stand = DismountHelper.findSafeDismountLocation(type, level, candidate, onlySafe);
                if (stand != null) {
                    return Optional.of(ServerPlayer.RespawnPosAngle.of(stand, pos, orientation));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int shapeIndex = state.getValue(SECTION).ordinal() * 4
                + state.getValue(SIDE).ordinal() * 2 + (state.getValue(UPPER) ? 1 : 0);
        int turns = switch (state.getValue(FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        return SHAPES[shapeIndex][turns];
    }

    private static VoxelShape[][] createShapes() {
        VoxelShape[][] shapes = new VoxelShape[Section.values().length * Side.values().length * 2][4];
        for (Section section : Section.values()) {
            for (Side side : Side.values()) {
                for (int height = 0; height < 2; height++) {
                    int index = section.ordinal() * 4 + side.ordinal() * 2 + height;
                    VoxelShape base = baseShape(section, side, height == 1);
                    for (int turns = 0; turns < 4; turns++) {
                        shapes[index][turns] = rotateShape(base, turns);
                    }
                }
            }
        }
        return shapes;
    }

    private static VoxelShape baseShape(Section section, Side side, boolean upper) {
        if (!upper) {
            return section == Section.FOOT
                    ? Block.box(0, 0, 8, 16, 16, 16)
                    : Block.box(0, 0, 0, 16, 13, 16);
        }
        int x0 = side == Side.LEFT ? 0 : 12;
        int x1 = x0 + 4;
        VoxelShape sideRail = Block.box(x0, 10, 0, x1, 15, 16);
        return switch (section) {
            case HEAD -> Shapes.or(sideRail, Block.box(x0, 0, 12, x1, 16, 16));
            case MIDDLE -> sideRail;
            case FOOT -> Shapes.or(Block.box(x0, 0, 8, x1, 16, 12),
                    Block.box(x0, 10, 8, x1, 15, 16));
        };
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
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SECTION, SIDE, UPPER, OCCUPIED);
    }
}
