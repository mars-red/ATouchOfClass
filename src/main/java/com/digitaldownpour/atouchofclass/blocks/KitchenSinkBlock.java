package com.digitaldownpour.atouchofclass.blocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Individually placed sinks pair side-by-side, with the left model owning the shared faucet. */
public class KitchenSinkBlock extends HorizontalFurnitureBlock {
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
    private static final Map<ChestType, Map<Direction, VoxelShape>> SHAPES = createShapes();

    public KitchenSinkBlock(Properties properties) {
        super(properties, new double[][] {{0, 0, 0, 16, 2, 16}});
        registerDefaultState(defaultBlockState().setValue(TYPE, ChestType.SINGLE));
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Direction right = state.getValue(FACING).getClockWise();
        // Prefer the left neighbor if both are available; established pairs never expand.
        if (matches(context.getLevel().getBlockState(context.getClickedPos().relative(right.getOpposite())),
                state, ChestType.SINGLE)) {
            return state.setValue(TYPE, ChestType.RIGHT);
        }
        if (matches(context.getLevel().getBlockState(context.getClickedPos().relative(right)),
                state, ChestType.SINGLE)) {
            return state.setValue(TYPE, ChestType.LEFT);
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        Direction right = state.getValue(FACING).getClockWise();
        ChestType type = state.getValue(TYPE);
        if (type == ChestType.SINGLE) {
            if (direction == right && matches(neighborState, state, ChestType.RIGHT)) {
                return state.setValue(TYPE, ChestType.LEFT);
            }
            if (direction == right.getOpposite() && matches(neighborState, state, ChestType.LEFT)) {
                return state.setValue(TYPE, ChestType.RIGHT);
            }
        } else {
            Direction partnerDirection = type == ChestType.LEFT ? right : right.getOpposite();
            ChestType partnerType = type == ChestType.LEFT ? ChestType.RIGHT : ChestType.LEFT;
            if (direction == partnerDirection && !matches(neighborState, state, partnerType)) {
                return state.setValue(TYPE, ChestType.SINGLE);
            }
        }
        return state;
    }

    private boolean matches(BlockState neighbor, BlockState state, ChestType type) {
        return neighbor.is(this) && neighbor.getValue(FACING) == state.getValue(FACING)
                && neighbor.getValue(TYPE) == type;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        ChestType mirrored = switch (state.getValue(TYPE)) {
            case LEFT -> ChestType.RIGHT;
            case RIGHT -> ChestType.LEFT;
            default -> ChestType.SINGLE;
        };
        return rotate(state, mirror.getRotation(state.getValue(FACING))).setValue(TYPE, mirrored);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(TYPE)).get(state.getValue(FACING));
    }

    private static Map<ChestType, Map<Direction, VoxelShape>> createShapes() {
        Map<ChestType, Map<Direction, VoxelShape>> shapes = new EnumMap<>(ChestType.class);
        for (ChestType type : ChestType.values()) {
            double left = type == ChestType.RIGHT ? 1 : 2;
            double right = type == ChestType.LEFT ? 15 : 14;
            var boxes = new ArrayList<double[]>();
            // The overhanging apron is visual only; neither selection nor collision enters the cabinet.
            boxes.add(new double[] {0, 0, 0, 16, 2, 1});
            boxes.add(new double[] {0, 0, 1, left, type == ChestType.RIGHT ? 1 : 2, 11});
            boxes.add(new double[] {right, 0, 1, 16, type == ChestType.LEFT ? 1 : 2, 11});
            boxes.add(new double[] {0, 0, 11, 16, 2, 16});
            boxes.add(new double[] {left, 0, 1, right, 1, 11});
            if (type != ChestType.RIGHT) {
                double cx = type == ChestType.LEFT ? 16 : 8;
                boxes.add(new double[] {cx-1.6, 2, 11, cx+1.6, 2.35, 13.6});
                boxes.add(new double[] {cx-.65, 2.35, 11.55, cx+.65, 4.7, 12.85});
                boxes.add(new double[] {cx-.53, 4.7, 11.67, cx+.53, 13, 12.73});
                boxes.add(new double[] {cx-.53, 12.6, 6.67, cx+.53, 14.9, 12.2});
                boxes.add(new double[] {cx-.68, 6.7, 5.66, cx+.68, 12.6, 6.94});
                boxes.add(new double[] {cx-.23, 7.8, 6, cx+.23, 8.25, 12.2});
            }
            shapes.put(type, createDirectionalShapes(boxes.toArray(double[][]::new)));
        }
        return shapes;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE);
    }
}
