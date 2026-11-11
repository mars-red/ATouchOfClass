package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public final class RakeBoardBlock extends HorizontalDirectionalBlock {
    private static final TagKey<Block> SUPPORTS = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "rake_board_supports"));
    public static final BooleanProperty RISES_LEFT = BooleanProperty.create("rises_left");
    public static final double WIDTH = 8;
    public static final double BOARD_FRONT = 6;
    public static final double BOARD_BACK = 10;
    public static final double VERTICAL_DROP = WIDTH * Math.sqrt(2);

    private static final Map<Direction, VoxelShape> RISING_LEFT_SHAPES = shapes(false);
    private static final Map<Direction, VoxelShape> RISING_RIGHT_SHAPES = shapes(true);

    public RakeBoardBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(RISES_LEFT, true));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction front = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        BlockState roof = context.getLevel().getBlockState(pos.above());
        if (!isSupport(roof)) return null;
        if (roof.getBlock() instanceof StairBlock && roof.getValue(StairBlock.SHAPE) == StairsShape.STRAIGHT) {
            Direction uphill = roof.getValue(StairBlock.FACING);
            for (Direction direction : context.getNearestLookingDirections()) {
                if (direction.getAxis().isHorizontal() && direction.getAxis() != uphill.getAxis()) {
                    front = direction.getOpposite();
                    break;
                }
            }
            return defaultBlockState().setValue(FACING, front)
                    .setValue(RISES_LEFT, uphill == front.getClockWise());
        }

        // With no straight stair slope to follow, use the half of the underside the player clicks.
        Direction leftward = front.getClockWise();
        var offset = context.getClickLocation().subtract(pos.getX() + 0.5, 0, pos.getZ() + 0.5);
        boolean left = offset.x * leftward.getStepX() + offset.z * leftward.getStepZ() > 0;
        return defaultBlockState().setValue(FACING, front).setValue(RISES_LEFT, left);
    }

    private static boolean isSupport(BlockState roof) {
        if (!roof.is(SUPPORTS)) return false;
        if (roof.getBlock() instanceof StairBlock) return roof.getValue(StairBlock.HALF) == Half.BOTTOM;
        if (roof.getBlock() instanceof SlabBlock) return roof.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupport(level.getBlockState(pos.above()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !isSupport(neighborState)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return mirror == Mirror.NONE ? state : state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .cycle(RISES_LEFT);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, RISES_LEFT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(RISES_LEFT) ? RISING_LEFT_SHAPES : RISING_RIGHT_SHAPES).get(state.getValue(FACING));
    }

    private static Map<Direction, VoxelShape> shapes(boolean left) {
        // The mesh has true diagonals and holes. Narrow boxes approximate the solid outline for targeting.
        double[][] boxes = new double[32][];
        for (int x = 0; x < 16; x++) {
            int x0 = left ? 15 - x : x;
            double slopeY = x + 0.5;
            boxes[2 * x] = new double[]{x0, slopeY, 0, x0 + 1, 16, 16};
            boxes[2 * x + 1] = new double[]{x0, slopeY - VERTICAL_DROP, BOARD_FRONT, x0 + 1, slopeY, BOARD_BACK};
        }
        return HorizontalFurnitureBlock.createDirectionalShapes(boxes);
    }
}
