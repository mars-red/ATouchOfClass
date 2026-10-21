package com.digitaldownpour.atouchofclass;

import java.util.List;
import com.digitaldownpour.atouchofclass.blocks.WindowLedgeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class WindowLedgeGameTests {
    public static void placesOnEveryWallFaceWithMatchingShape(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 3));
        for (var block : List.of(ModBlocks.BRICK_WINDOW_LEDGE.get(), ModBlocks.BUFF_BRICK_WINDOW_LEDGE.get())) {
            for (var support : List.of(Blocks.BRICKS, Blocks.STONE, Blocks.OAK_PLANKS, Blocks.GLASS)) {
                level.setBlockAndUpdate(wall, support.defaultBlockState());
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    BlockPos pos = wall.relative(face);
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    var item = new ItemStack(block.asItem());
                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                    player.setYRot(37);
                    var hit = new BlockHitResult(Vec3.atCenterOf(wall).add(
                            face.getStepX() * .5, 0, face.getStepZ() * .5), face, wall, false);
                    helper.assertTrue(item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit))
                            .consumesAction(), Component.literal("Ledge must place against " + support + " on " + face));
                    var state = level.getBlockState(pos);
                    helper.assertTrue(state.is(block) && state.getValue(WindowLedgeBlock.FACING) == face,
                            Component.literal("Facing must follow clicked wall face"));
                    var shape = state.getShape(level, pos).bounds();
                    var collision = state.getCollisionShape(level, pos).bounds();
                    helper.assertTrue(shape.equals(collision) && shape.minY == .75 && shape.maxY == 1,
                            Component.literal("Selection and collision must share top-aligned brick height"));
                    boolean alongX = face.getAxis() == Direction.Axis.X;
                    helper.assertTrue((alongX ? shape.getXsize() : shape.getZsize()) == .25
                                    && (alongX ? shape.getZsize() : shape.getXsize()) == 1,
                            Component.literal("Ledge must have full width and four-unit depth"));
                    double back = switch (face) {
                        case NORTH -> shape.maxZ;
                        case SOUTH -> 1 - shape.minZ;
                        case EAST -> 1 - shape.minX;
                        case WEST -> shape.maxX;
                        default -> 0;
                    };
                    helper.assertTrue(back == 1, Component.literal("Back must meet the supporting face"));
                    // Full-width bounds also guarantee no gap between two same-facing neighbors.
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
            for (var face : List.of(Direction.UP, Direction.DOWN)) {
                level.setBlockAndUpdate(wall.relative(face), Blocks.AIR.defaultBlockState());
                var item = new ItemStack(block.asItem());
                var hit = new BlockHitResult(Vec3.atCenterOf(wall).add(0, face.getStepY() * .5, 0), face, wall, false);
                helper.assertTrue(!item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit))
                        .consumesAction(), Component.literal("Floor and ceiling clicks must not place wall ledges"));
            }
        }
        helper.succeed();
    }

    public static void twoIndividualBricksCraftOneAndUnlockAndDrop(GameTestHelper helper) {
        var level = helper.getLevel();
        var blocks = List.of(ModBlocks.BRICK_WINDOW_LEDGE.get(), ModBlocks.BUFF_BRICK_WINDOW_LEDGE.get());
        var ingredients = List.of(ModItems.RED_BRICK.get(), ModItems.BUFF_BRICK.get());
        var names = List.of("brick_window_ledge", "buff_brick_window_ledge");
        for (int i = 0; i < blocks.size(); i++) {
            var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, names.get(i));
            var key = ResourceKey.create(Registries.RECIPE, id);
            var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
            var ingredient = ingredients.get(i);
            var input = CraftingInput.of(2, 1, List.of(new ItemStack(ingredient), new ItemStack(ingredient)));
            helper.assertTrue(recipe.matches(input, level), Component.literal("BB row must craft " + id));
            var result = recipe.assemble(input);
            helper.assertTrue(result.is(blocks.get(i).asItem()) && result.getCount() == 1,
                    Component.literal("Two single bricks must yield one matching ledge"));
            var square = CraftingInput.of(2, 2, List.of(new ItemStack(ingredient), new ItemStack(ingredient),
                    new ItemStack(ingredient), new ItemStack(ingredient)));
            var mixed = CraftingInput.of(2, 1, List.of(new ItemStack(ingredients.get(0)), new ItemStack(ingredients.get(1))));
            helper.assertTrue(!recipe.matches(square, level) && !recipe.matches(mixed, level),
                    Component.literal("Old square pattern and mixed materials must not craft ledges"));
            var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(
                    ATouchofClass.MODID, "recipes/building_blocks/" + names.get(i)));
            helper.assertTrue(advancement != null && advancement.value().rewards().recipes().contains(key),
                    Component.literal("Brick pickup must unlock matching recipe"));
            var pos = helper.absolutePos(new BlockPos(2, 2, 2));
            var state = blocks.get(i).defaultBlockState();
            level.setBlockAndUpdate(pos, state);
            var drops = Block.getDrops(state, level, pos, null);
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(blocks.get(i).asItem()) && drops.get(0).getCount() == 1,
                    Component.literal("Ledge must drop exactly one matching item"));
        }
        helper.succeed();
    }
}
