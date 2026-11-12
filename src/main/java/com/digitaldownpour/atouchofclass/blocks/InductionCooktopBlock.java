package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.entities.MergedFixtureBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A cosmetic, countertop-height cooktop that can form one seamless two-block pair. */
public class InductionCooktopBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final EnumProperty<ChestType> TYPE = EnumProperty.create("type", ChestType.class);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    public InductionCooktopBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TYPE, ChestType.SINGLE));
    }


    @Override
    public @org.jetbrains.annotations.Nullable MergedFixtureBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TYPE) == ChestType.SINGLE ? null : new MergedFixtureBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(TYPE) == ChestType.SINGLE ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        ChestType type = ChestType.SINGLE;
        if (candidatePartnerFacing(context, facing.getClockWise()) == facing) {
            type = ChestType.LEFT;
        } else if (candidatePartnerFacing(context, facing.getCounterClockWise()) == facing) {
            type = ChestType.RIGHT;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(TYPE, type);
    }

    private @org.jetbrains.annotations.Nullable Direction candidatePartnerFacing(BlockPlaceContext context, Direction direction) {
        BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(direction));
        return neighbor.is(this) && neighbor.getValue(TYPE) == ChestType.SINGLE
                ? neighbor.getValue(FACING) : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighbor, RandomSource random) {
        ChestType type = state.getValue(TYPE);
        if (type != ChestType.SINGLE && getConnectedDirection(state) == direction) {
            boolean validPartner = neighbor.is(this)
                    && neighbor.getValue(FACING) == state.getValue(FACING)
                    && neighbor.getValue(TYPE) == type.getOpposite()
                    && getConnectedDirection(neighbor) == direction.getOpposite();
            if (!validPartner) return state.setValue(TYPE, ChestType.SINGLE);
        }

        if (type == ChestType.SINGLE && neighbor.is(this)
                && neighbor.getValue(FACING) == state.getValue(FACING)
                && neighbor.getValue(TYPE) != ChestType.SINGLE
                && getConnectedDirection(neighbor) == direction.getOpposite()) {
            return state.setValue(TYPE, neighbor.getValue(TYPE).getOpposite());
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    public static Direction getConnectedDirection(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(TYPE) == ChestType.LEFT
                ? facing.getClockWise() : facing.getCounterClockWise();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState rotated = rotate(state, mirror.getRotation(state.getValue(FACING)));
        return mirror == Mirror.NONE ? rotated : rotated.setValue(TYPE, rotated.getValue(TYPE).getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE);
    }
}
