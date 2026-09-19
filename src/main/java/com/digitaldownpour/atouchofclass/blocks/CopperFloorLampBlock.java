package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A centered-shade indoor floor lamp with a visual height of two and a half blocks. */
public class CopperFloorLampBlock extends Block {
    public static final MapCodec<CopperFloorLampBlock> CODEC = simpleCodec(CopperFloorLampBlock::new);
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2);

    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Block.box(3, 0, 3, 13, 2, 13),
            Block.box(4, 2, 4, 12, 4, 12),
            Block.box(5.5, 4, 5.5, 10.5, 7, 10.5),
            Block.box(7, 7, 7, 9, 16, 9));
    private static final VoxelShape MIDDLE_SHAPE = Shapes.or(
            Block.box(7, 0, 7, 9, 14, 9),
            Block.box(6, 13, 6, 10, 15.5, 10),
            Block.box(7, 15.5, 7, 9, 16, 9));
    private static final VoxelShape TOP_SHAPE = Shapes.or(
            Block.box(7, 0, 7, 9, 2, 9),
            Block.box(1.5, 0.5, 1.5, 14.5, 2, 14.5),
            Block.box(2.5, 2, 2.5, 13.5, 4, 13.5),
            Block.box(4, 4, 4, 12, 6, 12),
            Block.box(5, 6, 5, 11, 8, 11));

    public CopperFloorLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 0));
    }

    @Override
    protected MapCodec<? extends CopperFloorLampBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (context.getClickedFace() != Direction.UP
                || pos.getY() > level.getMaxY() - 2
                || !level.getBlockState(pos.above()).canBeReplaced(context)
                || !level.getBlockState(pos.above(2)).canBeReplaced(context)) {
            return null;
        }

        BlockState state = defaultBlockState();
        return state.canSurvive(level, pos) ? state : null;
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
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int part = state.getValue(PART);
        if (part == 0) {
            BlockPos supportPos = pos.below();
            return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP);
        }

        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof CopperFloorLampBlock
                && below.getValue(PART) == part - 1;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos,
                           BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide || oldState.is(state.getBlock())
                || !(oldState.getBlock() instanceof CopperFloorLampBlock)) {
            return;
        }

        BlockPos basePos = pos.below(state.getValue(PART));
        for (int part = 0; part < 3; part++) {
            BlockPos partPos = basePos.above(part);
            BlockState other = level.getBlockState(partPos);
            if (other.getBlock() instanceof CopperFloorLampBlock
                    && other.getValue(PART) == part
                    && !other.is(state.getBlock())) {
                level.setBlock(partPos, state.getBlock().defaultBlockState().setValue(PART, part),
                        Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState replacement, boolean movedByPiston) {
        if (!level.isClientSide && !(replacement.getBlock() instanceof CopperFloorLampBlock)) {
            BlockPos basePos = pos.below(state.getValue(PART));
            for (int part = 0; part < 3; part++) {
                BlockPos partPos = basePos.above(part);
                if (partPos.equals(pos)) continue;
                BlockState other = level.getBlockState(partPos);
                if (other.getBlock() instanceof CopperFloorLampBlock && other.getValue(PART) == part) {
                    level.destroyBlock(partPos, false);
                }
            }
        }
        super.onRemove(state, level, pos, replacement, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return switch (state.getValue(PART)) {
            case 0 -> BASE_SHAPE;
            case 1 -> MIDDLE_SHAPE;
            default -> TOP_SHAPE;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }
}
