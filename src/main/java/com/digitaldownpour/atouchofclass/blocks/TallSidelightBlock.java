package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerLevel;

public class TallSidelightBlock extends Block {
    private static final VoxelShape NORTH_SHAPE =
            Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape WEST_SHAPE =
            Block.box(0, 0, 0, 3, 16, 16);
    private static final VoxelShape SOUTH_SHAPE =
            Block.box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape EAST_SHAPE =
            Block.box(13, 0, 0, 16, 16, 16);

    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2); // 0 = bottom, 1 = middle, 2 = top
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TallSidelightBlock(Properties blockProps) {
        super(blockProps);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, 0)
                .setValue(FACING, Direction.NORTH)
        );
    }

    private static VoxelShape shapeForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> Shapes.block();
        };
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();

        return super.getStateForPlacement(context)
                .setValue(FACING, facing)
                .setValue(PART, 0);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isClientSide() && state.getValue(PART) == 0) {
            Direction facing = state.getValue(FACING);
            // Place middle part above
            world.setBlock(
                    pos.above(),
                    state.setValue(PART, 1)
                            .setValue(FACING, state.getValue(FACING)),
                    3);
            // Place top part above middle
            world.setBlock(
                    pos.above(2),
                    state.setValue(PART, 2)
                            .setValue(FACING, state.getValue(FACING)),
                    3);
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
        BlockState newState = world.getBlockState(pos);
        if (!world.isClientSide() && state.getBlock() != newState.getBlock()) {
            int part = state.getValue(PART);
            BlockPos basePos = pos.below(part); // Calculate bottom position

            for (int i = 0; i < 3; i++) {
                if (i == part) continue;
                BlockPos p = basePos.above(i);
                BlockState otherPart = world.getBlockState(p);
                if (otherPart.is(this) && otherPart.getValue(PART) == i) {
                    // Only the originally broken section rolls loot; companion sections never do.
                    world.destroyBlock(p, false);
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, world, pos, isMoving);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int part = state.getValue(PART);
        if (part == 0) {
            return level.getBlockState(pos.below()).isSolid();
        } else {
            BlockState belowState = level.getBlockState(pos.below());
            return belowState.is(this)
                    && belowState.getValue(PART) == part - 1;
        }
    }
}
