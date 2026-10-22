package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Thin glazing that joins only across the latch seam of a matching tall door pair. */
public class TransomBlock extends HorizontalDirectionalBlock {
    public enum Join implements StringRepresentable {
        SINGLE, LEFT, RIGHT;
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public static final EnumProperty<Join> JOIN = EnumProperty.create("join", Join.class);

    public TransomBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(JOIN, Join.SINGLE));
    }

    private BlockState connections(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockState door = level.getBlockState(pos.below());
        if (!(door.getBlock() instanceof TallDoorBlock) || door.getValue(TallDoorBlock.PART) != 2
                || door.getValue(TallDoorBlock.FACING) != facing) return state.setValue(JOIN, Join.SINGLE);
        boolean left = door.getValue(TallDoorBlock.HINGE) == DoorHingeSide.LEFT;
        Direction towardPartner = left ? facing.getClockWise() : facing.getCounterClockWise();
        BlockPos partnerPos = pos.relative(towardPartner);
        BlockState other = level.getBlockState(partnerPos);
        BlockState partnerDoor = level.getBlockState(partnerPos.below());
        boolean paired = other.is(this) && other.getValue(FACING) == facing
                && partnerDoor.is(door.getBlock()) && partnerDoor.getValue(TallDoorBlock.PART) == 2
                && partnerDoor.getValue(TallDoorBlock.FACING) == facing
                && partnerDoor.getValue(TallDoorBlock.HINGE) != door.getValue(TallDoorBlock.HINGE);
        return state.setValue(JOIN, paired ? (left ? Join.LEFT : Join.RIGHT) : Join.SINGLE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (below.getBlock() instanceof TallDoorBlock) facing = below.getValue(TallDoorBlock.FACING);
        else if (below.getBlock() instanceof TallSidelightBlock) facing = below.getValue(TallSidelightBlock.FACING);
        return connections(defaultBlockState().setValue(FACING, facing), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return connections(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> box(0, 0, 0, 16, 16, 3);
            case EAST -> box(13, 0, 0, 16, 16, 16);
            case SOUTH -> box(0, 0, 13, 16, 16, 16);
            case WEST -> box(0, 0, 0, 3, 16, 16);
            default -> throw new IllegalStateException("Transoms must face horizontally");
        };
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        Join join = state.getValue(JOIN);
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .setValue(JOIN, join == Join.SINGLE ? join : join == Join.LEFT ? Join.RIGHT : Join.LEFT);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, JOIN);
    }
}
