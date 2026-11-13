package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public final class SpandrelBlock extends HorizontalDirectionalBlock {
    public static final int FRONT = 6;
    public static final int BACK = 10;
    public static final int BAND_BOTTOM = 8;
    public static final EnumProperty<Section> SECTION = EnumProperty.create("section", Section.class);
    private static final Map<Direction, VoxelShape> STRAIGHT = shapes(Section.STRAIGHT);
    private static final Map<Direction, VoxelShape> LEFT = shapes(Section.LEFT);
    private static final Map<Direction, VoxelShape> RIGHT = shapes(Section.RIGHT);

    public enum Section implements StringRepresentable {
        STRAIGHT("straight"), LEFT("left"), RIGHT("right");

        private final String name;
        Section(String name) { this.name = name; }
        @Override
        public String getSerializedName() { return name; }
        Section mirror() {
            return switch (this) {
                case STRAIGHT -> STRAIGHT;
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
            };
        }
    }

    public SpandrelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(SECTION, Section.STRAIGHT));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction front = context.getHorizontalDirection().getOpposite();
        Direction clicked = context.getClickedFace();
        Direction attachedSide = clicked.getOpposite();
        BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(attachedSide));
        if (clicked.getAxis().isHorizontal() && neighbor.getBlock() instanceof SpandrelBlock
                && neighbor.getValue(FACING).getAxis() != clicked.getAxis()) {
            return defaultBlockState().setValue(FACING, neighbor.getValue(FACING));
        }
        Section section = Section.STRAIGHT;
        if (attachedSide == front.getClockWise()) section = Section.LEFT;
        if (attachedSide == front.getCounterClockWise()) section = Section.RIGHT;
        return defaultBlockState().setValue(FACING, front).setValue(SECTION, section);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .setValue(SECTION, state.getValue(SECTION).mirror());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SECTION);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var shapes = switch (state.getValue(SECTION)) {
            case STRAIGHT -> STRAIGHT;
            case LEFT -> LEFT;
            case RIGHT -> RIGHT;
        };
        return shapes.get(state.getValue(FACING));
    }

    private static Map<Direction, VoxelShape> shapes(Section section) {
        if (section == Section.STRAIGHT) {
            return HorizontalFurnitureBlock.createDirectionalShapes(new double[][]{{0, BAND_BOTTOM, FRONT, 16, 16, BACK}});
        }
        // One-unit steps approximate the tapered edge for collision and selection.
        double[][] boxes = new double[16][];
        for (int x = 0; x < 16; x++) {
            double bottom = (section == Section.RIGHT ? x + .5 : 15.5 - x) / 2;
            boxes[x] = new double[]{x, bottom, FRONT, x + 1, 16, BACK};
        }
        return HorizontalFurnitureBlock.createDirectionalShapes(boxes);
    }
}
