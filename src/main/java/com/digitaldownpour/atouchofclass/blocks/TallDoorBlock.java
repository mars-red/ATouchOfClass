package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.ModComponents;
import com.digitaldownpour.atouchofclass.ModSounds;
import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.items.KeyRingItem;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;

public class TallDoorBlock extends Block implements EntityBlock {
    private static final VoxelShape NORTH_SHAPE =
            Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape WEST_SHAPE =
            Block.box(0, 0, 0, 3, 16, 16);
    private static final VoxelShape SOUTH_SHAPE =
            Block.box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape EAST_SHAPE =
            Block.box(13, 0, 0, 16, 16, 16);

    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2); // 0 = bottom, 1 = middle, 2 = top
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;

    public TallDoorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, 0)
                .setValue(OPEN, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(HINGE, DoorHingeSide.LEFT));
    }

    private static VoxelShape shapeForFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> Shapes.block();
        };
    }

    private static VoxelShape shapeForState(BlockState state) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(OPEN)) {
            facing = state.getValue(HINGE) == DoorHingeSide.LEFT
                    ? facing.getCounterClockWise()
                    : facing.getClockWise();
        }
        return shapeForFacing(facing);
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return shapeForState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, OPEN, FACING, HINGE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        DoorHingeSide hinge = determineHinge(context, facing);

        return super.getStateForPlacement(context)
                .setValue(FACING, facing)
                .setValue(HINGE, hinge)
                .setValue(OPEN, false)
                .setValue(PART, 0);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isClientSide() && state.getValue(PART) == 0) {
            Direction facing = state.getValue(FACING);
            // Place middle part above
            world.setBlock(
                    pos.above(),
                    state.setValue(PART, 1)
                            .setValue(FACING, state.getValue(FACING))
                            .setValue(OPEN, state.getValue(OPEN))
                            .setValue(HINGE, state.getValue(HINGE)),
            3);
            // Place top part above middle
            world.setBlock(
                    pos.above(2),
                    state.setValue(PART, 2)
                            .setValue(FACING, state.getValue(FACING))
                            .setValue(OPEN, state.getValue(OPEN))
                            .setValue(HINGE, state.getValue(HINGE)),
            3);
            // NeoForge can defer onPlace until after the item components have been
            // applied to the base. Only now are the other block entities available.
            if (world.getBlockEntity(pos) instanceof TallDoorBlockEntity base) {
                base.setKeyableInfo(new KeyableInfo(base.getKeyCode(), base.getKeyableName()));
            }
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
        BlockState newState = world.getBlockState(pos);
        if (!world.isClientSide() && state.getBlock() != newState.getBlock()) {
            int part = state.getValue(PART);
            BlockPos basePos = pos.below(part); // Calculate bottom position

            for (int i = 0; i < 3; i++) {
                if (i == part) continue;
                BlockPos p = basePos.above(i);
                BlockState otherPart = world.getBlockState(p);
                if (otherPart.is(this) && otherPart.getValue(PART) == i) {
                    // Only the originally broken section rolls loot; companion sections never do.
                    world.destroyBlock(p, false);
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, world, pos, isMoving);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockPos basePos = pos.below(state.getValue(PART));
            if (level.getBlockEntity(basePos) instanceof TallDoorBlockEntity be) {
                String keyCode = be.getKeyCode();
                if (keyCode == null || keyCode.isEmpty()) {
                    toggleDoor(level, basePos);
                    return InteractionResult.SUCCESS_SERVER;
                }

                playLockedSound(level, basePos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof KeyRingItem) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            BlockPos basePos = pos.below(state.getValue(PART));
            if (level.getBlockEntity(basePos) instanceof TallDoorBlockEntity be) {
                String code = be.getKeyCode();
                if (code == null || code.isEmpty()
                        || KeyRingItem.matchesCode(stack, code)) {
                    toggleDoor(level, basePos);
                    return InteractionResult.SUCCESS;
                }
                playLockedSound(level, basePos);
            }
            return InteractionResult.CONSUME;
        }
        if (stack.has(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE) && !level.isClientSide()) {
            BlockPos basePos = pos.below(state.getValue(PART));
            if (level.getBlockEntity(basePos) instanceof TallDoorBlockEntity be) {
                String doorKeyCode = be.getKeyCode();
                String keyCode = stack.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE).keyCode();
                if (doorKeyCode == null || doorKeyCode.isEmpty() || doorKeyCode.equals(keyCode)) {
                    toggleDoor(level, basePos);
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }

        // Consume the door interaction even when access is denied, preventing
        // the held item's use-on-block behavior from placing a block afterward.
        return useWithoutItem(state, level, pos, player, hitResult);
    }

    private void playLockedSound(Level level, BlockPos basePos) {
        level.playSound(null, basePos, ModSounds.LOCKED_DOOR.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void toggleDoor(Level world, BlockPos basePos) {
        BlockState bottomState = world.getBlockState(basePos);
        if (!bottomState.is(this)) return;

        boolean newOpen = !bottomState.getValue(OPEN);

        for (int i = 0; i < 3; i++) {
            BlockPos partPos = basePos.above(i);
            BlockState state = world.getBlockState(partPos);
            if (state.getBlock() == this) {
                world.setBlock(partPos, state.setValue(OPEN, newOpen), 2);
            }
        }

        // Try open neighbor door if it exists
        Direction facing = bottomState.getValue(FACING);
        DoorHingeSide hinge = bottomState.getValue(HINGE);
        Direction offsetDir = (hinge == DoorHingeSide.LEFT) ? facing.getClockWise() : facing.getCounterClockWise();
        BlockPos neighborBasePos = basePos.relative(offsetDir);

        BlockState neighborState = world.getBlockState(neighborBasePos);
        if (neighborState.is(this)
                && neighborState.getValue(PART) == 0
                && neighborState.getValue(FACING) == facing
                && neighborState.getValue(HINGE) != hinge) {

            for (int i = 0; i < 3; i++) {
                BlockPos neighborPartPos = neighborBasePos.above(i);
                BlockState neighborPartState = world.getBlockState(neighborPartPos);
                if (neighborPartState.getBlock() == this) {
                    world.setBlock(neighborPartPos, neighborPartState.setValue(OPEN, newOpen), 2);
                }
            }
        }

        world.playSound(
                null,
                basePos,
                newOpen ? ModSounds.TALL_DOOR_OPEN.value() : ModSounds.TALL_DOOR_CLOSE.value(),
                SoundSource.BLOCKS,
                1.0f,  // volume
                1.0f   // pitch
        );

    }


    private DoorHingeSide determineHinge(BlockPlaceContext context, Direction facing) {
        BlockPos pos = context.getClickedPos();

        double clickX = context.getClickLocation().x - (double) pos.getX();
        double clickZ = context.getClickLocation().z - (double) pos.getZ();

        return switch (facing) {
            case NORTH -> clickX > 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case EAST -> clickZ > 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case SOUTH -> clickX > 0.5 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            case WEST -> clickZ > 0.5 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            default -> DoorHingeSide.LEFT;
        };
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        int part = state.getValue(PART);
        if (part == 0) {
            return level.getBlockState(pos.below()).isSolid();
        } else {
            BlockState belowState = level.getBlockState(pos.below());
            return belowState.is(this)
                    && belowState.getValue(PART) == part - 1;
        }    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TallDoorBlockEntity(blockPos, blockState);
    }
}
