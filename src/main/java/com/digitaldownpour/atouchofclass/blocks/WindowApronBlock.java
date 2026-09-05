package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class WindowApronBlock extends CasingBlock {
    public static final EnumProperty<Cap> CAP = EnumProperty.create("cap", Cap.class);
    public static final BooleanProperty BASEBOARD = BooleanProperty.create("baseboard");
    private final Map<BlockState, VoxelShape> apronShapes = new HashMap<>();

    public WindowApronBlock(Properties properties) {
        super(properties, 5, 8);
        registerDefaultState(defaultBlockState().setValue(EDGE, Edge.TOP).setValue(CAP, Cap.NONE).setValue(BASEBOARD, false).setValue(BaseboardReturn.FACING, Direction.UP));
        for (BlockState state : stateDefinition.getPossibleStates()) {
            Direction facing = state.getValue(FACING);
            Direction top = edgeDirection(facing, state.getValue(EDGE));
            Cap cap = state.getValue(CAP);
            Direction outward = cap == Cap.LEFT ? right(facing, top).getOpposite() : right(facing, top);
            VoxelShape shape = Shapes.or(
                    tier(facing, top, outward, cap == Cap.NONE ? 16 : 9, 5, 0, 2),
                    tier(facing, top, outward, cap == Cap.NONE ? 16 : 7, 3, 2, 6),
                    tier(facing, top, outward, cap == Cap.NONE ? 16 : 6, 2, 6, 8));
            if (state.getValue(BASEBOARD) && facing.getAxis().isHorizontal()) {
                shape = Shapes.or(shape, tier(facing, Direction.DOWN,
                        facing.getClockWise(), 16, 2, 0, 4));
            }
            apronShapes.put(state, Shapes.or(shape, BaseboardReturn.shape(state.getValue(BaseboardReturn.FACING))));
        }
    }

    private static VoxelShape tier(Direction facing, Direction top, Direction outward, int width,
                                   int depth, int start, int end) {
        double[] min = {0, 0, 0};
        double[] max = {16, 16, 16};
        int depthAxis = axis(facing);
        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) max[depthAxis] = depth;
        else min[depthAxis] = 16 - depth;
        int heightAxis = axis(top);
        if (top.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            min[heightAxis] = 16 - end;
            max[heightAxis] = 16 - start;
        } else {
            min[heightAxis] = start;
            max[heightAxis] = end;
        }
        // The cap starts at the shared block boundary and extends away from the base apron.
        int widthAxis = axis(outward);
        if (outward.getAxisDirection() == Direction.AxisDirection.POSITIVE) max[widthAxis] = width;
        else min[widthAxis] = 16 - width;
        return Block.box(min[0], min[1], min[2], max[0], max[1], max[2]);
    }

    private static int axis(Direction direction) {
        return switch (direction.getAxis()) { case X -> 0; case Y -> 1; case Z -> 2; };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockPos target = context.replacingClickedOnBlock() ? context.getClickedPos()
                : context.getClickedPos().relative(clickedFace.getOpposite());
        BlockState neighbor = context.getLevel().getBlockState(target);
        if (neighbor.is(this)) {
            Direction facing = neighbor.getValue(FACING);
            Direction top = edgeDirection(facing, neighbor.getValue(EDGE));
            Direction right = right(facing, top);
            if (clickedFace == right || clickedFace == right.getOpposite()) {
                // A cap is terminal: placing beyond its inset end would leave a gap.
                if (neighbor.getValue(CAP) != Cap.NONE) return null;
                return withBaseboard(neighbor.setValue(CAP, clickedFace == right ? Cap.RIGHT : Cap.LEFT),
                        context.getLevel(), context.getClickedPos());
            }
        }
        // Click position never changes the apron edge; it always starts at the face's top.
        return withBaseboard(defaultBlockState().setValue(FACING, clickedFace).setValue(EDGE, Edge.TOP),
                context.getLevel(), context.getClickedPos());
    }

    private BlockState withBaseboard(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction returnFacing = BaseboardReturn.find(level, pos, facing);
        boolean connected = returnFacing != Direction.UP;
        if (facing.getAxis().isHorizontal()) {
            Direction side = facing.getClockWise();
            connected = connected || hasBaseboardAlongApron(level, pos, side, state)
                    || hasBaseboardAlongApron(level, pos, side.getOpposite(), state);
        }
        return state.setValue(BASEBOARD, connected).setValue(BaseboardReturn.FACING, returnFacing);
    }

    private boolean hasBaseboardAlongApron(BlockGetter level, BlockPos pos, Direction side, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos.MutableBlockPos cursor = pos.mutable().move(side);
        // Follow the connected apron to a real baseboard. Checking only the neighbors'
        // BASEBOARD flags would let aprons keep each other enabled after removal.
        while (true) {
            BlockState neighbor = level.getBlockState(cursor);
            if (neighbor.getBlock() instanceof BaseboardBlock) {
                return neighbor.getValue(BaseboardBlock.FACING) == facing;
            }
            if (!neighbor.is(this) || neighbor.getValue(FACING) != facing
                    || neighbor.getValue(EDGE) != state.getValue(EDGE)) {
                return false;
            }
            if (BaseboardReturn.find(level, cursor, facing) != Direction.UP) return true;
            cursor.move(side);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return withBaseboard(state, level, pos);
    }

    private static Direction right(Direction facing, Direction top) {
        // Top cross outward-facing normal gives the width direction in the apron's own frame.
        return Direction.getNearest(
                top.getStepY() * facing.getStepZ() - top.getStepZ() * facing.getStepY(),
                top.getStepZ() * facing.getStepX() - top.getStepX() * facing.getStepZ(),
                top.getStepX() * facing.getStepY() - top.getStepY() * facing.getStepX(), Direction.EAST);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CAP, BASEBOARD, BaseboardReturn.FACING);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = super.mirror(state, mirror).setValue(BaseboardReturn.FACING,
                mirror.mirror(state.getValue(BaseboardReturn.FACING)));
        if (state.getValue(CAP) == Cap.NONE) return mirrored;
        Direction oldRight = right(state.getValue(FACING), edgeDirection(state.getValue(FACING), state.getValue(EDGE)));
        Direction outward = state.getValue(CAP) == Cap.RIGHT ? oldRight : oldRight.getOpposite();
        Direction newRight = right(mirrored.getValue(FACING), edgeDirection(mirrored.getValue(FACING), mirrored.getValue(EDGE)));
        return mirrored.setValue(CAP, mirror.mirror(outward) == newRight ? Cap.RIGHT : Cap.LEFT);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return super.rotate(state, rotation).setValue(BaseboardReturn.FACING,
                rotation.rotate(state.getValue(BaseboardReturn.FACING)));
    }

    public enum Cap implements StringRepresentable {
        NONE("none"), LEFT("left"), RIGHT("right");
        private final String name;
        Cap(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return apronShapes.get(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return apronShapes.get(state);
    }
}
