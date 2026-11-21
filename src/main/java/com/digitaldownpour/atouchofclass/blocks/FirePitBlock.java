package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.entities.FirePitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Concrete fire pit. Same-block neighbours merge: the shared wall disappears so the gravel bed is
 * continuous while the outer perimeter stays four units thick.
 */
public class FirePitBlock extends Block implements EntityBlock {
    // Clockwise order also defines rotation and mirror permutations.
    public static final BooleanProperty[] EDGES = {
            BooleanProperty.create("north"), BooleanProperty.create("east"),
            BooleanProperty.create("south"), BooleanProperty.create("west")};
    /** True when the corner post is replaced by gravel (both edges and the diagonal connect). */
    public static final BooleanProperty[] OPEN_CORNERS = {
            BooleanProperty.create("north_east"), BooleanProperty.create("south_east"),
            BooleanProperty.create("south_west"), BooleanProperty.create("north_west")};
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final VoxelShape[] SHAPES = createShapes();
    private static final int FIRE_DAMAGE = 1;

    public FirePitBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any().setValue(LIT, false);
        for (BooleanProperty edge : EDGES) state = state.setValue(edge, false);
        for (BooleanProperty corner : OPEN_CORNERS) state = state.setValue(corner, false);
        registerDefaultState(state);
    }

    public static boolean isLit(BlockState state) {
        return state.getValue(LIT);
    }

    private BlockState connected(BlockState state, BlockGetter level, BlockPos pos) {
        for (int i = 0; i < 4; i++) {
            Direction a = DIRECTIONS[i], b = DIRECTIONS[(i + 1) % 4];
            boolean first = level.getBlockState(pos.relative(a)).is(this);
            boolean second = level.getBlockState(pos.relative(b)).is(this);
            boolean diagonal = level.getBlockState(pos.relative(a).relative(b)).is(this);
            state = state.setValue(EDGES[i], first).setValue(OPEN_CORNERS[i], first && second && diagonal);
        }
        return state;
    }

    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return connected(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return connected(state, level, pos);
    }

    private void refresh(Level level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (current.is(this)) {
            BlockState next = connected(current, level, pos);
            if (next != current) level.setBlock(pos, next, UPDATE_ALL);
        }
    }

    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide() && !oldState.is(this)) {
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) refresh(level, pos.offset(x, 0, z));
        }
    }

    @Override protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockState replacement = level.getBlockState(pos);
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
        if (!level.isClientSide() && !replacement.is(this)) {
            // Diagonal changes do not receive vanilla cardinal-neighbor shape updates.
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                BlockPos neighbor = pos.offset(x, 0, z);
                if (level.getBlockState(neighbor).is(this)) level.scheduleTick(neighbor, this, 1);
            }
        }
    }

    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        refresh(level, pos);
    }

    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        boolean flint = stack.is(Items.FLINT_AND_STEEL);
        boolean charge = stack.is(Items.FIRE_CHARGE);
        if (!isLit(state) && (flint || charge)) {
            level.playSound(player, pos, flint ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE,
                    SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(LIT, true), UPDATE_ALL_IMMEDIATE);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (flint) stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                else stack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        if (isLit(state) && stack.canPerformAction(ItemAbilities.SHOVEL_DOUSE)) {
            if (!level.isClientSide()) {
                level.levelEvent(null, 1009, pos, 0);
                level.setBlock(pos, state.setValue(LIT, false), UPDATE_ALL_IMMEDIATE);
                stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos pos = hit.getBlockPos();
        if (level instanceof ServerLevel server && projectile.isOnFire() && projectile.mayInteract(server, pos)
                && !isLit(state)) {
            level.setBlock(pos, state.setValue(LIT, true), UPDATE_ALL_IMMEDIATE);
        }
    }

    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
            InsideBlockEffectApplier applier, boolean precise) {
        if (isLit(state) && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().campfire(), FIRE_DAMAGE);
        }
        super.entityInside(state, level, pos, entity, applier, precise);
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isLit(state)) return;
        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE,
                    SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
        }
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5 + random.nextDouble() * 0.5 - 0.25,
                    pos.getY() + 0.8, pos.getZ() + 0.5 + random.nextDouble() * 0.5 - 0.25, 0.0, 0.07, 0.0);
        }
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int index = 0;
        for (int i = 0; i < 4; i++) {
            if (state.getValue(EDGES[i])) index |= 1 << i;
            if (state.getValue(OPEN_CORNERS[i])) index |= 1 << (i + 4);
        }
        return SHAPES[index];
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FirePitBlockEntity(pos, state);
    }

    @Override protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState result = state;
        for (int i = 0; i < 4; i++) {
            int j = Arrays.asList(DIRECTIONS).indexOf(rotation.rotate(DIRECTIONS[i]));
            result = result.setValue(EDGES[j], state.getValue(EDGES[i]))
                    .setValue(OPEN_CORNERS[j], state.getValue(OPEN_CORNERS[i]));
        }
        return result;
    }

    @Override protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        BlockState result = state;
        for (int i = 0; i < 4; i++) {
            int edge = Arrays.asList(DIRECTIONS).indexOf(mirror.mirror(DIRECTIONS[i]));
            // Reflection reverses the winding of each pair of corner directions.
            int corner = Arrays.asList(DIRECTIONS).indexOf(mirror.mirror(DIRECTIONS[(i + 1) % 4]));
            result = result.setValue(EDGES[edge], state.getValue(EDGES[i]))
                    .setValue(OPEN_CORNERS[corner], state.getValue(OPEN_CORNERS[i]));
        }
        return result;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EDGES).add(OPEN_CORNERS).add(LIT);
    }

    private static VoxelShape[] createShapes() {
        // Per-direction gravel strips (edge) and corner squares, matching the block model.
        double[][] strips = {{4, 0, 12, 4}, {12, 4, 16, 12}, {4, 12, 12, 16}, {0, 4, 4, 12}};
        double[][] corners = {{12, 0, 16, 4}, {12, 12, 16, 16}, {0, 12, 4, 16}, {0, 0, 4, 4}};
        double[][] baseStrips = {{2, 0, 14, 2}, {14, 2, 16, 14}, {2, 14, 14, 16}, {0, 2, 2, 14}};
        double[][] baseCorners = {{14, 0, 16, 2}, {14, 14, 16, 16}, {0, 14, 2, 16}, {0, 0, 2, 2}};
        VoxelShape[] shapes = new VoxelShape[256];
        for (int mask = 0; mask < 256; mask++) {
            VoxelShape base = box(2, 0, 2, 14, 2, 14);
            VoxelShape gravel = box(4, 9, 4, 12, 10, 12);
            for (int i = 0; i < 4; i++) {
                boolean edge = (mask & (1 << i)) != 0;
                boolean next = (mask & (1 << ((i + 1) % 4))) != 0;
                if (edge) {
                    double[] s = strips[i], b = baseStrips[i];
                    gravel = Shapes.or(gravel, box(s[0], 9, s[1], s[2], 10, s[3]));
                    base = Shapes.or(base, box(b[0], 0, b[1], b[2], 2, b[3]));
                }
                if (edge && next) {
                    double[] b = baseCorners[i];
                    base = Shapes.or(base, box(b[0], 0, b[1], b[2], 2, b[3]));
                }
                if ((mask & (1 << (i + 4))) != 0) {
                    double[] c = corners[i];
                    gravel = Shapes.or(gravel, box(c[0], 9, c[1], c[2], 10, c[3]));
                }
            }
            VoxelShape rim = Shapes.join(box(0, 9, 0, 16, 10, 16), gravel, BooleanOp.ONLY_FIRST);
            shapes[mask] = Shapes.or(base, box(0, 2, 0, 16, 9, 16), rim).optimize();
        }
        return shapes;
    }
}
