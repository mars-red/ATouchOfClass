package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** Floor-supported, level handrail with a single wooden post. */
public class OakBalustradeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<OakBalustradeBlock> CODEC = simpleCodec(OakBalustradeBlock::new);
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;

    public OakBalustradeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SHAPE,StairsShape.STRAIGHT));
    }

    @Override protected MapCodec<? extends OakBalustradeBlock> codec() { return CODEC; }

    @Nullable @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if(context.getClickedFace()!=Direction.UP) return null;
        BlockState below=context.getLevel().getBlockState(context.getClickedPos().below());
        Direction facing=below.getBlock() instanceof OakBalusterBlock ? below.getValue(FACING) : context.getHorizontalDirection().getOpposite();
        BlockState state=defaultBlockState().setValue(FACING,facing);
        return state.canSurvive(context.getLevel(),context.getClickedPos()) ? connected(state,context.getLevel(),context.getClickedPos()) : null;
    }

    @Override protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos) {
        return OakBalusterBlock.supported(level,pos);
    }

    private boolean rail(BlockState state) { return state.is(this); }

    private boolean continuing(BlockGetter level,BlockPos pos,Direction side,Direction facing) {
        BlockState other=level.getBlockState(pos.relative(side));
        return rail(other)&&other.getValue(FACING)==facing;
    }

    private BlockState connected(BlockState state,BlockGetter level,BlockPos pos) {
        Direction facing=state.getValue(FACING),right=facing.getClockWise();
        BlockState back=level.getBlockState(pos.relative(facing.getOpposite()));
        if(rail(back)) {
            Direction other=back.getValue(FACING);
            if(other.getAxis()!=facing.getAxis()&&!continuing(level,pos,other,facing))
                return state.setValue(SHAPE,other==right?StairsShape.OUTER_LEFT:StairsShape.OUTER_RIGHT);
        }
        BlockState front=level.getBlockState(pos.relative(facing));
        if(rail(front)) {
            Direction other=front.getValue(FACING);
            if(other.getAxis()!=facing.getAxis()&&!continuing(level,pos,other.getOpposite(),facing))
                return state.setValue(SHAPE,other==right?StairsShape.INNER_LEFT:StairsShape.INNER_RIGHT);
        }
        return state.setValue(SHAPE,StairsShape.STRAIGHT);
    }

    @Override protected BlockState updateShape(BlockState state,LevelReader level,ScheduledTickAccess ticks,
            BlockPos pos,Direction direction,BlockPos neighborPos,BlockState neighbor,RandomSource random) {
        return state.canSurvive(level,pos)?connected(state,level,pos):Blocks.AIR.defaultBlockState();
    }

    @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moved) {
        super.onPlace(state,level,pos,old,moved);
        if(!level.isClientSide&&(!old.is(this)||old.getValue(FACING)!=state.getValue(FACING))) {
            BlockState updated=connected(state,level,pos);
            if(updated!=state) level.setBlock(pos,updated,Block.UPDATE_ALL);
        }
    }

    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return FloorOakGeometry.railing(state.getValue(SHAPE),state.getValue(FACING));
    }

    @Override protected BlockState rotate(BlockState state,Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state,Mirror mirror) {
        return state.setValue(FACING,mirror.mirror(state.getValue(FACING)))
                .setValue(SHAPE,mirror==Mirror.NONE?state.getValue(SHAPE):OakBalusterBlock.mirrored(state.getValue(SHAPE)));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,SHAPE); }
}
