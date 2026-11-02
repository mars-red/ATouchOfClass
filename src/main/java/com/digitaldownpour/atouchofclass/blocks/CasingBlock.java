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
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty DADO_TOP = net.minecraft.world.level.block.state.properties.BooleanProperty.create("dado_top");
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty DADO_BOTTOM = net.minecraft.world.level.block.state.properties.BooleanProperty.create("dado_bottom");
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty DADO_CORNER_TOP = net.minecraft.world.level.block.state.properties.BooleanProperty.create("dado_corner_top");
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty DADO_CORNER_BOTTOM = net.minecraft.world.level.block.state.properties.BooleanProperty.create("dado_corner_bottom");
    // Facing points out from the supporting face, toward the player.
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Edge> EDGE = EnumProperty.create("edge", Edge.class);
    private final java.util.Map<BlockState, VoxelShape> shapes = new java.util.HashMap<>();

    public CasingBlock(Properties properties) {
        this(properties, 3, 7);
    }

    protected CasingBlock(Properties properties, double depth, double width) {
        super(properties);
        // Missing properties in older saves fall back to this state. Keep all
        // optional dado geometry disabled until a real neighbor enables it.
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(EDGE, Edge.RIGHT)
                .setValue(DADO_TOP, false).setValue(DADO_BOTTOM, false)
                .setValue(DADO_CORNER_TOP, false).setValue(DADO_CORNER_BOTTOM, false));
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

    private static void restrict(double[] min, double[] max, Direction direction, double size) {
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

    protected static Direction edgeDirection(Direction facing, Edge edge) {
        return switch (edge) {
            case RIGHT -> right(facing);
            case LEFT -> right(facing).getOpposite();
            case TOP -> up(facing);
            case BOTTOM -> up(facing).getOpposite();
        };
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
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
        return dadoConnections(defaultBlockState().setValue(FACING, facing).setValue(EDGE, edge), context.getLevel(), context.getClickedPos());
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
        return dadoShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return dadoShape(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EDGE, DADO_TOP, DADO_BOTTOM, DADO_CORNER_TOP, DADO_CORNER_BOTTOM);
    }

    private BlockState dadoConnections(BlockState state, BlockGetter level, BlockPos pos) {
        boolean top = false, bottom = false, cornerTop = false, cornerBottom = false;
        Direction facing = state.getValue(FACING);
        Edge edge = state.getValue(EDGE);
        if (getClass() == CasingBlock.class && facing.getAxis().isHorizontal()
                && (edge == Edge.LEFT || edge == Edge.RIGHT)) {
            BlockState neighbor = level.getBlockState(pos.relative(edgeDirection(facing, edge).getOpposite()));
            if (neighbor.getBlock() instanceof DadoRailBlock
                    && (neighbor.getValue(DadoRailBlock.FACING) == facing
                    || neighbor.getValue(DadoRailBlock.FACING) == edgeDirection(facing, edge))) {
                top = neighbor.getValue(DadoRailBlock.HALF) == net.minecraft.world.level.block.state.properties.Half.TOP;
                bottom = !top;
            }
            // The casing itself occupies the inside corner: a perpendicular
            // rail in front needs a return across this block before reaching it.
            BlockState front = level.getBlockState(pos.relative(facing));
            if (front.getBlock() instanceof DadoRailBlock
                    && front.getValue(DadoRailBlock.FACING) == edgeDirection(facing, edge)) {
                cornerTop = front.getValue(DadoRailBlock.HALF) == net.minecraft.world.level.block.state.properties.Half.TOP;
                cornerBottom = !cornerTop;
                top |= cornerTop;
                bottom |= cornerBottom;
            }
        }
        return state.setValue(DADO_TOP, top).setValue(DADO_BOTTOM, bottom)
                .setValue(DADO_CORNER_TOP, cornerTop).setValue(DADO_CORNER_BOTTOM, cornerBottom);
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
            net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        return dadoConnections(state, level, pos);
    }

    private VoxelShape dadoShape(BlockState state) {
        VoxelShape result = shapes.get(state);
        // Minecraft evaluates geometry for every possible state during bootstrap,
        // including connection flags that placement would never produce.
        if (getClass() != CasingBlock.class || state.getValue(FACING).getAxis().isVertical()
                || (state.getValue(EDGE) != Edge.LEFT && state.getValue(EDGE) != Edge.RIGHT)) return result;
        for (boolean top : new boolean[] {false, true}) {
            if (!state.getValue(top ? DADO_TOP : DADO_BOTTOM)) continue;
            Direction facing = state.getValue(FACING);
            Direction side = edgeDirection(facing, state.getValue(EDGE)).getOpposite();
            double y = top ? 12 : 0;
            for (double[] tier : new double[][] {{0,.5,1.5},{.5,1,2},{1,3,1.5},{3,3.5,2},{3.5,4,1.5}}) {
                double[] min = {0,y+tier[0],0}, max = {16,y+tier[1],16};
                restrict(min, max, facing.getOpposite(), tier[2]);
                restrict(min, max, side, 9);
                result = net.minecraft.world.phys.shapes.Shapes.or(result, Block.box(min[0],min[1],min[2],max[0],max[1],max[2]));
                if (state.getValue(top ? DADO_CORNER_TOP : DADO_CORNER_BOTTOM)) {
                    // The perpendicular leg runs from the wall to the front
                    // boundary, joining the existing rail without a gap.
                    double[] turnMin = {0,y+tier[0],0}, turnMax = {16,y+tier[1],16};
                    restrict(turnMin, turnMax, side, tier[2]);
                    result = net.minecraft.world.phys.shapes.Shapes.or(result,
                            Block.box(turnMin[0],turnMin[1],turnMin[2],turnMax[0],turnMax[1],turnMax[2]));
                }
            }
        }
        return result;
    }

    public enum Edge implements StringRepresentable {
        RIGHT("right"), LEFT("left"), TOP("top"), BOTTOM("bottom");
        private final String name;
        Edge(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}
