package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.OvenBlock;
import com.digitaldownpour.atouchofclass.entities.OvenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class OvenGameTests {
    @GameTest(template = "chandelier_test")
    public static void ovenUsesVanillaFurnaceBehaviorAndLightsWhileBurning(GameTestHelper helper) {
        BlockPos relativePos = new BlockPos(4, 1, 3);
        BlockPos absolutePos = helper.absolutePos(relativePos);
        var unlitState = ModBlocks.OVEN.get().defaultBlockState()
                .setValue(OvenBlock.FACING, Direction.NORTH);
        helper.setBlock(relativePos, unlitState);

        helper.assertTrue(helper.getLevel().getBlockEntity(absolutePos) instanceof OvenBlockEntity,
                "the oven must create its furnace block entity");
        OvenBlockEntity oven = (OvenBlockEntity) helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(oven.getContainerSize() == 3,
                "the oven must have the vanilla furnace input, fuel, and output slots");

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        helper.assertTrue(oven.createMenu(1, player.getInventory(), player) instanceof FurnaceMenu,
                "the oven must open the vanilla furnace interface");
        helper.assertTrue(!unlitState.getValue(OvenBlock.LIT) && unlitState.getLightEmission() == 0,
                "an idle oven must not emit light");

        oven.setItem(0, new ItemStack(Items.RAW_IRON));
        oven.setItem(1, new ItemStack(Items.COAL));
        AbstractFurnaceBlockEntity.serverTick(helper.getLevel(), absolutePos, unlitState, oven);

        var litState = helper.getLevel().getBlockState(absolutePos);
        helper.assertTrue(litState.getValue(OvenBlock.LIT),
                "fuel and a valid smelting recipe must light the oven");
        helper.assertTrue(litState.getLightEmission() == 13,
                "a burning oven must emit the same level 13 light as a vanilla furnace");
        helper.succeed();
    }
}
