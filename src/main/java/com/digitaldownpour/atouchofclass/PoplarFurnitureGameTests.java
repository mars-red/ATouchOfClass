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
