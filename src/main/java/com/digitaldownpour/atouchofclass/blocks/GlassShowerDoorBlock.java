package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A three-block-tall frameless glass door that pivots inside its occupied block column. */
public class GlassShowerDoorBlock extends ThreeHighGlassPanelBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;

    public GlassShowerDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(OPEN, false)
                .setValue(HINGE, DoorHingeSide.LEFT));
    }


    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        Direction facing = state.getValue(FACING);
        return state
                .setValue(OPEN, false)
                .setValue(HINGE, determineHinge(context, facing));
    }

    @Override
    protected VoxelShape shapeForState(BlockState state) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(OPEN)) {
            facing = state.getValue(HINGE) == DoorHingeSide.LEFT
                    ? facing.getCounterClockWise()
                    : facing.getClockWise();
        }
        return shapeForFacing(facing);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockPos basePos = pos.below(state.getValue(PART));
            BlockState bottom = level.getBlockState(basePos);
            if (!bottom.is(this)) return InteractionResult.FAIL;
            boolean open = !bottom.getValue(OPEN);
            for (int i = 0; i < 3; i++) {
                BlockPos partPos = basePos.above(i);
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(this)) {
                    level.setBlock(partPos, partState.setValue(OPEN, open), Block.UPDATE_CLIENTS);
                }
            }
            level.playSound(null, basePos,
                    open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 0.45F, 1.35F);
        }
        return InteractionResult.SUCCESS;
    }

    private DoorHingeSide determineHinge(BlockPlaceContext context, Direction facing) {
        BlockPos pos = context.getClickedPos();
        double x = context.getClickLocation().x - pos.getX();
        double z = context.getClickLocation().z - pos.getZ();
        return switch (facing) {
            case NORTH -> x > 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case EAST -> z > 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case SOUTH -> x > 0.5 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            case WEST -> z > 0.5 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            default -> DoorHingeSide.LEFT;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN, HINGE);
    }
}
