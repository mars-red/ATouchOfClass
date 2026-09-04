package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CasingBlock extends Block {
    // Facing points out from the supporting face, toward the player.
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Edge> EDGE = EnumProperty.create("edge", Edge.class);
    private final java.util.Map<BlockState, VoxelShape> shapes = new java.util.HashMap<>();

    public CasingBlock(Properties properties) {
        this(properties, 3, 7);
    }

    protected CasingBlock(Properties properties, int depth, int width) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(EDGE, Edge.RIGHT));
        for (BlockState state : stateDefinition.getPossibleStates()) {
            Direction facing = state.getValue(FACING);
            Direction edge = edgeDirection(facing, state.getValue(EDGE));
            double[] min = {0, 0, 0};
            double[] max = {16, 16, 16};
            restrict(min, max, facing.getOpposite(), depth);
            restrict(min, max, edge, width);
            shapes.put(state, Block.box(min[0], min[1], min[2], max[0], max[1], max[2]));
        }
    }

    private static void restrict(double[] min, double[] max, Direction direction, int size) {
        int axis = switch (direction.getAxis()) { case X -> 0; case Y -> 1; case Z -> 2; };
        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) min[axis] = 16 - size;
        else max[axis] = size;
    }

    private static Direction up(Direction facing) {
        return switch (facing) { case UP -> Direction.NORTH; case DOWN -> Direction.SOUTH; default -> Direction.UP; };
    }

    private static Direction right(Direction facing) {
        return facing.getAxis().isVertical() ? Direction.EAST : facing.getCounterClockWise();
    }

    private static Direction edgeDirection(Direction facing, Edge edge) {
        return switch (edge) {
            case RIGHT -> right(facing);
            case LEFT -> right(facing).getOpposite();
            case TOP -> up(facing);
            case BOTTOM -> up(facing).getOpposite();
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        Vec3 hit = context.getClickLocation().subtract(Vec3.atCenterOf(context.getClickedPos()));
        // Project into the clicked face. Its normal coordinate does not affect edge selection.
        double horizontal = projection(hit, right(facing));
        double vertical = projection(hit, up(facing));
        Edge edge = Math.abs(horizontal) >= Math.abs(vertical)
                ? (horizontal >= 0 ? Edge.RIGHT : Edge.LEFT)
                : (vertical >= 0 ? Edge.TOP : Edge.BOTTOM);
        // On a floor/ceiling, an exactly centered click defaults to the player's right.
        if (horizontal == 0 && vertical == 0 && facing.getAxis().isVertical()) {
            edge = edgeForDirection(facing, context.getHorizontalDirection().getClockWise());
        }
        return defaultBlockState().setValue(FACING, facing).setValue(EDGE, edge);
    }

    private static double projection(Vec3 hit, Direction direction) {
        return hit.x * direction.getStepX() + hit.y * direction.getStepY() + hit.z * direction.getStepZ();
    }

    private static Edge edgeForDirection(Direction facing, Direction direction) {
        for (Edge edge : Edge.values()) if (edgeDirection(facing, edge) == direction) return edge;
        throw new IllegalArgumentException("Edge must lie in the casing face");
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        Direction facing = rotation.rotate(state.getValue(FACING));
        Direction edge = rotation.rotate(edgeDirection(state.getValue(FACING), state.getValue(EDGE)));
        return state.setValue(FACING, facing).setValue(EDGE, edgeForDirection(facing, edge));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction facing = mirror.mirror(state.getValue(FACING));
        Direction edge = mirror.mirror(edgeDirection(state.getValue(FACING), state.getValue(EDGE)));
        return state.setValue(FACING, facing).setValue(EDGE, edgeForDirection(facing, edge));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EDGE);
    }

    public enum Edge implements StringRepresentable {
        RIGHT("right"), LEFT("left"), TOP("top"), BOTTOM("bottom");
        private final String name;
        Edge(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}
