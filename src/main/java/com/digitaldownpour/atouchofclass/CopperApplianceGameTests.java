package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.InductionCooktopBlock;
import com.digitaldownpour.atouchofclass.blocks.KitchenSinkBlock;
import com.digitaldownpour.atouchofclass.blocks.OvenBlock;
import com.digitaldownpour.atouchofclass.blocks.RefrigeratorBlock;
import com.digitaldownpour.atouchofclass.entities.MergedFixtureBlockEntity;
import com.digitaldownpour.atouchofclass.entities.OvenBlockEntity;
import com.digitaldownpour.atouchofclass.entities.RefrigeratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class CopperApplianceGameTests {
    public static void assembliesAndContentsSurviveFinishChanges(GameTestHelper helper) {
        checkPair(helper, new BlockPos(2, 1, 2), ModBlocks.COPPER_INDUCTION_COOKTOPS.getFirst().get(),
                InductionCooktopBlock.TYPE);
        checkPair(helper, new BlockPos(2, 1, 3), ModBlocks.COPPER_KITCHEN_SINKS.getFirst().get(),
                KitchenSinkBlock.TYPE);

        var level = helper.getLevel();
        BlockPos ovenPos = new BlockPos(2, 1, 4);
        var oven = ModBlocks.COPPER_OVENS.getFirst().get();
        var lit = oven.defaultBlockState().setValue(OvenBlock.LIT, true);
        helper.setBlock(ovenPos, lit);
        var ovenEntity = GameTestChecks.requireType(level.getBlockEntity(helper.absolutePos(ovenPos)), OvenBlockEntity.class);
        ovenEntity.setItem(0, new ItemStack(Items.RAW_IRON));
        helper.setBlock(ovenPos, ((WeatheringCopper) oven).getNext(lit).orElseThrow());
        helper.assertTrue(level.getBlockEntity(helper.absolutePos(ovenPos)) == ovenEntity
                        && ovenEntity.getItem(0).is(Items.RAW_IRON) && helper.getBlockState(ovenPos).getValue(OvenBlock.LIT),
                Component.literal("Oxidizing a lit oven must preserve its block entity and contents"));

        BlockPos relativeMaster = new BlockPos(5, 1, 3);
        BlockPos master = helper.absolutePos(relativeMaster);
        var fridge = ModBlocks.COPPER_REFRIGERATORS.getFirst().get();
        var fridgeState = fridge.defaultBlockState();
        helper.setBlock(relativeMaster.below(), Blocks.STONE);
        helper.setBlock(relativeMaster.below().west(), Blocks.STONE);
        helper.setBlock(relativeMaster, fridgeState);
        fridge.setPlacedBy(level, master, fridgeState, null, ItemStack.EMPTY);
        var fridgeEntity = GameTestChecks.requireType(level.getBlockEntity(master), RefrigeratorBlockEntity.class);
        fridgeEntity.setItem(53, new ItemStack(Items.GOLDEN_APPLE));
        BlockPos upperRight = RefrigeratorBlock.partPos(master, Direction.NORTH,
                RefrigeratorBlock.Layer.UPPER, RefrigeratorBlock.Side.RIGHT);
        var waxed = HoneycombItem.getWaxed(level.getBlockState(upperRight)).orElseThrow();
        level.setBlock(upperRight, waxed, Block.UPDATE_ALL_IMMEDIATE);
        for (var layer : RefrigeratorBlock.Layer.values()) {
            for (var side : RefrigeratorBlock.Side.values()) {
                var part = level.getBlockState(RefrigeratorBlock.partPos(master, Direction.NORTH, layer, side));
                helper.assertTrue(part.is(waxed.getBlock()) && part.getValue(RefrigeratorBlock.LAYER) == layer
                                && part.getValue(RefrigeratorBlock.SIDE) == side,
                        Component.literal("Waxing any refrigerator section must wax the entire assembly"));
            }
        }
        helper.assertTrue(level.getBlockEntity(master) == fridgeEntity && fridgeEntity.getItem(53).is(Items.GOLDEN_APPLE),
                Component.literal("Waxing a refrigerator must preserve its inventory"));
        helper.succeed();
    }

    private static void checkPair(GameTestHelper helper, BlockPos left, Block block, EnumProperty<ChestType> type) {
        BlockPos right = left.east();
        var level = helper.getLevel();
        helper.setBlock(left, block);
        helper.setBlock(right, block.defaultBlockState().setValue(type, ChestType.RIGHT));
        var waxed = HoneycombItem.getWaxed(helper.getBlockState(right)).orElseThrow();
        helper.setBlock(right, waxed);
        helper.assertTrue(helper.getBlockState(left).is(waxed.getBlock())
                        && helper.getBlockState(left).getValue(type) == ChestType.LEFT
                        && helper.getBlockState(right).getValue(type) == ChestType.RIGHT
                        && level.getBlockEntity(helper.absolutePos(left)) instanceof MergedFixtureBlockEntity
                        && level.getBlockEntity(helper.absolutePos(right)) instanceof MergedFixtureBlockEntity,
                Component.literal("Waxing a paired fixture must preserve both halves and their renderers"));
    }
}
