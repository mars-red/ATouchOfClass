package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Independent one-block panels: vertical connections affect appearance and use, never survival. */
public class ShutterBlock extends Block {
    public static final MapCodec<ShutterBlock> CODEC = simpleCodec(ShutterBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty JOINED_ABOVE = BooleanProperty.create("joined_above");
    public static final BooleanProperty JOINED_BELOW = BooleanProperty.create("joined_below");

    public ShutterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(HINGE, DoorHingeSide.LEFT).setValue(OPEN, false)
                .setValue(JOINED_ABOVE, false).setValue(JOINED_BELOW, false));
    }

    @Override
    protected MapCodec<? extends ShutterBlock> codec() { return CODEC; }

    private boolean joins(BlockState state, BlockState neighbor) {
        return neighbor.is(this) && neighbor.getValue(FACING) == state.getValue(FACING)
                && neighbor.getValue(HINGE) == state.getValue(HINGE);
    }

    private BlockState connections(BlockState state, BlockGetter level, BlockPos pos) {
        return state.setValue(JOINED_ABOVE, joins(state, level.getBlockState(pos.above())))
                .setValue(JOINED_BELOW, joins(state, level.getBlockState(pos.below())));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        boolean open = false;
        // Top/bottom clicks extend an existing shutter column; ordinary mounting
        // uses the clicked wall face, regardless of the player's viewing direction.
        if (facing.getAxis().isVertical()) {
            BlockState target = context.getLevel().getBlockState(pos.relative(facing.getOpposite()));
            if (!target.is(this)) return null;
            facing = target.getValue(FACING);
            open = target.getValue(OPEN);
        }
        double x = context.getClickLocation().x - pos.getX();
        double z = context.getClickLocation().z - pos.getZ();
        DoorHingeSide hinge = switch (facing) {
            case NORTH -> x > .5 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            case EAST -> z > .5 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            case SOUTH -> x > .5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case WEST -> z > .5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            default -> throw new IllegalStateException("Shutters require a horizontal facing");
        };
        BlockState state = defaultBlockState().setValue(FACING, facing).setValue(HINGE, hinge);
        // Newly joined panels take the nearest group's state, with below taking precedence.
        BlockState below = context.getLevel().getBlockState(pos.below());
        BlockState above = context.getLevel().getBlockState(pos.above());
        if (joins(state, below)) open = below.getValue(OPEN);
        else if (joins(state, above)) open = above.getValue(OPEN);
        return connections(state.setValue(OPEN, open), context.getLevel(), pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        // No support dependency and no removal propagation: each shutter breaks alone.
        return connections(state, level, pos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) setGroupOpen(state, level, pos, state.getValue(OPEN));
    }

    private void setGroupOpen(BlockState state, Level level, BlockPos pos, boolean open) {
        level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_ALL);
        for (Direction vertical : new Direction[]{Direction.DOWN, Direction.UP}) {
            BlockPos cursor = pos.relative(vertical);
            while (level.isInWorldBounds(cursor) && level.hasChunkAt(cursor)) {
                BlockState neighbor = level.getBlockState(cursor);
                if (!joins(state, neighbor)) break;
                level.setBlock(cursor, neighbor.setValue(OPEN, open), Block.UPDATE_ALL);
                cursor = cursor.relative(vertical);
            }
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            boolean open = !state.getValue(OPEN);
            setGroupOpen(state, level, pos, open);
            level.playSound(null, pos, open ? SoundEvents.WOODEN_TRAPDOOR_OPEN
                    : SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, .8F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction edge = state.getValue(FACING).getOpposite();
        if (state.getValue(OPEN)) {
            edge = state.getValue(HINGE) == DoorHingeSide.LEFT
                    ? state.getValue(FACING).getClockWise() : state.getValue(FACING).getCounterClockWise();
        }
        // Like a vanilla door, selection and collision cover the entire thin panel.
        return switch (edge) {
            case NORTH -> box(0, 0, 0, 16, 16, 2);
            case SOUTH -> box(0, 0, 14, 16, 16, 16);
            case WEST -> box(0, 0, 0, 2, 16, 16);
            case EAST -> box(14, 0, 0, 16, 16, 16);
            default -> throw new IllegalStateException("Invalid shutter edge");
        };
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .setValue(HINGE, state.getValue(HINGE) == DoorHingeSide.LEFT ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HINGE, OPEN, JOINED_ABOVE, JOINED_BELOW);
    }
}
