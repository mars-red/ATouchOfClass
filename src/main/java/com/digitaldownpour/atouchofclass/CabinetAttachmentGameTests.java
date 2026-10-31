package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.KitchenSinkBlock;
import com.digitaldownpour.atouchofclass.blocks.InductionCooktopBlock;
import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class CabinetAttachmentGameTests {
    private static void check(GameTestHelper h, boolean condition, String message) {
        h.assertTrue(condition, Component.literal(message));
    }
    private static PlayerInteractEvent.RightClickBlock use(GameTestHelper h, net.minecraft.world.entity.player.Player player,
                                                          BlockPos pos, Direction face) {
        var event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos,
                new BlockHitResult(Vec3.atCenterOf(pos).relative(face, .5), face, pos, false));
        CabinetAttachments.use(event);
        return event;
    }

    public static void everySurfaceAttachesPersistsAndDrops(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var relative = new BlockPos(2, 2, 2);
        var pos = h.absolutePos(relative);
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!CabinetAttachments.isAttachment(block.defaultBlockState())) continue;
            h.setBlock(relative, ModBlocks.DARK_OAK_DRAWER_CABINET.get());
            var cabinet = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos);
            cabinet.setItem(0, new ItemStack(Items.DIAMOND, 3));
            var stack = new ItemStack(block, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            var event = use(h, player, pos, Direction.UP);
            check(h, event.isCanceled() && stack.getCount() == 1, block + " must attach and consume one");
            check(h, cabinet.getAttachment().is(block) && h.getBlockState(relative.above()).isAir(), block + " must leave upper position empty");
            var saved = cabinet.saveWithoutMetadata(h.getLevel().registryAccess());
            var restored = new CabinetBlockEntity(pos, cabinet.getBlockState());
            restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
            check(h, restored.getAttachment().equals(cabinet.getAttachment()) && restored.getItem(0).getCount() == 3,
                    block + " must persist attachment and inventory");
            var drops = Block.getDrops(cabinet.getBlockState(), h.getLevel(), pos, cabinet);
            check(h, drops.stream().filter(s -> s.is(block.asItem())).mapToInt(ItemStack::getCount).sum() == 1,
                    block + " must drop exactly one attachment");
            check(h, drops.stream().anyMatch(s -> s.is(ModBlocks.DARK_OAK_DRAWER_CABINET.get().asItem())), "cabinet must also drop");
            check(h, use(h, player, pos, Direction.UP).getCancellationResult() == net.minecraft.world.InteractionResult.FAIL
                    && stack.getCount() == 1, "second attachment must fail without consumption");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setShiftKeyDown(true);
            use(h, player, pos, Direction.NORTH);
            check(h, cabinet.getAttachment().isAir() && cabinet.getItem(0).getCount() == 3, "removal must leave inventory intact");
            player.setShiftKeyDown(false);
            cabinet.clearContent();
            h.setBlock(relative, Blocks.AIR);
        }
        h.succeed();
    }

    public static void freeUpperSpaceAndSidePlacementGuard(GameTestHelper h) {
        var relative = new BlockPos(3, 2, 3);
        var pos = h.absolutePos(relative);
        h.setBlock(relative, ModBlocks.DARK_OAK_DRAWER_CABINET.get());
        CabinetAttachments.attach(h.getLevel(), pos, ModBlocks.CALCITE_SINK.get().defaultBlockState());
        h.setBlock(relative.above(), Blocks.STONE);
        var cabinet = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos);
        check(h, !cabinet.getAttachment().isAir() && h.getBlockState(relative.above()).is(Blocks.STONE), "ordinary blocks may overlap attachment");
        h.setBlock(relative.above(), Blocks.AIR);
        h.setBlock(relative.above().east(), Blocks.STONE);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.INDUCTION_COOKTOP.get()));
        check(h, use(h, player, pos, Direction.UP).getCancellationResult() == net.minecraft.world.InteractionResult.FAIL,
                "cooktop cannot replace an attached sink");
        var event = use(h, player, pos.above().east(), Direction.WEST);
        check(h, event.isCanceled() && event.getCancellationResult() == net.minecraft.world.InteractionResult.FAIL,
                "side placement above occupied cabinet must fail");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Blocks.TORCH));
        check(h, !use(h, player, pos, Direction.UP).isCanceled(), "ordinary items must not be restricted");
        h.succeed();
    }

    public static void kitchenPairsSplitInEveryFacing(GameTestHelper h) {
        var relative = new BlockPos(4, 2, 4);
        var pos = h.absolutePos(relative);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Direction right = facing.getClockWise();
            for (boolean reverse : new boolean[]{false, true}) {
                h.setBlock(relative, ModBlocks.DARK_OAK_DRAWER_CABINET.get());
                h.setBlock(relative.relative(right), ModBlocks.DARK_OAK_DRAWER_CABINET.get());
                var state = ModBlocks.KITCHEN_SINK.get().defaultBlockState().setValue(KitchenSinkBlock.FACING, facing);
                CabinetAttachments.attach(h.getLevel(), reverse ? pos.relative(right) : pos, state);
                CabinetAttachments.attach(h.getLevel(), reverse ? pos : pos.relative(right), state);
                var left = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos);
                var rightBE = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos.relative(right));
                check(h, left.getAttachment().getValue(KitchenSinkBlock.TYPE) == ChestType.LEFT
                        && rightBE.getAttachment().getValue(KitchenSinkBlock.TYPE) == ChestType.RIGHT, "sinks must pair in either order");
                h.setBlock(relative.relative(right, 2), ModBlocks.DARK_OAK_DRAWER_CABINET.get());
                CabinetAttachments.attach(h.getLevel(), pos.relative(right, 2), state);
                var third = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos.relative(right, 2));
                check(h, third.getAttachment().getValue(KitchenSinkBlock.TYPE) == ChestType.SINGLE,
                        "third sink must not expand established pair");
                var player = h.makeMockPlayer(GameType.SURVIVAL);
                player.setShiftKeyDown(true);
                use(h, player, pos.relative(right), Direction.UP);
                check(h, left.getAttachment().getValue(KitchenSinkBlock.TYPE) == ChestType.SINGLE
                        && rightBE.getAttachment().isAir(), "removing an attachment must split its pair");
                CabinetAttachments.attach(h.getLevel(), pos.relative(right), state);
                check(h, left.getAttachment().getValue(KitchenSinkBlock.TYPE) == ChestType.LEFT
                        && third.getAttachment().getValue(KitchenSinkBlock.TYPE) == ChestType.SINGLE,
                        "bridge attachment must choose only its left neighbor");
                h.setBlock(relative.relative(right, 2), Blocks.AIR);
                h.setBlock(relative.relative(right), Blocks.AIR);
                check(h, left.getAttachment().getValue(KitchenSinkBlock.TYPE) == ChestType.SINGLE, "breaking partner must restore single sink");
                h.setBlock(relative, Blocks.AIR);
            }
        }
        h.succeed();
    }

    public static void cooktopPairsSplitInEveryFacing(GameTestHelper h) {
        var relative = new BlockPos(4, 2, 4);
        var pos = h.absolutePos(relative);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Direction right = facing.getClockWise();
            for (boolean reverse : new boolean[]{false, true}) {
                h.setBlock(relative, ModBlocks.DARK_OAK_DRAWER_CABINET.get());
                h.setBlock(relative.relative(right), ModBlocks.DARK_OAK_DRAWER_CABINET.get());
                var state = ModBlocks.INDUCTION_COOKTOP.get().defaultBlockState().setValue(InductionCooktopBlock.FACING, facing);
                CabinetAttachments.attach(h.getLevel(), reverse ? pos.relative(right) : pos, state);
                CabinetAttachments.attach(h.getLevel(), reverse ? pos : pos.relative(right), state);
                var left = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos);
                var rightBE = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos.relative(right));
                check(h, left.getAttachment().getValue(InductionCooktopBlock.TYPE) == ChestType.LEFT
                        && rightBE.getAttachment().getValue(InductionCooktopBlock.TYPE) == ChestType.RIGHT, "cooktops must pair in either order");
                h.setBlock(relative.relative(right, 2), ModBlocks.DARK_OAK_DRAWER_CABINET.get());
                CabinetAttachments.attach(h.getLevel(), pos.relative(right, 2), state);
                var third = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos.relative(right, 2));
                check(h, third.getAttachment().getValue(InductionCooktopBlock.TYPE) == ChestType.SINGLE,
                        "third cooktop must not expand established pair");
                var player = h.makeMockPlayer(GameType.SURVIVAL);
                player.setShiftKeyDown(true);
                use(h, player, pos.relative(right), Direction.UP);
                check(h, left.getAttachment().getValue(InductionCooktopBlock.TYPE) == ChestType.SINGLE
                        && rightBE.getAttachment().isAir(), "removing an attachment must split its pair");
                CabinetAttachments.attach(h.getLevel(), pos.relative(right), state);
                check(h, left.getAttachment().getValue(InductionCooktopBlock.TYPE) == ChestType.SINGLE
                        && third.getAttachment().getValue(InductionCooktopBlock.TYPE) == ChestType.RIGHT,
                        "bridge attachment must choose only its right neighbor");
                h.setBlock(relative.relative(right, 2), Blocks.AIR);
                h.setBlock(relative.relative(right), Blocks.AIR);
                check(h, left.getAttachment().getValue(InductionCooktopBlock.TYPE) == ChestType.SINGLE, "breaking partner must restore single cooktop");
                h.setBlock(relative, Blocks.AIR);
            }
        }
        h.succeed();
    }

    public static void everyCabinetBreakDropsAttachmentAndContents(GameTestHelper h) {
        var relative = new BlockPos(2, 2, 2);
        var pos = h.absolutePos(relative);
        for (var holder : ModBlocks.cabinetBlocks()) {
            h.setBlock(relative, holder.get());
            var cabinet = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos);
            cabinet.setItem(0, new ItemStack(Items.DIAMOND, 3));
            CabinetAttachments.attach(h.getLevel(), pos, ModBlocks.KITCHEN_SINK.get().defaultBlockState());
            h.getLevel().destroyBlock(pos, true);
            var drops = h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(pos).inflate(1));
            check(h, drops.stream().filter(e -> e.getItem().is(holder.get().asItem())).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                    "each cabinet variant must drop once");
            check(h, drops.stream().filter(e -> e.getItem().is(ModBlocks.KITCHEN_SINK.get().asItem())).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                    "attached sink must drop once on destruction");
            check(h, drops.stream().filter(e -> e.getItem().is(Items.DIAMOND)).mapToInt(e -> e.getItem().getCount()).sum() == 3,
                    "stored contents must drop once");
            drops.forEach(net.minecraft.world.entity.item.ItemEntity::discard);
        }
        h.succeed();
    }

    public static void attachedCopperWaxesScrapesAndWeathers(GameTestHelper h) {
        var relative = new BlockPos(2, 2, 2);
        var pos = h.absolutePos(relative);
        h.setBlock(relative, ModBlocks.DARK_OAK_DRAWER_CABINET.get());
        var cabinet = (CabinetBlockEntity)h.getLevel().getBlockEntity(pos);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (Block block : new Block[]{ModBlocks.CUT_COPPER_COUNTERTOP.get(), ModBlocks.CUT_COPPER_SINK.get()}) {
            var state = block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST);
            cabinet.setAttachment(state);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.HONEYCOMB, 2));
            use(h, player, pos, Direction.UP);
            check(h, cabinet.getAttachment().equals(net.minecraft.world.item.HoneycombItem.getWaxed(state).orElseThrow()), "wax must apply to attachment");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            use(h, player, pos, Direction.UP);
            check(h, cabinet.getAttachment().equals(state), "axe must remove wax");
            for (int i = 0; i < 10000 && cabinet.getAttachment().is(block); i++) CabinetAttachments.weather(h.getLevel(), pos, net.minecraft.util.RandomSource.create(1234 + i));
            check(h, !cabinet.getAttachment().is(block), "unwaxed attachment must oxidize");
            use(h, player, pos, Direction.UP);
            check(h, cabinet.getAttachment().equals(state), "axe must scrape oxidation and preserve facing");
        }
        h.succeed();
    }
}
