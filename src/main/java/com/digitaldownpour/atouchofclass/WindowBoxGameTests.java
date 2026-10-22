package com.digitaldownpour.atouchofclass;

import java.util.ArrayList;
import java.util.List;
import com.digitaldownpour.atouchofclass.blocks.WindowBoxBlock;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class WindowBoxGameTests {
    private static void check(GameTestHelper helper, boolean result, String message) {
        helper.assertTrue(result, Component.literal(message));
    }

    public static void everyWoodPlacesFlushOnEveryWall(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos wall = helper.absolutePos(new BlockPos(3,2,3));
        for (var definition : WindowBoxVariants.VALUES) {
            var block = ModBlocks.WINDOW_BOXES.get(definition.id()).get();
            for (var support : List.of(Blocks.STONE, Blocks.GLASS, Blocks.OAK_PLANKS)) {
                level.setBlockAndUpdate(wall, support.defaultBlockState());
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    BlockPos pos = wall.relative(face);
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    var item = new ItemStack(block.asItem());
                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                    player.setYRot(37);
                    var hit = new BlockHitResult(Vec3.atCenterOf(wall).add(
                            face.getStepX()*.5, 0, face.getStepZ()*.5), face, wall, false);
                    check(helper, item.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, item, hit)).consumesAction(),
                            "Window box must place on " + face + " of " + support);
                    var state = level.getBlockState(pos);
                    check(helper, state.is(block) && state.getValue(WindowBoxBlock.FACING) == face, "Clicked face sets orientation");
                    var shape = state.getShape(level,pos).bounds();
                    check(helper, shape.equals(state.getCollisionShape(level,pos).bounds()) && shape.minY == .5 && shape.maxY == 1,
                            "Shape must occupy the upper eight units");
                    boolean x = face.getAxis() == Direction.Axis.X;
                    check(helper, (x ? shape.getXsize() : shape.getZsize()) == .625
                            && (x ? shape.getZsize() : shape.getXsize()) == 1, "Ten deep and sixteen wide");
                    double back = switch (face) {
                        case NORTH -> shape.maxZ;
                        case SOUTH -> 1-shape.minZ;
                        case EAST -> 1-shape.minX;
                        case WEST -> shape.maxX;
                        default -> 0;
                    };
                    check(helper, back == 1, "Back must be flush with supporting face");
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
            for (var face : List.of(Direction.UP, Direction.DOWN)) {
                level.setBlockAndUpdate(wall.relative(face), Blocks.AIR.defaultBlockState());
                var item = new ItemStack(block.asItem());
                var hit = new BlockHitResult(Vec3.atCenterOf(wall).add(0,face.getStepY()*.5,0),face,wall,false);
                check(helper, !item.useOn(new UseOnContext(level,player,InteractionHand.MAIN_HAND,item,hit)).consumesAction(),
                        "Vertical faces must not mount a wall box");
            }
        }
        helper.succeed();
    }

    public static void joinsExtendsAndBreaksIndependently(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos base = helper.absolutePos(new BlockPos(3,2,3));
        for (var definition : WindowBoxVariants.VALUES) {
            var block = ModBlocks.WINDOW_BOXES.get(definition.id()).get();
            var other = ModBlocks.WINDOW_BOXES.get(definition.wood().equals("oak") ? "spruce_window_box" : "oak_window_box").get();
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                Direction left = facing.getClockWise();
                BlockPos lp = base.relative(left), rp = base.relative(left.getOpposite());
                var state = block.defaultBlockState().setValue(WindowBoxBlock.FACING,facing);
                level.setBlockAndUpdate(base,state);
                level.setBlockAndUpdate(lp,state);
                level.setBlockAndUpdate(rp,state);
                check(helper, level.getBlockState(base).getValue(WindowBoxBlock.JOINED_LEFT)
                        && level.getBlockState(base).getValue(WindowBoxBlock.JOINED_RIGHT), "Middle joins both ends for " + definition.id());
                check(helper, !level.getBlockState(lp).getValue(WindowBoxBlock.JOINED_LEFT)
                        && !level.getBlockState(rp).getValue(WindowBoxBlock.JOINED_RIGHT), "Exposed ends keep caps");
                level.destroyBlock(base,false);
                check(helper, level.getBlockState(lp).is(block) && level.getBlockState(rp).is(block)
                        && !level.getBlockState(lp).getValue(WindowBoxBlock.JOINED_RIGHT)
                        && !level.getBlockState(rp).getValue(WindowBoxBlock.JOINED_LEFT), "Breaking middle leaves independent capped boxes");
                level.setBlockAndUpdate(base,state);
                level.setBlockAndUpdate(lp,other.defaultBlockState().setValue(WindowBoxBlock.FACING,facing));
                level.setBlockAndUpdate(rp,state.setValue(WindowBoxBlock.FACING,facing.getOpposite()));
                check(helper, !level.getBlockState(base).getValue(WindowBoxBlock.JOINED_LEFT)
                        && !level.getBlockState(base).getValue(WindowBoxBlock.JOINED_RIGHT), "Different woods and facings do not join");
                level.setBlockAndUpdate(lp,Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(rp,Blocks.AIR.defaultBlockState());
                var item = new ItemStack(block.asItem());
                player.setItemInHand(InteractionHand.MAIN_HAND,item);
                var hit = new BlockHitResult(Vec3.atCenterOf(base).add(left.getStepX()*.5, .4, left.getStepZ()*.5),left,base,false);
                check(helper,item.useOn(new UseOnContext(level,player,InteractionHand.MAIN_HAND,item,hit)).consumesAction(),"Clicking an end extends a box");
                check(helper,level.getBlockState(lp).is(block) && level.getBlockState(lp).getValue(WindowBoxBlock.FACING)==facing
                        && level.getBlockState(base).getValue(WindowBoxBlock.JOINED_LEFT),"Extension preserves facing and joins");
                var joined = state.setValue(WindowBoxBlock.JOINED_LEFT,true);
                check(helper, joined.rotate(Rotation.CLOCKWISE_90).getValue(WindowBoxBlock.FACING)==facing.getClockWise()
                        && joined.rotate(Rotation.CLOCKWISE_90).getValue(WindowBoxBlock.JOINED_LEFT),"Rotation preserves local joins");
                check(helper, joined.mirror(Mirror.LEFT_RIGHT).getValue(WindowBoxBlock.JOINED_RIGHT)
                        && !joined.mirror(Mirror.LEFT_RIGHT).getValue(WindowBoxBlock.JOINED_LEFT),"Reflection swaps end caps");
                for (var pos : List.of(base,lp,rp)) level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
            }
        }
        helper.succeed();
    }

    public static void everyWoodCraftsSixUnlocksAndDrops(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3,2,3));
        for (var definition : WindowBoxVariants.VALUES) {
            var id = Identifier.fromNamespaceAndPath(ATouchofClass.MODID,definition.id());
            var key = ResourceKey.create(Registries.RECIPE,id);
            var recipe = GameTestChecks.requireType(level.getServer().getRecipeManager().byKey(key).orElseThrow().value(),ShapedRecipe.class);
            var ingredients = new ArrayList<ItemStack>();
            for (int i=0;i<9;i++) ingredients.add(new ItemStack(i>=3 && i<6 ? Blocks.DIRT : definition.sourceBlock()));
            var input = CraftingInput.of(3,3,ingredients);
            var item = ModItems.WINDOW_BOX_ITEMS.get(definition.id()).get();
            check(helper,recipe.matches(input,level),"WWW/DDD/WWW must craft " + id);
            var result = recipe.assemble(input);
            check(helper,result.is(item) && result.getCount()==6,"Recipe yields six matching boxes");
            var mixed = new ArrayList<>(ingredients);
            mixed.set(0,new ItemStack(definition.wood().equals("oak") ? Blocks.SPRUCE_PLANKS : Blocks.OAK_PLANKS));
            check(helper,!recipe.matches(CraftingInput.of(3,3,mixed),level),"Mixed planks must not craft");
            mixed.set(0,ingredients.get(0)); mixed.set(4,new ItemStack(Blocks.COARSE_DIRT));
            check(helper,!recipe.matches(CraftingInput.of(3,3,mixed),level),"D must be ordinary dirt");
            var advancement = level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(ATouchofClass.MODID,"recipes/building_blocks/"+definition.id()));
            check(helper,advancement!=null && advancement.value().rewards().recipes().contains(key),"Matching recipe unlock loads");
            var state = ModBlocks.WINDOW_BOXES.get(definition.id()).get().defaultBlockState();
            level.setBlockAndUpdate(pos,state);
            var drops = Block.getDrops(state,level,pos,null);
            check(helper,drops.size()==1 && drops.getFirst().is(item) && drops.getFirst().getCount()==1,"Each box drops one matching box including its dirt");
            level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }
}
