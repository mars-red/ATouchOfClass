package com.digitaldownpour.atouchofclass.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/** A compact porcelain toilet whose lid can be toggled by interaction. */
public class ToiletBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ToiletBlock> CODEC = simpleCodec(ToiletBlock::new);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        double[][] boxes = {
                {4, 0, 4, 12, 1, 13},
                {5, 1, 5, 11, 6, 12},
                {2, 6, 1, 14, 10, 13},
                {2, 8, 12, 14, 16, 16}
        };
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape shape = Shapes.empty();
            int turns = switch (direction) {
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
                default -> 0;
            };
            for (double[] box : boxes) {
                double x0 = box[0], z0 = box[2], x1 = box[3], z1 = box[5];
                for (int i = 0; i < turns; i++) {
                    double nextX0 = 16 - z1;
                    double nextX1 = 16 - z0;
                    z0 = x0;
                    z1 = x1;
                    x0 = nextX0;
                    x1 = nextX1;
                }
                shape = Shapes.or(shape, Block.box(x0, box[1], z0, x1, box[4], z1));
            }
            SHAPES.put(direction, shape);
        }
    }

    public ToiletBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    protected MapCodec<? extends ToiletBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(OPEN, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        return toggleLid(state, level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        return toggleLid(state, level, pos, player);
    }

    private InteractionResult toggleLid(BlockState state, Level level, BlockPos pos, Player player) {
        boolean opening = !state.getValue(OPEN);
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(OPEN, opening), Block.UPDATE_ALL);
            level.playSound(null, pos,
                    opening ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 0.6F, opening ? 1.15F : 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }
}
