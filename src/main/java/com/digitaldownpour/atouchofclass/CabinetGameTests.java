package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import com.digitaldownpour.atouchofclass.blocks.DoorCabinetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;

public class CabinetGameTests {

    public static void poplarCabinetsCraftDropAndStack(GameTestHelper helper) {
        var level = helper.getLevel();
        for (var holder : java.util.List.of(ModBlocks.POPLAR_DOOR_CABINET, ModBlocks.POPLAR_DRAWER_CABINET)) {
            var id = holder.getId();
            var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id);
            var recipe = (net.minecraft.world.item.crafting.ShapedRecipe) level.getServer()
                    .getRecipeManager().byKey(key).orElseThrow().value();
            var ingredients = new java.util.ArrayList<ItemStack>();
            int emptySlot = holder == ModBlocks.POPLAR_DOOR_CABINET ? 4 : 1;
            for (int i = 0; i < 9; i++) ingredients.add(i == emptySlot ? ItemStack.EMPTY : new ItemStack(Items.POPLAR_SLAB));
            var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, ingredients);
            helper.assertTrue(recipe.matches(input, level), Component.literal(id + " must use poplar slabs"));
            var result = recipe.assemble(input);
            helper.assertTrue(result.is(holder.get().asItem()) && result.getCount() == 2,
                    Component.literal(id + " must craft two cabinets"));
            var advancement = level.getServer().getAdvancements().get(net.minecraft.resources.Identifier.fromNamespaceAndPath(
                    ATouchofClass.MODID, "recipes/building_blocks/" + id.getPath()));
            helper.assertTrue(advancement != null && advancement.value().rewards().recipes().contains(key),
                    Component.literal(id + " must have a recipe unlock advancement"));
            var drops = net.minecraft.world.level.block.Block.getDrops(holder.get().defaultBlockState(),
                    level, helper.absolutePos(new BlockPos(2, 1, 2)), null);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(holder.get().asItem()),
                    Component.literal(id + " must drop its matching item"));
        }
        BlockPos lower = new BlockPos(2, 1, 2);
        var state = ModBlocks.POPLAR_DOOR_CABINET.get().defaultBlockState();
        helper.setBlock(lower, state);
        helper.setBlock(lower.above(), state);
        helper.assertTrue(helper.getBlockState(lower).getValue(DoorCabinetBlock.JOINED_ABOVE)
                        && helper.getBlockState(lower.above()).getValue(DoorCabinetBlock.JOINED_BELOW),
                Component.literal("Poplar door cabinets must join vertically"));
        helper.setBlock(lower.above(), Blocks.AIR);
        helper.assertTrue(!helper.getBlockState(lower).getValue(DoorCabinetBlock.JOINED_ABOVE),
                Component.literal("Removing a poplar cabinet must clear the join"));
        helper.succeed();
    }

    public static void everyCabinetVariantProvidesPersistentChestStorage(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        int x = 1;

        for (var cabinetHolder : ModBlocks.cabinetBlocks()) {
            BlockPos relativePos = new BlockPos(x, 2, 2);
            helper.setBlock(relativePos, cabinetHolder.get());

            var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
            helper.assertTrue(blockEntity instanceof CabinetBlockEntity, Component.literal(cabinetHolder.getId() + " must create a cabinet block entity"));
            CabinetBlockEntity cabinet = GameTestChecks.requireType(blockEntity, CabinetBlockEntity.class);

            helper.assertTrue(cabinet.getContainerSize() == 27, Component.literal(cabinetHolder.getId() + " must expose 27 storage slots"));
            helper.assertTrue(cabinet.createMenu(1, player.getInventory(), player) instanceof ChestMenu, Component.literal(cabinetHolder.getId() + " must open the standard chest menu"));

            cabinet.setItem(0, new ItemStack(Items.DIAMOND, 3));
            var saved = cabinet.saveWithoutMetadata(helper.getLevel().registryAccess());
            var restored = new CabinetBlockEntity(helper.absolutePos(relativePos), cabinet.getBlockState());
            restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
            helper.assertTrue(restored.getItem(0).is(Items.DIAMOND) && restored.getItem(0).getCount() == 3, Component.literal(cabinetHolder.getId() + " must preserve stored items when saved and loaded"));

            cabinet.clearContent();
            helper.setBlock(relativePos, Blocks.AIR);
            x += 2;
        }

        helper.succeed();
    }

    public static void matchingDoorCabinetsJoinWhenStacked(GameTestHelper helper) {
        BlockPos lowerPos = new BlockPos(2, 1, 2);
        BlockPos upperPos = lowerPos.above();
        var cabinet = ModBlocks.DARK_OAK_DOOR_CABINET.get();
        var northFacing = cabinet.defaultBlockState().setValue(DoorCabinetBlock.FACING, Direction.NORTH);

        helper.setBlock(lowerPos, northFacing);
        helper.setBlock(upperPos, northFacing);

        var lower = helper.getBlockState(lowerPos);
        var upper = helper.getBlockState(upperPos);
        helper.assertTrue(lower.getValue(DoorCabinetBlock.JOINED_ABOVE), Component.literal("the lower door cabinet must join the matching cabinet above"));
        helper.assertTrue(upper.getValue(DoorCabinetBlock.JOINED_BELOW), Component.literal("the upper door cabinet must join the matching cabinet below"));

        helper.setBlock(upperPos, northFacing.setValue(DoorCabinetBlock.FACING, Direction.EAST));
        lower = helper.getBlockState(lowerPos);
        upper = helper.getBlockState(upperPos);
        helper.assertTrue(!lower.getValue(DoorCabinetBlock.JOINED_ABOVE), Component.literal("door cabinets facing different directions must not join"));
        helper.assertTrue(!upper.getValue(DoorCabinetBlock.JOINED_BELOW), Component.literal("a rotated door cabinet must not join the cabinet below"));

        helper.succeed();
    }
}
