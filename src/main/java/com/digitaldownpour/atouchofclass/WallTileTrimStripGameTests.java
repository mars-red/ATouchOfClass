package com.digitaldownpour.atouchofclass;

import java.util.List;
import com.digitaldownpour.atouchofclass.blocks.CasingBlock;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class WallTileTrimStripGameTests {
    private static void assertModelMatchesShape(GameTestHelper helper, Direction face,
                                               CasingBlock.Edge edge, net.minecraft.world.phys.AABB bounds) {
        String path = "/assets/atouchofclass/models/block/wall_tile_trim_strip_"
                + face.getSerializedName() + "_" + edge.getSerializedName() + ".json";
        try (var stream = WallTileTrimStripGameTests.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing model " + path);
            var model = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(
                    stream, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var element = model.getAsJsonArray("elements").get(0).getAsJsonObject();
            var from = element.getAsJsonArray("from");
            var to = element.getAsJsonArray("to");
            var rendered = new net.minecraft.world.phys.AABB(
                    from.get(0).getAsDouble() / 16, from.get(1).getAsDouble() / 16,
                    from.get(2).getAsDouble() / 16, to.get(0).getAsDouble() / 16,
                    to.get(1).getAsDouble() / 16, to.get(2).getAsDouble() / 16);
            helper.assertTrue(rendered.equals(bounds), Component.literal(
                    "Rendered model must match hit box for " + face + " / " + edge));
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot read model " + path, exception);
        }
    }

    public static void placementAndRecipe(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        var block = ModBlocks.WALL_TILE_TRIM_STRIP.get();
        BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 3));
        for (var support : List.of(Blocks.STONE, Blocks.OAK_PLANKS, Blocks.GLASS)) {
            level.setBlockAndUpdate(wall, support.defaultBlockState());
            for (Direction face : Direction.Plane.HORIZONTAL) {
                for (var edge : CasingBlock.Edge.values()) {
                    var direction = switch (edge) {
                        case TOP -> Direction.UP;
                        case BOTTOM -> Direction.DOWN;
                        case RIGHT -> face.getCounterClockWise();
                        case LEFT -> face.getClockWise();
                    };
                    var pos = wall.relative(face);
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    var item = new ItemStack(block.asItem());
                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                    var hit = new BlockHitResult(Vec3.atCenterOf(wall).add(
                            face.getStepX() * .5 + direction.getStepX() * .45,
                            direction.getStepY() * .45,
                            face.getStepZ() * .5 + direction.getStepZ() * .45), face, wall, false);
                    item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit));
                    var state = level.getBlockState(pos);
                    helper.assertTrue(state.is(block) && state.getValue(CasingBlock.FACING) == face
                            && state.getValue(CasingBlock.EDGE) == edge, Component.literal("Click must select " + face + " / " + edge));
                    var bounds = state.getShape(level, pos).bounds();
                    assertModelMatchesShape(helper, face, edge, bounds);
                    double depth = face.getAxis() == Direction.Axis.X ? bounds.getXsize() : bounds.getZsize();
                    double width = direction.getAxis() == Direction.Axis.Y ? bounds.getYsize()
                            : direction.getAxis() == Direction.Axis.X ? bounds.getXsize() : bounds.getZsize();
                    double length = edge == CasingBlock.Edge.TOP || edge == CasingBlock.Edge.BOTTOM
                            ? (face.getAxis() == Direction.Axis.X ? bounds.getZsize() : bounds.getXsize()) : bounds.getYsize();
                    double boundary = switch (direction) {
                        case UP -> bounds.maxY;
                        case DOWN -> 1 - bounds.minY;
                        case EAST -> bounds.maxX;
                        case WEST -> 1 - bounds.minX;
                        case SOUTH -> bounds.maxZ;
                        case NORTH -> 1 - bounds.minZ;
                    };
                    helper.assertTrue(depth == .5 / 16 && width == 1.0 / 16 && length == 1 && boundary == 1
                            && bounds.equals(state.getCollisionShape(level, pos).bounds()),
                            Component.literal("Trim bounds must be flush, full length, half-unit deep and one unit wide"));
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (var face : List.of(Direction.UP, Direction.DOWN)) {
            var item = new ItemStack(block.asItem());
            player.setItemInHand(InteractionHand.MAIN_HAND, item);
            var hit = new BlockHitResult(Vec3.atCenterOf(wall).add(0, face.getStepY() * .5, 0), face, wall, false);
            helper.assertTrue(!item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit)).consumesAction(),
                    Component.literal("Trim places on side faces only"));
        }
        var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "wall_tile_trim_strip");
        var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id)).orElseThrow().value();
        var steel = ModBlocks.BRUSHED_STAINLESS_STEEL.get().asItem();
        var input = CraftingInput.of(3, 1, List.of(new ItemStack(steel), new ItemStack(steel), new ItemStack(steel)));
        var result = recipe.assemble(input);
        helper.assertTrue(recipe.matches(input, level) && result.is(block.asItem()) && result.getCount() == 64,
                Component.literal("SSS must craft 64 trim strips"));
        helper.succeed();
    }
}
