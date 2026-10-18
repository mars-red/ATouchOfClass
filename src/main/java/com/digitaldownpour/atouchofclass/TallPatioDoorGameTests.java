package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.util.FakePlayer;

public class TallPatioDoorGameTests {

    public static void keyCutterCodesPatioDoorAndKeysOperateIt(GameTestHelper helper) {
        for (var definition : PatioDoorVariants.VALUES) checkKeys(helper, definition);
        helper.succeed();
    }

    private static void checkKeys(GameTestHelper helper, PatioDoorVariants.Definition definition) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var expected = new KeyableInfo("patio-code", "Patio Door");
        var menu = new KeyCutterMenu(0, player.getInventory());
        menu.getSlot(0).set(new ItemStack(ModItems.TALL_PATIO_DOOR_ITEMS.get(definition.id()).get(), 4));
        helper.assertTrue(menu.areSlotsKeyable(), Component.literal("Key cutter must accept oak patio doors"));
        menu.craft(expected.keyCode(), expected.keyableName());
        var coded = menu.getSlot(1).getItem().copy();
        helper.assertTrue(menu.getSlot(0).getItem().isEmpty() && coded.getCount() == 4
                && expected.equals(coded.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE)), Component.literal("Key cutter must code the complete door stack"));
        menu.getSlot(1).set(ItemStack.EMPTY);
        menu.getSlot(0).set(new ItemStack(ModItems.DOOR_KEY.get()));
        menu.craft(expected.keyCode(), "Patio Key");
        var key = menu.getSlot(1).getItem().copy();
        helper.assertTrue(expected.keyCode().equals(key.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE).keyCode()), Component.literal("Key cutter must make a matching key"));
        BlockPos base = new BlockPos(2, 2, 2);
        helper.setBlock(base.below(), Blocks.STONE);
        var support = helper.absolutePos(base.below());
        var hit = new BlockHitResult(Vec3.atCenterOf(support), Direction.UP, support, false);
        helper.assertTrue(coded.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, coded, hit)).consumesAction(), Component.literal("Key-cutter output must place"));
        helper.useBlock(base, player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Empty hand must not open a coded patio door"));
        var wrong = key.copy();
        wrong.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, new KeyableInfo("wrong", "Wrong Key"));
        player.setItemInHand(InteractionHand.MAIN_HAND, wrong);
        helper.useBlock(base.above(), player);
        helper.assertTrue(!helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Wrong key must not open a coded patio door"));
        player.setItemInHand(InteractionHand.MAIN_HAND, key);
        helper.useBlock(base.above(2), player);
        helper.assertTrue(helper.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Matching key must open the patio door from any section"));
        var ring = new ItemStack(ModItems.KEY_RING.get());
        ring.set(DataComponents.CONTAINER,
                ItemContainerContents.fromItems(List.of(wrong, key)));
        player.setItemInHand(InteractionHand.MAIN_HAND, ring);
        helper.useBlock(base.above(), player);
        for (int part = 0; part < 3; part++) {
            helper.assertTrue(!helper.getBlockState(base.above(part)).getValue(TallDoorBlock.OPEN), Component.literal("Matching key ring must close all three sections"));
        }
        level.destroyBlock(helper.absolutePos(base), false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }

    public static void survivalPlacementAndEachSectionDropsOneDoor(GameTestHelper helper) {
        for (var definition : PatioDoorVariants.VALUES) checkBreaking(helper, GameType.SURVIVAL, false, definition);
        helper.succeed();
    }

    public static void codedItemPreservesKeyDataWhenEachSectionBreaks(GameTestHelper helper) {
        for (var definition : PatioDoorVariants.VALUES) checkBreaking(helper, GameType.SURVIVAL, true, definition);
        helper.succeed();
    }

    public static void creativeBreakingRemovesWholeDoorWithoutDrops(GameTestHelper helper) {
        for (var definition : PatioDoorVariants.VALUES) checkBreaking(helper, GameType.CREATIVE, false, definition);
        helper.succeed();
    }

    private static void checkBreaking(GameTestHelper helper, GameType mode, boolean coded, PatioDoorVariants.Definition definition) {
        var level = helper.getLevel();
        var player = new FakePlayer(level,
                new GameProfile(UUID.randomUUID(), "patio-drop-test"));
        player.gameMode.changeGameModeForPlayer(mode);
        BlockPos base = helper.absolutePos(new BlockPos(2, 2, 2));
        var expected = new KeyableInfo("patio-code", "Patio Door");
        for (int part = 0; part < 3; part++) {
            level.setBlockAndUpdate(base.below(), Blocks.STONE.defaultBlockState());
            var item = new ItemStack(ModItems.TALL_PATIO_DOOR_ITEMS.get(definition.id()).get());
            if (coded) item.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, expected);
            var hit = new BlockHitResult(Vec3.atCenterOf(base.below()), Direction.UP, base.below(), false);
            helper.assertTrue(item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit)).consumesAction(), Component.literal("Oak patio door item must place"));
            for (int section = 0; section < 3; section++) {
                var pos = base.above(section);
                var state = level.getBlockState(pos);
                helper.assertTrue(state.is(ModBlocks.TALL_PATIO_DOORS.get(definition.id()).get())
                        && state.getValue(TallDoorBlock.PART) == section, Component.literal("Item must place all three sections"));
                helper.assertTrue(level.getBlockEntity(pos) instanceof TallDoorBlockEntity, Component.literal("Each section needs a door block entity"));
            }
            helper.assertTrue(player.gameMode.destroyBlock(base.above(part)), Component.literal("Breaking must succeed"));
            for (int section = 0; section < 3; section++) {
                helper.assertTrue(level.getBlockState(base.above(section)).isAir(), Component.literal("Breaking any section removes the entire leaf"));
            }
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(base).inflate(1, 3, 1));
            if (mode == GameType.CREATIVE) {
                helper.assertTrue(drops.isEmpty(), Component.literal("Creative breaking must not drop door items"));
            } else {
                helper.assertTrue(drops.size() == 1, Component.literal("Breaking any section must spawn exactly one drop"));
                var drop = drops.getFirst();
                helper.assertTrue(drop.getItem().is(ModItems.TALL_PATIO_DOOR_ITEMS.get(definition.id()).get())
                        && drop.getItem().getCount() == 1, Component.literal("Drop must be one oak patio door"));
                if (coded) helper.assertTrue(expected.equals(drop.getItem().get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE)), Component.literal("Drop must preserve key code and name"));
                drop.discard();
            }
        }
            }

    public static void planksAndGlassCraftFourAndUnlockRecipe(GameTestHelper helper) {
        for (var definition : PatioDoorVariants.VALUES) checkRecipe(helper, definition);
        helper.succeed();
    }

    private static void checkRecipe(GameTestHelper helper, PatioDoorVariants.Definition definition) {
        var level = helper.getLevel();
        var id = ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, definition.id());
        var key = ResourceKey.create(Registries.RECIPE, id);
        var holder = level.getServer().getRecipeManager().byKey(key).orElseThrow();
        helper.assertTrue(holder.value() instanceof ShapedRecipe, Component.literal("Patio recipe must load"));
        var recipe = (ShapedRecipe) holder.value();
        var wood = new ItemStack(definition.sourceBlock().asItem());
        var glass = new ItemStack(Items.GLASS);
        var input = CraftingInput.of(3, 3,
                List.of(wood, glass, wood, wood, glass, wood, wood, glass, wood));
        helper.assertTrue(recipe.matches(input, level), Component.literal("Three rows of planks/glass/planks must match"));
        var output = recipe.assemble(input, level.registryAccess());
        helper.assertTrue(output.is(ModItems.TALL_PATIO_DOOR_ITEMS.get(definition.id()).get()) && output.getCount() == 4, Component.literal("Recipe must yield four oak patio doors"));
        var wrongInput = CraftingInput.of(3, 3,
                List.of(wood, new ItemStack(Items.GLASS_PANE), wood, wood, glass, wood, wood, glass, wood));
        helper.assertTrue(!recipe.matches(wrongInput, level), Component.literal("Glass panes must not substitute for glass blocks"));
        var player = new ServerPlayer(level.getServer(), level,
                new GameProfile(UUID.randomUUID(), "patio-recipe-test"),
                ClientInformation.createDefault());
        player.connection = new FakePlayer(level,
                new GameProfile(UUID.randomUUID(), "patio-test-connection")).connection;
        var advancement = level.getServer().getAdvancements().get(
                ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "recipes/misc/" + definition.id()));
        helper.assertTrue(advancement != null, Component.literal("Patio recipe advancement must load"));
        player.getInventory().add(wood.copy());
        CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), wood);
        helper.assertTrue(player.getAdvancements().getOrStartProgress(advancement).isDone()
                && player.getRecipeBook().contains(key), Component.literal("Acquiring oak planks must unlock the patio recipe"));
    }
}
