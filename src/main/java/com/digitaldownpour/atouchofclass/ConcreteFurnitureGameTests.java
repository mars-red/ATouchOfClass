package com.digitaldownpour.atouchofclass;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;

public final class ConcreteFurnitureGameTests {
    private ConcreteFurnitureGameTests() {}

    public static void everyColorHasSlabRecipesAndUnlocks(GameTestHelper helper) {
        var level = helper.getLevel();
        for (DyeColor color : DyeColor.values()) {
            String prefix = color.getName() + "_concrete";
            var slab = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(prefix + "_slab"));
            helper.assertTrue(slab != null && slab != net.minecraft.world.item.Items.AIR,
                    Component.literal("Vanilla slab must exist: " + prefix));
            for (String kind : List.of("countertop", "sink")) {
                var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, prefix + "_" + kind);
                var key = ResourceKey.create(Registries.RECIPE, id);
                var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
                var input = kind.equals("countertop")
                        ? CraftingInput.of(3, 1, List.of(new ItemStack(slab), new ItemStack(slab), new ItemStack(slab)))
                        : CraftingInput.of(3, 2, List.of(new ItemStack(slab), ItemStack.EMPTY, new ItemStack(slab),
                                ItemStack.EMPTY, new ItemStack(slab), ItemStack.EMPTY));
                helper.assertTrue(recipe.matches(input, level), Component.literal("Matching slab pattern must craft " + id));
                var output = recipe.assemble(input);
                helper.assertTrue(output.is(BuiltInRegistries.ITEM.getValue(id)) && output.getCount() == 6,
                        Component.literal("Recipe must produce six matching furniture items: " + id));
                var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(
                        ATouchofClass.MODID, "recipes/building_blocks/" + prefix + "_" + kind));
                helper.assertTrue(advancement != null && advancement.value().rewards().recipes().contains(key),
                        Component.literal("Advancement must unlock " + id));
                helper.assertTrue(advancement.value().criteria().containsKey("has_concrete_slab"),
                        Component.literal("Slab pickup criterion must exist for " + id));
            }
        }
        helper.succeed();
    }
}
