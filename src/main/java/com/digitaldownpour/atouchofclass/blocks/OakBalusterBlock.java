package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

public class OakBalusterBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<OakBalusterBlock> CODEC=simpleCodec(OakBalusterBlock::new);
    public OakBalusterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(OakBalustradeBlock.SHAPE,StairsShape.STRAIGHT));
    }
    @Override protected MapCodec<? extends OakBalusterBlock> codec() { return CODEC; }

    static boolean supported(LevelReader level,BlockPos pos) {
        BlockPos below=pos.below();BlockState support=level.getBlockState(below);
        return support.getBlock() instanceof OakBalusterBlock || support.isFaceSturdy(level,below,Direction.UP);
    }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        if(context.getClickedFace()!=Direction.UP) return null;
        BlockState below=context.getLevel().getBlockState(context.getClickedPos().below());
        Direction facing=below.getBlock() instanceof OakBalusterBlock?below.getValue(FACING):context.getHorizontalDirection().getOpposite();
        BlockState state=defaultBlockState().setValue(FACING,facing);
        return supported(context.getLevel(),context.getClickedPos())?aligned(state,context.getLevel(),context.getClickedPos()):null;
    }
    private BlockState aligned(BlockState state,BlockGetter level,BlockPos pos) {
        BlockState above=level.getBlockState(pos.above());
        if(above.getBlock() instanceof OakBalustradeBlock || above.getBlock() instanceof OakBalusterBlock)
            return state.setValue(FACING,above.getValue(FACING)).setValue(OakBalustradeBlock.SHAPE,above.getValue(OakBalustradeBlock.SHAPE));
        return state.setValue(OakBalustradeBlock.SHAPE,StairsShape.STRAIGHT);
    }
    @Override protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos) { return supported(level,pos); }
    @Override protected BlockState updateShape(BlockState state,LevelReader level,ScheduledTickAccess ticks,BlockPos pos,
            Direction direction,BlockPos neighborPos,BlockState neighbor,RandomSource random) {
        return supported(level,pos)?aligned(state,level,pos):Blocks.AIR.defaultBlockState();
    }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return FloorOakGeometry.baluster(state.getValue(OakBalustradeBlock.SHAPE),state.getValue(FACING));
    }
    static StairsShape mirrored(StairsShape shape) {
        return switch(shape) {
            case INNER_LEFT->StairsShape.INNER_RIGHT;case INNER_RIGHT->StairsShape.INNER_LEFT;
            case OUTER_LEFT->StairsShape.OUTER_RIGHT;case OUTER_RIGHT->StairsShape.OUTER_LEFT;default->StairsShape.STRAIGHT;
        };
    }
    @Override protected BlockState rotate(BlockState state,Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state,Mirror mirror) {
        return state.setValue(FACING,mirror.mirror(state.getValue(FACING))).setValue(OakBalustradeBlock.SHAPE,
                mirror==Mirror.NONE?state.getValue(OakBalustradeBlock.SHAPE):mirrored(state.getValue(OakBalustradeBlock.SHAPE)));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,OakBalustradeBlock.SHAPE); }
}
