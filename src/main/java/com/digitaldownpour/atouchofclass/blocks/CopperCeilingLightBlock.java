package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Low-profile copper ceiling lights in bowl and coffered styles. */
public class CopperCeilingLightBlock extends Block {
    public static final MapCodec<CopperCeilingLightBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Codec.BOOL.fieldOf("coffered").forGetter(CopperCeilingLightBlock::isCoffered),
                    propertiesCodec()).apply(instance, CopperCeilingLightBlock::new));

    private static final VoxelShape BOWL_SHAPE = Shapes.or(
            Block.box(3, 15, 3, 13, 16, 13),
            Block.box(1, 12.5, 1, 15, 15, 15),
            Block.box(2, 8, 2, 14, 12.5, 14),
            Block.box(4, 6, 4, 12, 8, 12),
            Block.box(7, 5, 7, 9, 6, 9));
    private static final VoxelShape COFFERED_SHAPE = Shapes.or(
            Block.box(3, 14.5, 3, 13, 16, 13),
            Block.box(1, 11.5, 1, 15, 14.5, 15),
            Block.box(2, 7, 2, 14, 11.5, 14),
            Block.box(7, 6, 7, 9, 7, 9));

    private final boolean coffered;

    public CopperCeilingLightBlock(boolean coffered, Properties properties) {
        super(properties);
        this.coffered = coffered;
    }

    public boolean isCoffered() {
        return coffered;
    }

    @Override
    protected MapCodec<? extends CopperCeilingLightBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return context.getClickedFace() == Direction.DOWN
                && state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return direction == Direction.UP && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return coffered ? COFFERED_SHAPE : BOWL_SHAPE;
    }
}
