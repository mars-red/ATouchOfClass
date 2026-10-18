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
import net.minecraft.server.level.ServerLevel;

/** A one-block-wide, three-block-tall decorative lamp post. */
public class CopperLampPostBlock extends Block {
    public static final MapCodec<CopperLampPostBlock> CODEC = simpleCodec(CopperLampPostBlock::new);
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2);

    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 3.5, 14),
            Block.box(4, 3.5, 4, 12, 7, 12),
            Block.box(5.5, 7, 5.5, 10.5, 13.5, 10.5),
            Block.box(7, 13.5, 7, 9, 16, 9));
    private static final VoxelShape MIDDLE_SHAPE = Block.box(6.5, 0, 6.5, 9.5, 16, 9.5);
    private static final VoxelShape TOP_SHAPE = Shapes.or(
            Block.box(7, 0, 7, 9, 5.5, 9),
            Block.box(5, 4, 5, 11, 7, 11),
            Block.box(3, 7, 3, 13, 15, 13),
            Block.box(7, 15, 7, 9, 16, 9));

    public CopperLampPostBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 0));
    }

    @Override
    protected MapCodec<? extends CopperLampPostBlock> codec() {
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
        if (!level.isClientSide() && state.getValue(PART) == 0) {
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
        return below.getBlock() instanceof CopperLampPostBlock
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
        if (level.isClientSide() || oldState.is(state.getBlock())
                || !(oldState.getBlock() instanceof CopperLampPostBlock)) {
            return;
        }

        // Waxing, scraping, and oxidation replace one section at a time. Carry the new
        // finish across the other two sections before neighbor updates can split the post.
        BlockPos basePos = pos.below(state.getValue(PART));
        for (int part = 0; part < 3; part++) {
            BlockPos partPos = basePos.above(part);
            BlockState other = level.getBlockState(partPos);
            if (other.getBlock() instanceof CopperLampPostBlock
                    && other.getValue(PART) == part
                    && !other.is(state.getBlock())) {
                level.setBlock(partPos, state.getBlock().defaultBlockState().setValue(PART, part),
                        Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockState replacement = level.getBlockState(pos);
        if (!level.isClientSide() && !(replacement.getBlock() instanceof CopperLampPostBlock)) {
            BlockPos basePos = pos.below(state.getValue(PART));
            for (int part = 0; part < 3; part++) {
                BlockPos partPos = basePos.above(part);
                if (partPos.equals(pos)) continue;
                BlockState other = level.getBlockState(partPos);
                if (other.getBlock() instanceof CopperLampPostBlock && other.getValue(PART) == part) {
                    level.destroyBlock(partPos, false);
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
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
