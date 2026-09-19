package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/** Shared player-facing placement and rotated shape handling for furniture blocks. */
public abstract class HorizontalFurnitureBlock extends HorizontalDirectionalBlock {
    private final Map<Direction, VoxelShape> shapes;

    protected HorizontalFurnitureBlock(Properties properties, double[][] northFacingBoxes) {
        super(properties);
        shapes = createDirectionalShapes(northFacingBoxes);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static Map<Direction, VoxelShape> createDirectionalShapes(double[][] boxes) {
        Map<Direction, VoxelShape> directionalShapes = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape shape = Shapes.empty();
            int turns = switch (direction) {
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
                default -> 0;
            };

            for (double[] box : boxes) {
                double x0 = box[0];
                double z0 = box[2];
                double x1 = box[3];
                double z1 = box[5];
                for (int turn = 0; turn < turns; turn++) {
                    double nextX0 = 16 - z1;
                    double nextX1 = 16 - z0;
                    z0 = x0;
                    z1 = x1;
                    x0 = nextX0;
                    x1 = nextX1;
                }
                shape = Shapes.or(shape, Block.box(x0, box[1], z0, x1, box[4], z1));
            }
            directionalShapes.put(direction, shape);
        }
        return directionalShapes;
    }
}
