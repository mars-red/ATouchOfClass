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
import net.minecraft.world.level.block.state.properties.Half;
import com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

/** Floor-supported, level handrail with a single wooden post. */
public class OakBalustradeBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<OakBalustradeBlock> CODEC = simpleCodec(OakBalustradeBlock::new);
    public static final BooleanProperty TOP_JOIN = BooleanProperty.create("top_join");
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final EnumProperty<Slope> SLOPE = EnumProperty.create("slope", Slope.class,
            Slope.FLAT, Slope.UPPER_LEFT, Slope.UPPER_RIGHT, Slope.LOWER_LEFT, Slope.LOWER_RIGHT,
            Slope.BOTTOM_LEFT, Slope.BOTTOM_RIGHT);

    public OakBalustradeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SHAPE,StairsShape.STRAIGHT).setValue(SLOPE,Slope.FLAT).setValue(TOP_JOIN,false));
    }

    @Override protected MapCodec<? extends OakBalustradeBlock> codec() { return CODEC; }

    @Nullable @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked=context.getClickedFace();
        BlockState source=context.getLevel().getBlockState(context.getClickedPos().relative(clicked.getOpposite()));
        boolean extension=source.is(this)&&source.getValue(SLOPE)!=Slope.FLAT;
        if(clicked!=Direction.UP && !(extension && clicked.getAxis().isHorizontal()
                && clicked.getAxis()!=source.getValue(FACING).getAxis())) return null;
        BlockState below=context.getLevel().getBlockState(context.getClickedPos().below());
        Direction facing=extension?source.getValue(FACING):below.getBlock() instanceof OakBalusterBlock ? below.getValue(FACING) : context.getHorizontalDirection().getOpposite();
        // On stairs, select the nearest side of the stair run, independent of looking uphill/downhill.
        if(!extension && below.getBlock() instanceof StairBlock && below.getValue(StairBlock.HALF)==Half.BOTTOM) {
            Direction ascent=below.getValue(StairBlock.FACING);
            if(facing.getAxis()==ascent.getAxis()) facing=ascent.getCounterClockWise();
        }
        BlockState state=defaultBlockState().setValue(FACING,facing);
        return state.canSurvive(context.getLevel(),context.getClickedPos()) ? connected(state,context.getLevel(),context.getClickedPos()) : null;
    }

    @Override protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos) {
        return OakBalusterBlock.supported(level,pos) || slope(level,pos,state.getValue(FACING))!=Slope.FLAT;
    }

    private boolean rail(BlockState state) { return state.is(this)&&state.getValue(SLOPE)==Slope.FLAT; }

    private Slope stairSlope(BlockGetter level,BlockPos pos,Direction facing) {
        BlockState stair=level.getBlockState(pos.below());
        if(!(stair.getBlock() instanceof StairBlock) || stair.getValue(StairBlock.HALF)!=Half.BOTTOM) return Slope.FLAT;
        // Require the entire 2x2 post footprint to meet the stair's upper tread,
        // including inner/outer stair shapes rather than assuming a full half-tread.
        Direction ascent=stair.getValue(StairBlock.FACING),right=facing.getClockWise();
        if(ascent.getAxis()!=right.getAxis()) return Slope.FLAT;
        Slope result=ascent==right?Slope.UPPER_LEFT:Slope.UPPER_RIGHT;
        double x0=result==Slope.UPPER_LEFT?9:5,x1=x0+2,z0=11.75,z1=13.75;
        int turns=switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        for(int i=0;i<turns;i++){double n0=16-z1,n1=16-z0;z0=x0;z1=x1;x0=n0;x1=n1;}
        var contact=Block.box(x0,15.99,z0,x1,16,z1);
        return Shapes.joinIsNotEmpty(contact,stair.getCollisionShape(level,pos.below()),
                BooleanOp.ONLY_FIRST)?Slope.FLAT:result;
    }

    private boolean anchor(BlockGetter level,BlockPos pos,Direction facing,Slope expected) {
        BlockState other=level.getBlockState(pos);
        return other.is(this)&&other.getValue(FACING)==facing&&stairSlope(level,pos,facing)==expected;
    }

    private Slope slope(BlockGetter level,BlockPos pos,Direction facing) {
        Slope direct=stairSlope(level,pos,facing);
        if(direct!=Slope.FLAT) return direct;
        Direction right=facing.getClockWise();
        // The landing sits below the post-free diagonal half. Bend its grip
        // upward instead of leaving a full horizontal rail beneath the slope.
        BlockState above=level.getBlockState(pos.above());
        if (above.is(this) && above.getValue(FACING)==facing && OakBalusterBlock.supported(level,pos)) {
            boolean bottomLeft=anchor(level,pos.above().relative(right),facing,Slope.UPPER_LEFT);
            boolean bottomRight=anchor(level,pos.above().relative(right.getOpposite()),facing,Slope.UPPER_RIGHT);
            if (bottomLeft != bottomRight) return bottomLeft ? Slope.BOTTOM_LEFT : Slope.BOTTOM_RIGHT;
        }
        // A post-free half must touch a stair-supported half at an actual rail interface.
        // Read its physical stair support, avoiding mutual support between floating pieces.
        boolean rising=anchor(level,pos.below(),facing,Slope.UPPER_LEFT)
                ||anchor(level,pos.relative(right),facing,Slope.UPPER_LEFT);
        boolean falling=anchor(level,pos.below(),facing,Slope.UPPER_RIGHT)
                ||anchor(level,pos.relative(right.getOpposite()),facing,Slope.UPPER_RIGHT);
        return rising==falling?Slope.FLAT:rising?Slope.LOWER_RIGHT:Slope.LOWER_LEFT;
    }

    private boolean continuing(BlockGetter level,BlockPos pos,Direction side,Direction facing) {
        BlockState other=level.getBlockState(pos.relative(side));
        return rail(other)&&other.getValue(FACING)==facing;
    }

    private BlockState connected(BlockState state,BlockGetter level,BlockPos pos) {
        Slope slope=slope(level,pos,state.getValue(FACING));
        Direction uphill=slope==Slope.UPPER_LEFT?state.getValue(FACING).getClockWise():state.getValue(FACING).getCounterClockWise();
        BlockState above=level.getBlockState(pos.above());
        boolean continues=above.is(this)&&above.getValue(FACING)==state.getValue(FACING);
        boolean top=(slope==Slope.UPPER_LEFT||slope==Slope.UPPER_RIGHT)&&!continues
                &&!level.getBlockState(pos.relative(uphill)).isAir();
        state=state.setValue(SLOPE,slope).setValue(TOP_JOIN,top);
        if(slope!=Slope.FLAT) return state.setValue(SHAPE,StairsShape.STRAIGHT);
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
        if(!level.isClientSide()&&(!old.is(this)||old.getValue(FACING)!=state.getValue(FACING))) {
            BlockState updated=connected(state,level,pos);
            if(updated!=state) level.setBlock(pos,updated,Block.UPDATE_ALL);
        }
    }

    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        if(state.getValue(SLOPE)!=Slope.FLAT) return SlopedBalustradeGeometry.shape(state.getValue(SLOPE),state.getValue(FACING),state.getValue(TOP_JOIN));
        return FloorOakGeometry.railing(state.getValue(SHAPE),state.getValue(FACING));
    }

    @Override protected BlockState rotate(BlockState state,Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state,Mirror mirror) {
        return state.setValue(FACING,mirror.mirror(state.getValue(FACING)))
                .setValue(SLOPE,mirror==Mirror.NONE?state.getValue(SLOPE):state.getValue(SLOPE).reversed())
                .setValue(SHAPE,mirror==Mirror.NONE?state.getValue(SHAPE):OakBalusterBlock.mirrored(state.getValue(SHAPE)));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,SHAPE,SLOPE,TOP_JOIN); }
}
