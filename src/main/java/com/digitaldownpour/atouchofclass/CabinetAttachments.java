package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.*;
import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Cabinet-owned work surfaces; no block is installed at the position above the cabinet. */
@EventBusSubscriber(modid = ATouchofClass.MODID)
public final class CabinetAttachments {
    private CabinetAttachments() {}

    public static boolean isAttachment(BlockState state) {
        return state.getBlock() instanceof CountertopBlock || state.getBlock() instanceof SinkBlock
                || state.getBlock() instanceof KitchenSinkBlock || state.getBlock() instanceof InductionCooktopBlock;
    }

    @SubscribeEvent
    public static void use(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var player = event.getEntity();
        if (player.isSpectator() || !player.mayBuild()) return;
        var stack = event.getItemStack();
        var pos = event.getPos();
        var blockItem = stack.getItem() instanceof BlockItem item ? item : null;
        boolean placing = blockItem != null && isAttachment(blockItem.getBlock().defaultBlockState());
        if (placing) {
            // Check the actual placement position too: clicking an adjacent block must not bypass the rule.
            var context = new BlockPlaceContext(player, event.getHand(), stack, event.getHitVec());
            if (level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet) {
                event.setCanceled(true);
                event.setCancellationResult(cabinet.getAttachment().isAir() ? InteractionResult.SUCCESS : InteractionResult.FAIL);
                if (cabinet.getAttachment().isAir() && !level.isClientSide()) {
                    attach(level, pos, blockItem.getBlock().defaultBlockState()
                            .setValue(HorizontalDirectionalBlock.FACING, player.getDirection().getOpposite()));
                    stack.consume(1, player);
                    level.playSound(null, pos, blockItem.getBlock().defaultBlockState().getSoundType().getPlaceSound(),
                            net.minecraft.sounds.SoundSource.BLOCKS, 1, 1);
                }
            } else if (level.getBlockEntity(context.getClickedPos().below()) instanceof CabinetBlockEntity below
                    && !below.getAttachment().isAir()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet) || cabinet.getAttachment().isAir()) return;
        var state = cabinet.getAttachment();
        if (player.isSecondaryUseActive() && stack.isEmpty()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (!level.isClientSide()) {
                cabinet.setAttachment(Blocks.AIR.defaultBlockState());
                refreshPairs(level, pos);
                if (!player.getAbilities().instabuild) {
                    var removed = new ItemStack(state.getBlock());
                    if (!player.getInventory().add(removed)) Block.popResource(level, pos, removed);
                }
            }
            return;
        }
        BlockState modified = null;
        if (stack.is(Items.HONEYCOMB)) modified = HoneycombItem.getWaxed(state).orElse(null);
        else if (stack.is(net.minecraft.tags.ItemTags.AXES)) {
            Block unwaxed = net.neoforged.neoforge.common.DataMapHooks.getBlockUnwaxed(state.getBlock());
            modified = unwaxed != null ? unwaxed.withPropertiesOf(state) : WeatheringCopper.getPrevious(state).orElse(null);
        }
        if (modified != null) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (!level.isClientSide()) {
                cabinet.setAttachment(modified);
                if (stack.is(Items.HONEYCOMB)) stack.consume(1, player);
                else stack.hurtAndBreak(1, player, event.getHand().asEquipmentSlot());
            }
        }
    }

    private static net.minecraft.world.level.block.state.properties.EnumProperty<ChestType> pairType(BlockState state) {
        return state.getBlock() instanceof InductionCooktopBlock ? InductionCooktopBlock.TYPE : KitchenSinkBlock.TYPE;
    }

    private static boolean canPair(BlockState state) {
        return state.getBlock() instanceof KitchenSinkBlock || state.getBlock() instanceof InductionCooktopBlock;
    }

    public static void attach(Level level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet) || !cabinet.getAttachment().isAir()) return;
        if (canPair(state)) {
            var typeProperty = pairType(state);
            state = state.setValue(typeProperty, ChestType.SINGLE);
            Direction right = state.getValue(HorizontalDirectionalBlock.FACING).getClockWise();
            // Preserve each standalone family's preferred side when bridging two single fittings.
            Direction[] sides = state.getBlock() instanceof InductionCooktopBlock
                    ? new Direction[] {right, right.getOpposite()} : new Direction[] {right.getOpposite(), right};
            for (Direction side : sides) {
                if (level.getBlockEntity(pos.relative(side)) instanceof CabinetBlockEntity neighbor
                        && matches(neighbor.getAttachment(), state, ChestType.SINGLE)) {
                    boolean onLeft = side == right.getOpposite();
                    neighbor.setAttachment(neighbor.getAttachment().setValue(typeProperty, onLeft ? ChestType.LEFT : ChestType.RIGHT));
                    state = state.setValue(typeProperty, onLeft ? ChestType.RIGHT : ChestType.LEFT);
                    break;
                }
            }
        }
        cabinet.setAttachment(state);
    }

    private static boolean matches(BlockState a, BlockState b, ChestType type) {
        return canPair(a) && a.is(b.getBlock())
                && a.getValue(HorizontalDirectionalBlock.FACING) == b.getValue(HorizontalDirectionalBlock.FACING)
                && a.getValue(pairType(a)) == type;
    }

    public static void refreshPairs(Level level, BlockPos changed) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos pos = changed.relative(side);
            if (!(level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet)) continue;
            var state = cabinet.getAttachment();
            if (!canPair(state)) continue;
            var type = state.getValue(pairType(state));
            if (type == ChestType.SINGLE) continue;
            Direction right = state.getValue(HorizontalDirectionalBlock.FACING).getClockWise();
            BlockPos partnerPos = pos.relative(type == ChestType.LEFT ? right : right.getOpposite());
            if (!(level.getBlockEntity(partnerPos) instanceof CabinetBlockEntity partner)
                    || !matches(partner.getAttachment(), state, type == ChestType.LEFT ? ChestType.RIGHT : ChestType.LEFT)) {
                cabinet.setAttachment(state.setValue(pairType(state), ChestType.SINGLE));
            }
        }
    }

    public static void weather(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet)) return;
        var state = cabinet.getAttachment();
        if (!(state.getBlock() instanceof WeatheringCopper copper) || random.nextFloat() >= 0.05688889F) return;
        int same = 0, older = 0, age = copper.getAge().ordinal();
        // Match vanilla copper's distance and probability, including nearby cabinet attachments.
        for (BlockPos neighbor : BlockPos.withinManhattan(pos.above(), 4)) {
            if (neighbor.distManhattan(pos.above()) > 4 || neighbor.equals(pos.above())) continue;
            BlockState other = level.getBlockState(neighbor);
            if (level.getBlockEntity(neighbor.below()) instanceof CabinetBlockEntity attached && !attached.getAttachment().isAir())
                other = attached.getAttachment();
            if (other.getBlock() instanceof WeatheringCopper c) {
                if (c.getAge().ordinal() < age) return;
                if (c.getAge().ordinal() == age) same++; else older++;
            }
        }
        float chance = (older + 1F) / (older + same + 1F);
        if (random.nextFloat() < chance * chance * copper.getChanceModifier())
            copper.getNext(state).ifPresent(cabinet::setAttachment);
    }
}
