package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Common placement and cleanup behavior for one-block-wide, three-block-tall glass panels. */
public class ThreeHighGlassPanelBlock extends Block {
    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape EAST_SHAPE = Block.box(15, 0, 0, 16, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 15, 16, 16, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 0, 1, 16, 16);

    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ThreeHighGlassPanelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(PART, 0)
                .setValue(FACING, Direction.NORTH));
    }

    protected static VoxelShape shapeForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> Shapes.block();
        };
    }

    protected VoxelShape shapeForState(BlockState state) {
        return shapeForFacing(state.getValue(FACING));
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                            CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (pos.getY() > context.getLevel().getMaxY() - 2
                || !context.getLevel().getBlockState(pos.above()).canBeReplaced(context)
                || !context.getLevel().getBlockState(pos.above(2)).canBeReplaced(context)) {
            return null;
        }

        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(PART, 0);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && state.getValue(PART) == 0) {
            level.setBlock(pos.above(), state.setValue(PART, 1), Block.UPDATE_ALL);
            level.setBlock(pos.above(2), state.setValue(PART, 2), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState replacement, boolean moved) {
        if (!level.isClientSide && !replacement.is(this)) {
            int part = state.getValue(PART);
            BlockPos basePos = pos.below(part);
            for (int i = 0; i < 3; i++) {
                BlockPos partPos = basePos.above(i);
                if (partPos.equals(pos)) continue;
                BlockState other = level.getBlockState(partPos);
                if (other.is(this)
                        && other.getValue(PART) == i
                        && other.getValue(FACING) == state.getValue(FACING)) {
                    level.destroyBlock(partPos, false);
                }
            }
        }
        super.onRemove(state, level, pos, replacement, moved);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int part = state.getValue(PART);
        if (part == 0) {
            BlockPos supportPos = pos.below();
            BlockState support = level.getBlockState(supportPos);
            return support.getBlock() instanceof StairBlock
                    || support.isFaceSturdy(level, supportPos, Direction.UP);
        }
        BlockState below = level.getBlockState(pos.below());
        return below.is(this)
                && below.getValue(PART) == part - 1
                && below.getValue(FACING) == state.getValue(FACING);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }
}
