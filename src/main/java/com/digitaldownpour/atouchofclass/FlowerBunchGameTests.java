package com.digitaldownpour.atouchofclass;

import java.util.List;
import com.digitaldownpour.atouchofclass.blocks.FlowerBunchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.JsonOps;

public final class FlowerBunchGameTests {
    private static void check(GameTestHelper helper, boolean result, String message) {
        helper.assertTrue(result, Component.literal(message));
    }

    private static net.minecraft.world.InteractionResult plant(GameTestHelper helper,
            net.minecraft.world.entity.player.Player player, ItemStack stack, BlockPos box, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var hit = new BlockHitResult(Vec3.atCenterOf(box).add(face.getStepX()*.5,face.getStepY()*.5,face.getStepZ()*.5),face,box,false);
        return stack.useOn(new UseOnContext(helper.getLevel(),player,InteractionHand.MAIN_HAND,stack,hit));
    }

    public static void everyFlowerPlantsInEveryWindowBox(GameTestHelper helper) {
        var level=helper.getLevel();
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos box=helper.absolutePos(new BlockPos(3,2,3));
        level.setBlockAndUpdate(box.above(),Blocks.AIR.defaultBlockState());
        for (var flower : FlowerBunchVariants.VALUES) for (var wood : WindowBoxVariants.VALUES) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                var support=ModBlocks.WINDOW_BOXES.get(wood.id()).get().defaultBlockState().setValue(FlowerBunchBlock.FACING,facing);
                level.setBlockAndUpdate(box,support);
                Direction click=wood.wood().equals("oak") ? Direction.UP : facing;
                var stack=new ItemStack(ModItems.FLOWER_BUNCH_ITEMS.get(flower.id()).get(),2);
                check(helper,plant(helper,player,stack,box,click).consumesAction(),"Must plant "+flower.id()+" in "+wood.id());
                var state=level.getBlockState(box.above());
                check(helper,state.is(ModBlocks.FLOWER_BUNCHES.get(flower.id()).get()) && state.getValue(FlowerBunchBlock.FACING)==facing,
                        "Bunch follows planter facing, including outward sunflower heads");
                check(helper,stack.getCount()==1 && level.getBlockState(box).is(support.getBlock()),"Planting consumes one bunch and keeps the box");
                check(helper,state.getCollisionShape(level,box.above()).isEmpty(),"Flowers have no collision");
                var bounds=state.getShape(level,box.above()).bounds();
                check(helper,bounds.minY==-.125 && bounds.maxY==(flower.height()-2)/16.0,"Stems meet soil and selection fits flower height");
                var saved=BlockState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
                check(helper,BlockState.CODEC.parse(JsonOps.INSTANCE,saved).getOrThrow()==state,"Planted flower type and facing survive serialization");
                level.setBlockAndUpdate(box.above(),Blocks.AIR.defaultBlockState());
            }
        }
        var creative=helper.makeMockPlayer(GameType.CREATIVE);
        creative.getAbilities().instabuild=true;
        var stack=new ItemStack(ModItems.FLOWER_BUNCH_ITEMS.get("peony_bunch").get(),2);
        check(helper,plant(helper,creative,stack,box,Direction.UP).consumesAction() && stack.getCount()==2,"Creative planting keeps the held stack");
        helper.succeed();
    }

    public static void rejectsGroundOccupiedBoxesAndExcludedPlants(GameTestHelper helper) {
        var level=helper.getLevel();
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos box=helper.absolutePos(new BlockPos(3,2,3));
        for (var flower : FlowerBunchVariants.VALUES) {
            var stack=new ItemStack(ModItems.FLOWER_BUNCH_ITEMS.get(flower.id()).get(),3);
            for (var ground : List.of(Blocks.DIRT,Blocks.GRASS_BLOCK,Blocks.FARMLAND,Blocks.FLOWER_POT,Blocks.STONE)) {
                level.setBlockAndUpdate(box.above(),Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(box,ground.defaultBlockState());
                check(helper,!plant(helper,player,stack,box,Direction.UP).consumesAction() && stack.getCount()==3,"Bunches are planter-only");
            }
            level.setBlockAndUpdate(box,ModBlocks.OAK_WINDOW_BOX.get().defaultBlockState());
            level.setBlockAndUpdate(box.above(),Blocks.STONE.defaultBlockState());
            check(helper,!plant(helper,player,stack,box,Direction.UP).consumesAction() && stack.getCount()==3,"Blocked planting space must fail without consumption");
            level.setBlockAndUpdate(box.above(),Blocks.AIR.defaultBlockState());
            check(helper,plant(helper,player,stack,box,Direction.NORTH).consumesAction(),"Clicking the box side plants above it");
            var planted=level.getBlockState(box.above());
            check(helper,!plant(helper,player,stack,box,Direction.UP).consumesAction() && stack.getCount()==2
                    && level.getBlockState(box.above())==planted,"One bunch per box, no replacement or extra consumption");
            level.setBlockAndUpdate(box.above(),Blocks.AIR.defaultBlockState());
        }
        for (String excluded : List.of("mangrove_propagule","spore_blossom","azalea","flowering_azalea","chorus_flower","flowering_azalea_leaves","cherry_leaves"))
            check(helper,!ModBlocks.FLOWER_BUNCHES.containsKey(excluded+"_bunch"),"Excluded plants must have no bunch: "+excluded);
        helper.succeed();
    }

    public static void harvestAndSupportLossDropExactlyOneBunch(GameTestHelper helper) {
        var level=helper.getLevel();
        BlockPos box=helper.absolutePos(new BlockPos(3,2,3));
        BlockPos neighbor=box.east();
        var area=new AABB(box).inflate(3);
        for (var flower : FlowerBunchVariants.VALUES) {
            level.getEntitiesOfClass(ItemEntity.class,area).forEach(ItemEntity::discard);
            var support=ModBlocks.OAK_WINDOW_BOX.get().defaultBlockState().setValue(FlowerBunchBlock.FACING,Direction.SOUTH);
            var bunch=ModBlocks.FLOWER_BUNCHES.get(flower.id()).get().defaultBlockState();
            var item=ModItems.FLOWER_BUNCH_ITEMS.get(flower.id()).get();
            level.setBlockAndUpdate(box,support);level.setBlockAndUpdate(neighbor,support);
            level.setBlockAndUpdate(box.above(),bunch);level.setBlockAndUpdate(neighbor.above(),bunch);
            level.destroyBlock(box.above(),true);
            var drops=level.getEntitiesOfClass(ItemEntity.class,area,e->e.getItem().is(item));
            check(helper,drops.size()==1 && drops.getFirst().getItem().getCount()==1
                    && level.getBlockState(box).is(support.getBlock()) && level.getBlockState(neighbor.above()).is(bunch.getBlock()),
                    "Harvest drops one bunch and leaves both planter and adjacent flowers intact");
            level.getEntitiesOfClass(ItemEntity.class,area).forEach(ItemEntity::discard);
            level.setBlockAndUpdate(box.above(),bunch);
            level.destroyBlock(box,true);
            drops=level.getEntitiesOfClass(ItemEntity.class,area,e->e.getItem().is(item));
            var boxDrops=level.getEntitiesOfClass(ItemEntity.class,area,e->e.getItem().is(ModItems.OAK_WINDOW_BOX.get()));
            check(helper,level.getBlockState(box.above()).isAir() && drops.size()==1 && drops.getFirst().getItem().getCount()==1
                    && boxDrops.size()==1,"Breaking a planter releases exactly its box and bunch");
            check(helper,level.getBlockState(neighbor.above()).is(bunch.getBlock()),"Neighbor flowers survive support removal elsewhere");
            level.setBlockAndUpdate(neighbor.above(),Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(neighbor,Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    public static void fourMatchingFlowersCraftOneAndUnlock(GameTestHelper helper) {
        var level=helper.getLevel();
        for (var flower : FlowerBunchVariants.VALUES) {
            var id=Identifier.fromNamespaceAndPath(ATouchofClass.MODID,flower.id());
            var key=ResourceKey.create(Registries.RECIPE,id);
            var recipe=GameTestChecks.requireType(level.getServer().getRecipeManager().byKey(key).orElseThrow().value(),ShapedRecipe.class);
            var f=new ItemStack(flower.sourceBlock());
            var input=CraftingInput.of(2,2,List.of(f,f,f,f));
            check(helper,recipe.matches(input,level),"Four matching flowers craft "+id);
            var output=recipe.assemble(input);
            check(helper,output.is(ModItems.FLOWER_BUNCH_ITEMS.get(flower.id()).get()) && output.getCount()==1,"Recipe yields one bunch");
            var different=new ItemStack(flower.flower().equals("poppy") ? Blocks.DANDELION : Blocks.POPPY);
            check(helper,!recipe.matches(CraftingInput.of(2,2,List.of(different,f,f,f)),level)
                    && !recipe.matches(CraftingInput.of(1,4,List.of(f,f,f,f)),level),"Mixed flowers and line recipes must fail");
            var advancement=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(ATouchofClass.MODID,"recipes/decorations/"+flower.id()));
            check(helper,advancement!=null && advancement.value().rewards().recipes().contains(key),"Recipe unlock must load");
        }
        helper.succeed();
    }
}
