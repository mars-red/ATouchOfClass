package com.digitaldownpour.atouchofclass;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;

public final class PoplarFurnitureGameTests {
    private PoplarFurnitureGameTests() {}

    public static void tallSidelightCraftsPlacesAndDrops(GameTestHelper helper) {
        var level = helper.getLevel();
        var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "tall_poplar_sidelight");
        var block = ModBlocks.TALL_POPLAR_SIDELIGHT.get();
        var key = ResourceKey.create(Registries.RECIPE, id);
        var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var plank = new ItemStack(Items.POPLAR_PLANKS);
        var glass = new ItemStack(Items.GLASS);
        var input = CraftingInput.of(2, 3, List.of(plank, glass, plank, glass, plank, glass));
        helper.assertTrue(recipe.matches(input, level), Component.literal("Poplar planks and glass must craft sidelights"));
        var result = recipe.assemble(input);
        helper.assertTrue(result.is(ModItems.TALL_POPLAR_SIDELIGHT_ITEM.get()) && result.getCount() == 4,
                Component.literal("Recipe must produce four tall poplar sidelights"));
        var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(
                ATouchofClass.MODID, "recipes/misc/tall_poplar_sidelight"));
        helper.assertTrue(advancement != null && advancement.value().rewards().recipes().contains(key),
                Component.literal("Tall poplar sidelight must unlock its recipe"));
        var base = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlockAndUpdate(base.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        for (var facing : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            level.setBlockAndUpdate(base, block.defaultBlockState().setValue(
                    com.digitaldownpour.atouchofclass.blocks.TallSidelightBlock.FACING, facing));
            for (int part = 0; part < 3; part++) {
                var state = level.getBlockState(base.above(part));
                helper.assertTrue(state.is(block)
                        && state.getValue(com.digitaldownpour.atouchofclass.blocks.TallSidelightBlock.PART) == part
                        && state.getValue(com.digitaldownpour.atouchofclass.blocks.TallSidelightBlock.FACING) == facing,
                        Component.literal("All three sidelight sections must face the placement direction"));
                var drops = Block.getDrops(state, level, base.above(part), null);
                helper.assertTrue(drops.size() == 1 && drops.getFirst().is(block.asItem()),
                        Component.literal("Each broken sidelight section must drop the matching item"));
            }
            level.destroyBlock(base.above(), false);
            for (int part = 0; part < 3; part++) {
                helper.assertTrue(level.getBlockState(base.above(part)).isAir(),
                        Component.literal("Breaking the middle must remove all three sidelight sections"));
            }
        }
        helper.succeed();
    }

    public static void parquetCraftsUnlocksAndDrops(GameTestHelper helper) {
        var level = helper.getLevel();
        var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "poplar_parquet");
        var block = ModBlocks.BASIC_BLOCKS.get(id.getPath()).get();
        var key = ResourceKey.create(Registries.RECIPE, id);
        var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
        var plank = new ItemStack(Items.POPLAR_PLANKS);
        var input = CraftingInput.of(3, 3, List.of(plank, ItemStack.EMPTY, plank,
                ItemStack.EMPTY, plank, ItemStack.EMPTY, plank, ItemStack.EMPTY, plank));
        helper.assertTrue(recipe.matches(input, level), Component.literal("Parquet must craft from poplar planks"));
        var result = recipe.assemble(input);
        helper.assertTrue(result.is(block.asItem()) && result.getCount() == 5,
                Component.literal("Five poplar planks must produce five poplar parquet blocks"));
        var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(
                ATouchofClass.MODID, "recipes/building_blocks/poplar_parquet"));
        helper.assertTrue(advancement != null && advancement.value().rewards().recipes().contains(key),
                Component.literal("Poplar parquet must unlock its recipe"));
        helper.assertTrue(block.defaultBlockState().is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE),
                Component.literal("Poplar parquet must use the same mining tag as other parquet"));
        var drops = Block.getDrops(block.defaultBlockState(), level, helper.absolutePos(new BlockPos(2, 1, 2)), null);
        helper.assertTrue(drops.size() == 1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount() == 1,
                Component.literal("Poplar parquet must drop itself"));
        helper.succeed();
    }

    public static void everyBedColorCraftsUnlocksAndDrops(GameTestHelper helper) {
        var level = helper.getLevel();
        for (String color : FourPostBedVariants.COLORS) {
            var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "poplar_" + color + "_four_post_bed");
            var block = ModBlocks.FOUR_POST_BEDS.get(id.getPath()).get();
            var item = ModItems.FOUR_POST_BED_ITEMS.get(id.getPath()).get();
            var key = ResourceKey.create(Registries.RECIPE, id);
            var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
            var bed = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                    Identifier.withDefaultNamespace(color + "_bed")));
            var log = new ItemStack(Items.POPLAR_LOG);
            var input = CraftingInput.of(2, 3, List.of(log, log, bed, bed, log, log));
            helper.assertTrue(recipe.matches(input, level), Component.literal(id + " must craft from poplar logs and matching beds"));
            var wrong = new ItemStack(Items.BIRCH_LOG);
            helper.assertTrue(!recipe.matches(CraftingInput.of(2, 3, List.of(wrong, wrong, bed, bed, wrong, wrong)), level),
                    Component.literal(id + " must reject other wood varieties"));
            var result = recipe.assemble(input);
            helper.assertTrue(result.is(item) && result.getCount() == 1 && item.getBlock() == block,
                    Component.literal(id + " must craft one matching bed"));
            var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(
                    ATouchofClass.MODID, "recipes/building_blocks/" + id.getPath()));
            helper.assertTrue(advancement != null && advancement.value().rewards().recipes().contains(key),
                    Component.literal(id + " must have a matching recipe unlock"));
            helper.assertTrue(block.defaultBlockState().is(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE),
                    Component.literal(id + " must be mineable with an axe"));
            var drops = Block.getDrops(block.defaultBlockState(), level, helper.absolutePos(new BlockPos(2, 1, 2)), null);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(item) && drops.getFirst().getCount() == 1,
                    Component.literal(id + " must drop one matching bed"));
        }
        helper.succeed();
    }

    public static void diningTableAndChairCraftAndDrop(GameTestHelper helper) {
        var level = helper.getLevel();
        for (String kind : List.of("dining_table", "dining_chair")) {
            var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "poplar_" + kind);
            var holder = kind.equals("dining_table") ? ModBlocks.DINING_TABLES.get(id.getPath())
                    : ModBlocks.DINING_CHAIRS.get(id.getPath());
            var key = ResourceKey.create(Registries.RECIPE, id);
            var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
            var plank = new ItemStack(Items.POPLAR_PLANKS);
            var stick = new ItemStack(Items.STICK);
            var slab = new ItemStack(Items.POPLAR_SLAB);
            var input = kind.equals("dining_table")
                    ? CraftingInput.of(3, 2, List.of(plank, new ItemStack(Items.STRIPPED_POPLAR_WOOD), plank,
                            plank, ItemStack.EMPTY, plank))
                    : CraftingInput.of(3, 3, List.of(stick, stick, stick, slab, slab, slab, stick, ItemStack.EMPTY, stick));
            helper.assertTrue(recipe.matches(input, level), Component.literal(id + " must craft from matching poplar ingredients"));
            var result = recipe.assemble(input);
            helper.assertTrue(result.is(holder.get().asItem()) && result.getCount() == (kind.equals("dining_table") ? 4 : 2),
                    Component.literal(id + " must craft the matching item and quantity"));
            var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(
                    ATouchofClass.MODID, "recipes/decorations/" + id.getPath()));
            helper.assertTrue(advancement != null && advancement.value().rewards().recipes().contains(key),
                    Component.literal(id + " must have a recipe unlock"));
            var drops = Block.getDrops(holder.get().defaultBlockState(), level,
                    helper.absolutePos(new BlockPos(2, 1, 2)), null);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(holder.get().asItem()),
                    Component.literal(id + " must drop its matching item"));
        }
        helper.succeed();
    }

    public static void tablesNightstandsAndDressersCraftAndDrop(GameTestHelper helper) {
        var level = helper.getLevel();
        for (String kind : List.of("side_table", "nightstand", "dresser")) {
            var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "poplar_" + kind);
            var holder = switch (kind) {
                case "side_table" -> ModBlocks.SIDE_TABLES.get(id.getPath());
                case "nightstand" -> ModBlocks.NIGHTSTANDS.get(id.getPath());
                default -> ModBlocks.DRESSERS.get(id.getPath());
            };
            var recipeKey = ResourceKey.create(Registries.RECIPE, id);
            var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(recipeKey).orElseThrow().value();
            CraftingInput input;
            if (kind.equals("dresser")) {
                input = CraftingInput.of(2, 2, List.of(new ItemStack(Items.STRIPPED_POPLAR_LOG),
                        new ItemStack(Items.STRIPPED_POPLAR_LOG), new ItemStack(Items.CHEST), new ItemStack(Items.CHEST)));
            } else {
                var stacks = new java.util.ArrayList<ItemStack>();
                for (int i = 0; i < 3; i++) stacks.add(new ItemStack(Items.POPLAR_SLAB));
                if (kind.equals("nightstand")) {
                    for (int i = 0; i < 3; i++) stacks.add(new ItemStack(Items.POPLAR_PLANKS));
                }
                stacks.add(new ItemStack(Items.POPLAR_PLANKS));
                stacks.add(ItemStack.EMPTY);
                stacks.add(new ItemStack(Items.POPLAR_PLANKS));
                input = CraftingInput.of(3, kind.equals("nightstand") ? 3 : 2, stacks);
            }
            helper.assertTrue(recipe.matches(input, level), Component.literal(id + " must craft from matching poplar ingredients"));
            var result = recipe.assemble(input);
            helper.assertTrue(result.is(holder.get().asItem()) && result.getCount() == (kind.equals("dresser") ? 1 : 3),
                    Component.literal(id + " must produce the matching furniture and quantity"));
            var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(
                    ATouchofClass.MODID, "recipes/decorations/" + id.getPath()));
            helper.assertTrue(advancement != null && advancement.value().rewards().recipes().contains(recipeKey),
                    Component.literal(id + " must have a recipe unlock advancement"));
            var drops = Block.getDrops(holder.get().defaultBlockState(), level,
                    helper.absolutePos(new BlockPos(2, 1, 2)), null);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(holder.get().asItem()) && drops.getFirst().getCount() == 1,
                    Component.literal(id + " must drop exactly its matching item"));
        }
        helper.succeed();
    }
}
