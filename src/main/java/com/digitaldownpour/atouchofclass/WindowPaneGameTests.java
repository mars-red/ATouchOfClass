package com.digitaldownpour.atouchofclass;

import java.util.List;
import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.blocks.TransomBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;

public final class WindowPaneGameTests {
    private static void check(GameTestHelper h, boolean value, String message) {
        h.assertTrue(value, Component.literal(message));
    }

    public static void pairsStaySeparateAndUpdate(GameTestHelper h) {
        var level = h.getLevel();
        var origin = h.absolutePos(new BlockPos(3, 5, 3));
        for (var definition : PatioDoorVariants.VALUES) {
            var block = ModBlocks.WINDOW_PANES.get(definition.wood()+"_window_pane").get();
            for (var door : List.of(ModBlocks.TALL_OAK_DOOR.get(), ModBlocks.TALL_OAK_PATIO_DOOR.get())) {
                for (Direction facing : Direction.Plane.HORIZONTAL) {
                    var across = facing.getClockWise();
                    for (boolean reverse : List.of(false, true)) {
                        for (int i=0;i<4;i++) {
                            var pos = origin.relative(across,i);
                            level.setBlockAndUpdate(pos.below(), door.defaultBlockState().setValue(TallDoorBlock.PART,2)
                                    .setValue(TallDoorBlock.FACING,facing)
                                    .setValue(TallDoorBlock.HINGE,i%2==0 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT));
                        }
                        for (int j=0;j<4;j++) {
                            int i = reverse ? 3-j : j;
                            level.setBlockAndUpdate(origin.relative(across,i),block.defaultBlockState().setValue(TransomBlock.FACING,facing));
                        }
                        for (int i=0;i<4;i++) check(h,level.getBlockState(origin.relative(across,i)).getValue(TransomBlock.JOIN)
                                == (i%2==0 ? TransomBlock.Join.LEFT : TransomBlock.Join.RIGHT),"Four doors must form two independent window pane pairs");
                        level.setBlockAndUpdate(origin.relative(across).below(),Blocks.AIR.defaultBlockState());
                        check(h,level.getBlockState(origin).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE
                                && level.getBlockState(origin.relative(across)).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE,
                                "Door removal must restore both end frames");
                        check(h,level.getBlockState(origin.relative(across,2)).getValue(TransomBlock.JOIN)==TransomBlock.Join.LEFT,
                                "Removing one pair must leave the other pair joined");
                        level.setBlockAndUpdate(origin.relative(across,3),Blocks.AIR.defaultBlockState());
                        check(h,level.getBlockState(origin.relative(across,2)).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE,
                                "Window pane removal must restore its partner frame");
                        for (int i=0;i<4;i++) {
                            var pos=origin.relative(across,i);
                            level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
                            level.setBlockAndUpdate(pos.below(),Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }
        h.succeed();
    }

    public static void unrelatedNeighborsStaySingle(GameTestHelper h) {
        var level=h.getLevel();
        var pos=h.absolutePos(new BlockPos(3,5,3));
        var block=ModBlocks.WINDOW_PANES.get("oak_window_pane").get();
        var partner=pos.east();
        var state=block.defaultBlockState();
        for (var support : List.of(Blocks.AIR,Blocks.STONE,ModBlocks.TALL_OAK_SIDELIGHT.get())) {
            level.setBlockAndUpdate(pos.below(),support.defaultBlockState());
            level.setBlockAndUpdate(partner.below(),support.defaultBlockState());
            level.setBlockAndUpdate(pos,state);
            level.setBlockAndUpdate(partner,state);
            check(h,level.getBlockState(pos).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE
                    && level.getBlockState(partner).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE,
                    "Standalone, solid-supported and sidelight window panes never merge");
            level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(partner,Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos.below(),Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(partner.below(),Blocks.AIR.defaultBlockState());
        }
        var door=ModBlocks.TALL_OAK_DOOR.get().defaultBlockState().setValue(TallDoorBlock.PART,2);
        level.setBlockAndUpdate(pos.below(),door);
        level.setBlockAndUpdate(partner.below(),door);
        level.setBlockAndUpdate(pos,state);
        level.setBlockAndUpdate(partner,state);
        check(h,level.getBlockState(pos).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE,"Same hinges cannot merge");
        level.setBlockAndUpdate(partner.below(),door.setValue(TallDoorBlock.HINGE,DoorHingeSide.RIGHT));
        check(h,level.getBlockState(pos).getValue(TransomBlock.JOIN)==TransomBlock.Join.LEFT,"Adding a valid pair below existing window panes must merge");
        level.setBlockAndUpdate(partner,ModBlocks.WINDOW_PANES.get("birch_window_pane").get().defaultBlockState());
        check(h,level.getBlockState(pos).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE,"Different window pane woods cannot merge");
        level.setBlockAndUpdate(partner,state.setValue(TransomBlock.FACING,Direction.SOUTH));
        check(h,level.getBlockState(pos).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE,"Different orientations cannot merge");
        h.succeed();
    }

    public static void placementAlignsAndNeedsNoDoor(GameTestHelper h) {
        var level=h.getLevel();
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        var pos=h.absolutePos(new BlockPos(3,5,3));
        for (var definition : PatioDoorVariants.VALUES) {
            var block=ModBlocks.WINDOW_PANES.get(definition.wood()+"_window_pane").get();
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                for (var support : List.of(Blocks.STONE.defaultBlockState(),
                        ModBlocks.TALL_OAK_DOOR.get().defaultBlockState().setValue(TallDoorBlock.PART,2).setValue(TallDoorBlock.FACING,facing),
                        ModBlocks.TALL_OAK_SIDELIGHT.get().defaultBlockState()
                                .setValue(com.digitaldownpour.atouchofclass.blocks.TallSidelightBlock.PART,2)
                                .setValue(com.digitaldownpour.atouchofclass.blocks.TallSidelightBlock.FACING,facing))) {
                    level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(pos.below(),support);
                    // Deliberately face away from the door to verify automatic alignment.
                    player.setYRot(support.is(Blocks.STONE) ? facing.getOpposite().toYRot() : facing.toYRot());
                    var item=new ItemStack(block.asItem());
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,item);
                    var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos.below()).add(0,.5,0),Direction.UP,pos.below(),false);
                    check(h,item.useOn(new net.minecraft.world.item.context.UseOnContext(level,player,
                            net.minecraft.world.InteractionHand.MAIN_HAND,item,hit)).consumesAction(),"Window pane item must place: "+definition.wood()+" "+facing+" above "+support);
                    var placed=level.getBlockState(pos);
                    check(h,placed.is(block) && placed.getValue(TransomBlock.FACING)==facing,"Placement must align with tall doors and sidelights");
                    var bounds=placed.getShape(level,pos).bounds();
                    check(h,bounds.getYsize()==1 && (facing.getAxis()==Direction.Axis.Z ? bounds.getZsize() : bounds.getXsize())==1.0/16,
                            "Full height window pane must occupy the centered one-unit depth");
                    boolean z=facing.getAxis()==Direction.Axis.Z;
                    check(h,(z ? bounds.minZ : bounds.minX)==7.5/16 && (z ? bounds.maxZ : bounds.maxX)==8.5/16,
                            "Pane must sit on the block center plane");
                    check(h,placed.getCollisionShape(level,pos).bounds().equals(bounds),"Selection and collision must match");
                    level.setBlockAndUpdate(pos.below(),Blocks.AIR.defaultBlockState());
                    check(h,level.getBlockState(pos).is(block),"Window panes must survive without a door or support");
                    level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
                }
            }
        }
        h.succeed();
    }

    public static void windowGridRetainsMuntins(GameTestHelper h) {
        var level=h.getLevel();
        var origin=h.absolutePos(new BlockPos(2,4,2));
        for (var definition : PatioDoorVariants.VALUES) {
            var block=ModBlocks.WINDOW_PANES.get(definition.wood()+"_window_pane").get();
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                var across=facing.getClockWise();
                for (int y=0;y<2;y++) for (int x=0;x<3;x++)
                    level.setBlockAndUpdate(origin.relative(across,x).above(y),block.defaultBlockState().setValue(TransomBlock.FACING,facing));
                for (int y=0;y<2;y++) for (int x=0;x<3;x++) {
                    var pos=origin.relative(across,x).above(y);
                    check(h,level.getBlockState(pos).getValue(TransomBlock.JOIN)==TransomBlock.Join.SINGLE,
                            "Every pane in a window grid must retain all four frame edges");
                }
                for (int y=1;y>=0;y--) for (int x=0;x<3;x++)
                    level.setBlockAndUpdate(origin.relative(across,x).above(y),Blocks.AIR.defaultBlockState());
            }
        }
        h.succeed();
    }

    public static void everyWoodCraftsFourUnlocksAndDrops(GameTestHelper h) {
        var level=h.getLevel();
        var pos=h.absolutePos(new BlockPos(3,5,3));
        for (var definition : PatioDoorVariants.VALUES) {
            String name=definition.wood()+"_window_pane";
            var id=Identifier.fromNamespaceAndPath(ATouchofClass.MODID,name);
            var key=ResourceKey.create(Registries.RECIPE,id);
            var recipe=GameTestChecks.requireType(level.getServer().getRecipeManager().byKey(key).orElseThrow().value(),ShapedRecipe.class);
            var slab=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(definition.wood()+"_slab"));
            var input=CraftingInput.of(3,1,List.of(new ItemStack(slab),new ItemStack(Blocks.GLASS),new ItemStack(slab)));
            check(h,recipe.matches(input,level),"SGS with matching slabs and a glass block must craft "+name);
            var block=ModBlocks.WINDOW_PANES.get(name).get();
            var result=recipe.assemble(input);
            check(h,result.is(block.asItem()) && result.getCount()==4,"Recipe must yield four matching window panes");
            var wrong=CraftingInput.of(3,1,List.of(new ItemStack(slab),new ItemStack(Blocks.GLASS_PANE),new ItemStack(slab)));
            check(h,!recipe.matches(wrong,level),"Glass panes must not satisfy the recipe");
            var advancement=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(ATouchofClass.MODID,"recipes/misc/"+name));
            check(h,advancement!=null && advancement.value().rewards().recipes().contains(key),"Recipe unlock must load");
            var state=block.defaultBlockState();
            var drops=Block.getDrops(state,level,pos,null);
            check(h,drops.size()==1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount()==1,"Each window pane drops one matching item");
        }
        h.succeed();
    }
}
