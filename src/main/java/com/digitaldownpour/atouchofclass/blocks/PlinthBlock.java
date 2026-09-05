package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Shares Casing's placement rules and fills its open side beside baseboard. */
public class PlinthBlock extends CasingBlock {
    public static final BooleanProperty BASEBOARD = BooleanProperty.create("baseboard");

    public PlinthBlock(Properties properties) {
        super(properties, 4, 8);
        registerDefaultState(defaultBlockState().setValue(BASEBOARD, false));
    }

    private BlockState withBaseboard(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Edge edge = state.getValue(EDGE);
        boolean connected = false;
        if (facing.getAxis().isHorizontal() && (edge == Edge.LEFT || edge == Edge.RIGHT)) {
            Direction gap = edgeDirection(facing, edge).getOpposite();
            BlockState neighbor = level.getBlockState(pos.relative(gap));
            connected = (neighbor.getBlock() instanceof BaseboardBlock
                    && neighbor.getValue(BaseboardBlock.FACING) == facing)
                    || (neighbor.getBlock() instanceof WindowApronBlock
                    && neighbor.getValue(WindowApronBlock.FACING) == facing
                    && neighbor.getValue(WindowApronBlock.BASEBOARD));
        }
        return state.setValue(BASEBOARD, connected);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withBaseboard(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return withBaseboard(state, level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BASEBOARD);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = super.getShape(state, level, pos, context);
        if (!state.getValue(BASEBOARD) || state.getValue(FACING).getAxis().isVertical()
                || (state.getValue(EDGE) != Edge.LEFT && state.getValue(EDGE) != Edge.RIGHT)) return shape;
        VoxelShape strip = switch (state.getValue(FACING)) {
            case SOUTH -> Block.box(0, 0, 0, 16, 4, 2);
            case NORTH -> Block.box(0, 0, 14, 16, 4, 16);
            case EAST -> Block.box(0, 0, 0, 2, 4, 16);
            case WEST -> Block.box(14, 0, 0, 16, 4, 16);
            default -> Shapes.empty();
        };
        return Shapes.or(shape, strip);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
