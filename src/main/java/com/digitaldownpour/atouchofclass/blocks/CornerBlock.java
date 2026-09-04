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

public class CornerBlock extends Block {
    // Facing points out from the supporting face, toward the player.
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Corner> CORNER = EnumProperty.create("corner", Corner.class);
    private final java.util.Map<BlockState, VoxelShape> shapes = new java.util.HashMap<>();

    public CornerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(CORNER, Corner.TOP_RIGHT));
        for (BlockState state : stateDefinition.getPossibleStates()) {
            Direction facing = state.getValue(FACING);
            Corner corner = state.getValue(CORNER);
            double[] min = {0, 0, 0};
            double[] max = {16, 16, 16};
            restrict(min, max, facing.getOpposite(), 4);
            restrict(min, max, horizontalDirection(facing, corner), 8);
            restrict(min, max, verticalDirection(facing, corner), 8);
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

    private static Direction horizontalDirection(Direction facing, Corner corner) {
        return corner.right ? right(facing) : right(facing).getOpposite();
    }

    private static Direction verticalDirection(Direction facing, Corner corner) {
        return corner.top ? up(facing) : up(facing).getOpposite();
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        Vec3 hit = context.getClickLocation().subtract(Vec3.atCenterOf(context.getClickedPos()));
        // Project into the clicked face. Its normal coordinate does not affect corner selection.
        double horizontal = projection(hit, right(facing));
        double vertical = projection(hit, up(facing));
        // Each half of each face axis selects one side of the corner. Center lines
        // consistently choose right/top, including an exact center click.
        return defaultBlockState().setValue(FACING, facing)
                .setValue(CORNER, Corner.from(horizontal >= 0, vertical >= 0));
    }
    private static double projection(Vec3 hit, Direction direction) {
        return hit.x * direction.getStepX() + hit.y * direction.getStepY() + hit.z * direction.getStepZ();
    }

    private static Corner cornerForDirections(Direction facing, Direction horizontal, Direction vertical) {
        Vec3 corner = new Vec3(horizontal.getStepX() + vertical.getStepX(),
                horizontal.getStepY() + vertical.getStepY(), horizontal.getStepZ() + vertical.getStepZ());
        return Corner.from(projection(corner, right(facing)) > 0, projection(corner, up(facing)) > 0);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        Direction oldFacing = state.getValue(FACING);
        Corner corner = state.getValue(CORNER);
        Direction facing = rotation.rotate(oldFacing);
        return state.setValue(FACING, facing).setValue(CORNER, cornerForDirections(facing,
                rotation.rotate(horizontalDirection(oldFacing, corner)),
                rotation.rotate(verticalDirection(oldFacing, corner))));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction oldFacing = state.getValue(FACING);
        Corner corner = state.getValue(CORNER);
        Direction facing = mirror.mirror(oldFacing);
        return state.setValue(FACING, facing).setValue(CORNER, cornerForDirections(facing,
                mirror.mirror(horizontalDirection(oldFacing, corner)),
                mirror.mirror(verticalDirection(oldFacing, corner))));
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
        builder.add(FACING, CORNER);
    }

    public enum Corner implements StringRepresentable {
        TOP_RIGHT("top_right", true, true), TOP_LEFT("top_left", false, true),
        BOTTOM_RIGHT("bottom_right", true, false), BOTTOM_LEFT("bottom_left", false, false);

        private final String name;
        private final boolean right;
        private final boolean top;

        Corner(String name, boolean right, boolean top) {
            this.name = name;
            this.right = right;
            this.top = top;
        }

        private static Corner from(boolean right, boolean top) {
            return top ? (right ? TOP_RIGHT : TOP_LEFT) : (right ? BOTTOM_RIGHT : BOTTOM_LEFT);
        }

        @Override
        public String getSerializedName() { return name; }
    }
}
