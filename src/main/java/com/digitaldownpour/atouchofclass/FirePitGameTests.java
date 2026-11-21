package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.FirePitBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public final class FirePitGameTests {
    private static final BlockPos ORIGIN = new BlockPos(2, 1, 2);

    private FirePitGameTests() {}

    private static void place(GameTestHelper helper, int x, int z, FirePitBlock block) {
        BlockPos pos = ORIGIN.offset(x, 0, z);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, block.defaultBlockState());
    }

    private static net.minecraft.world.level.block.state.BlockState stateAt(GameTestHelper helper, int x, int z) {
        return helper.getLevel().getBlockState(helper.absolutePos(ORIGIN.offset(x, 0, z)));
    }

    public static void squareJoinsAroundOpenCenterGravel(GameTestHelper helper) {
        FirePitBlock block = ModBlocks.FIRE_PITS.get("gray_concrete_fire_pit").get();
        for (int x = 0; x < 2; x++) for (int z = 0; z < 2; z++) place(helper, x, z, block);
        var northWest = stateAt(helper, 0, 0);
        helper.assertTrue(northWest.getValue(FirePitBlock.EDGES[1]) && northWest.getValue(FirePitBlock.EDGES[2])
                        && !northWest.getValue(FirePitBlock.EDGES[0]) && !northWest.getValue(FirePitBlock.EDGES[3]),
                Component.literal("north-west pit must connect only east and south"));
        helper.assertTrue(northWest.getValue(FirePitBlock.OPEN_CORNERS[1]) && !northWest.getValue(FirePitBlock.OPEN_CORNERS[0]),
                Component.literal("only the interior corner of a 2x2 joins its gravel"));

        helper.setBlock(ORIGIN.offset(1, 0, 1), Blocks.AIR);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(!stateAt(helper, 0, 0).getValue(FirePitBlock.OPEN_CORNERS[1]),
                    Component.literal("removing the diagonal must restore the corner post"));
            helper.succeed();
        });
    }

    public static void differentColoursDoNotJoin(GameTestHelper helper) {
        place(helper, 0, 0, ModBlocks.FIRE_PITS.get("gray_concrete_fire_pit").get());
        place(helper, 1, 0, ModBlocks.FIRE_PITS.get("red_concrete_fire_pit").get());
        helper.assertTrue(!stateAt(helper, 0, 0).getValue(FirePitBlock.EDGES[1]),
                Component.literal("different concrete colours must stay separate"));
        helper.succeed();
    }

    public static void collisionHasRimAroundGravelBed(GameTestHelper helper) {
        place(helper, 0, 0, ModBlocks.FIRE_PITS.get("white_concrete_fire_pit").get());
        BlockPos pos = helper.absolutePos(ORIGIN);
        var shape = helper.getLevel().getBlockState(pos).getCollisionShape(helper.getLevel(), pos);
        helper.assertTrue(shape.max(net.minecraft.core.Direction.Axis.Y) == 10.0 / 16,
                Component.literal("pit is ten units tall"));
        helper.assertTrue(!Shapes.joinIsNotEmpty(Shapes.box(4.0 / 16, 9.0 / 16 + 1e-4, 4.0 / 16, 12.0 / 16, 1, 12.0 / 16),
                shape, BooleanOp.AND), Component.literal("gravel bed must be open above y=9"));
        helper.assertTrue(!Shapes.joinIsNotEmpty(Shapes.box(0, 2.0 / 16, 0, 1, 9.0 / 16, 1), shape, BooleanOp.ONLY_FIRST),
                Component.literal("body below the gravel must be solid"));
        helper.succeed();
    }

    public static void igniteAndDouse(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        place(helper, 0, 0, ModBlocks.FIRE_PITS.get("black_concrete_fire_pit").get());
        BlockPos pos = helper.absolutePos(ORIGIN);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
        var state = level.getBlockState(pos);
        helper.assertTrue(!state.getValue(FirePitBlock.LIT) && state.getLightEmission() == 0,
                Component.literal("pits start unlit"));

        ItemStack flint = new ItemStack(Items.FLINT_AND_STEEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, flint);
        state.useItemOn(flint, level, player, InteractionHand.MAIN_HAND, hit);
        state = level.getBlockState(pos);
        helper.assertTrue(state.getValue(FirePitBlock.LIT) && state.getLightEmission() == 15,
                Component.literal("flint and steel must light the pit at full brightness"));

        ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
        state.useItemOn(shovel, level, player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(!level.getBlockState(pos).getValue(FirePitBlock.LIT),
                Component.literal("a shovel must douse the pit"));
        helper.succeed();
    }

    public static void everyColourCraftsTwo(GameTestHelper helper) {
        var level = helper.getLevel();
        for (DyeColor color : DyeColor.values()) {
            String id = color.getName() + "_concrete_fire_pit";
            var key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(ATouchofClass.MODID, id));
            var recipe = (ShapedRecipe) level.getServer().getRecipeManager().byKey(key).orElseThrow().value();
            ItemStack concrete = new ItemStack(Blocks.CONCRETE.pick(color));
            ItemStack gravel = new ItemStack(Items.GRAVEL);
            var input = CraftingInput.of(3, 2, List.of(concrete, gravel, concrete.copy(),
                    ItemStack.EMPTY, new ItemStack(Items.CAMPFIRE), ItemStack.EMPTY));
            var result = recipe.assemble(input);
            helper.assertTrue(recipe.matches(input, level) && result.getCount() == 2
                            && result.is(ModBlocks.FIRE_PITS.get(id).get().asItem()),
                    Component.literal(id + " must craft two from CGC / F"));
        }
        helper.succeed();
    }
}
