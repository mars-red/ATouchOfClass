package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A decorative bunch above a planter; its base reaches two units into the planter's soil. */
public class FlowerBunchBlock extends HorizontalDirectionalBlock {
    // New planter families can opt into planting without changing the item or flower classes.
    public static final TagKey<Block> PLANTERS = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "flower_bunch_planters"));
    private final int height;

    public FlowerBunchBlock(int height, Properties properties) {
        super(properties);
        this.height = height;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState support = context.getLevel().getBlockState(pos.below());
        if (!support.is(PLANTERS)) return null;
        Direction facing = support.hasProperty(FACING) ? support.getValue(FACING) : context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(PLANTERS);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (!canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        BlockState support = level.getBlockState(pos.below());
        return support.hasProperty(FACING) ? state.setValue(FACING, support.getValue(FACING)) : state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> box(0, -2, 0, 16, height - 2, 10);
            case WEST -> box(6, -2, 0, 16, height - 2, 16);
            case NORTH -> box(0, -2, 6, 16, height - 2, 16);
            case EAST -> box(0, -2, 0, 10, height - 2, 16);
            default -> throw new IllegalStateException("Flower bunches must face horizontally");
        };
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
