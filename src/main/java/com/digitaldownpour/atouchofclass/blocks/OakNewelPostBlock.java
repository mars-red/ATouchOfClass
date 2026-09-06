package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import javax.annotation.Nullable;

public class OakNewelPostBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<OakNewelPostBlock> CODEC=simpleCodec(OakNewelPostBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
    public OakNewelPostBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HALF,DoubleBlockHalf.LOWER));
    }
    @Override protected MapCodec<? extends OakNewelPostBlock> codec(){return CODEC;}

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos=context.getClickedPos();
        Level level=context.getLevel();
        if(context.getClickedFace()!=Direction.UP || pos.getY()>=level.getMaxY()
                || !level.getBlockState(pos.above()).canBeReplaced(context)) return null;
        double x=context.getClickLocation().x-pos.getX(),z=context.getClickLocation().z-pos.getZ();
        Direction facing=z>=0.5?(x>=0.5?Direction.NORTH:Direction.EAST):(x>=0.5?Direction.WEST:Direction.SOUTH);
        BlockState state=defaultBlockState().setValue(FACING,facing);
        return state.canSurvive(level,pos) && level.isUnobstructed(state.setValue(HALF,DoubleBlockHalf.UPPER),pos.above(),CollisionContext.empty())?state:null;
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack) {
        level.setBlock(pos.above(),state.setValue(HALF,DoubleBlockHalf.UPPER),Block.UPDATE_ALL);
    }
    @Override protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos) {
        BlockState below=level.getBlockState(pos.below());
        if(state.getValue(HALF)==DoubleBlockHalf.UPPER)
            return below.is(this)&&below.getValue(HALF)==DoubleBlockHalf.LOWER&&below.getValue(FACING)==state.getValue(FACING);
        return below.isFaceSturdy(level,pos.below(),Direction.UP);
    }
    @Override protected BlockState updateShape(BlockState state,LevelReader level,ScheduledTickAccess ticks,BlockPos pos,
            Direction direction,BlockPos neighborPos,BlockState neighbor,RandomSource random) {
        return state.canSurvive(level,pos)?state:Blocks.AIR.defaultBlockState();
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moved) {
        if(!replacement.is(this)) {
            BlockPos otherPos=state.getValue(HALF)==DoubleBlockHalf.LOWER?pos.above():pos.below();
            BlockState other=level.getBlockState(otherPos);
            if(other.is(this)&&other.getValue(HALF)!=state.getValue(HALF)&&other.getValue(FACING)==state.getValue(FACING))
                level.destroyBlock(otherPos,false);
        }
        super.onRemove(state,level,pos,replacement,moved);
    }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return NewelPostGeometry.shape(state.getValue(FACING),state.getValue(HALF));
    }
    @Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState state,Mirror mirror) {
        Direction facing=state.getValue(FACING);
        // FACING encodes a quadrant, so mirroring swaps corner pairs rather than compass normals.
        if(mirror==Mirror.LEFT_RIGHT) facing=switch(facing){case NORTH->Direction.WEST;case WEST->Direction.NORTH;case EAST->Direction.SOUTH;default->Direction.EAST;};
        if(mirror==Mirror.FRONT_BACK) facing=switch(facing){case NORTH->Direction.EAST;case EAST->Direction.NORTH;case SOUTH->Direction.WEST;default->Direction.SOUTH;};
        return state.setValue(FACING,facing);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,HALF);}
}
