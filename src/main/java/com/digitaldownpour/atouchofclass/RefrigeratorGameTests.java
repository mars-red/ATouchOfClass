package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.RefrigeratorBlock;
import com.digitaldownpour.atouchofclass.entities.RefrigeratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class RefrigeratorGameTests {
    @GameTest(template = "chandelier_test")
    public static void refrigeratorBuildsSixPartsWithOnePersistentDoubleChestInventory(GameTestHelper helper) {
        RefrigeratorBlock block = ModBlocks.REFRIGERATOR.get();
        BlockPos relativeMaster = new BlockPos(4, 1, 3);
        BlockPos absoluteMaster = helper.absolutePos(relativeMaster);
        Direction facing = Direction.NORTH;

        helper.setBlock(relativeMaster.below(), Blocks.STONE);
        helper.setBlock(relativeMaster.below().west(), Blocks.STONE);

        var masterState = block.defaultBlockState()
                .setValue(RefrigeratorBlock.FACING, facing)
                .setValue(RefrigeratorBlock.LAYER, RefrigeratorBlock.Layer.LOWER)
                .setValue(RefrigeratorBlock.SIDE, RefrigeratorBlock.Side.LEFT);
        helper.setBlock(relativeMaster, masterState);
        block.setPlacedBy(helper.getLevel(), absoluteMaster, masterState, null, ItemStack.EMPTY);

        int blockEntityCount = 0;
        for (RefrigeratorBlock.Layer layer : RefrigeratorBlock.Layer.values()) {
            for (RefrigeratorBlock.Side side : RefrigeratorBlock.Side.values()) {
                BlockPos partPos = RefrigeratorBlock.partPos(absoluteMaster, facing, layer, side);
                var state = helper.getLevel().getBlockState(partPos);
                helper.assertTrue(state.is(block), "every refrigerator section must be placed");
                helper.assertTrue(state.getValue(RefrigeratorBlock.LAYER) == layer,
                        "each section must have the correct layer");
                helper.assertTrue(state.getValue(RefrigeratorBlock.SIDE) == side,
                        "each section must have the correct side");
                if (helper.getLevel().getBlockEntity(partPos) != null) blockEntityCount++;
            }
        }
        helper.assertTrue(blockEntityCount == 1, "only the lower-left section may own storage");

        var blockEntity = helper.getLevel().getBlockEntity(absoluteMaster);
        helper.assertTrue(blockEntity instanceof RefrigeratorBlockEntity,
                "the lower-left section must create the refrigerator block entity");
        RefrigeratorBlockEntity refrigerator = (RefrigeratorBlockEntity) blockEntity;
        helper.assertTrue(refrigerator.getContainerSize() == 54,
                "the refrigerator must have the same 54-slot capacity as a double chest");

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        helper.assertTrue(refrigerator.createMenu(1, player.getInventory(), player) instanceof ChestMenu,
                "the refrigerator must open the standard six-row chest menu");

        refrigerator.setItem(53, new ItemStack(Items.GOLDEN_APPLE, 2));
        var saved = refrigerator.saveWithoutMetadata(helper.getLevel().registryAccess());
        var restored = new RefrigeratorBlockEntity(absoluteMaster, masterState);
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.getItem(53).is(Items.GOLDEN_APPLE)
                        && restored.getItem(53).getCount() == 2,
                "all 54 slots must persist through saving and loading");

        refrigerator.clearContent();
        BlockPos upperRight = RefrigeratorBlock.partPos(
                absoluteMaster, facing, RefrigeratorBlock.Layer.UPPER, RefrigeratorBlock.Side.RIGHT);
        helper.getLevel().destroyBlock(upperRight, false);
        for (RefrigeratorBlock.Layer layer : RefrigeratorBlock.Layer.values()) {
            for (RefrigeratorBlock.Side side : RefrigeratorBlock.Side.values()) {
                helper.assertTrue(helper.getLevel().getBlockState(
                                RefrigeratorBlock.partPos(absoluteMaster, facing, layer, side)).isAir(),
                        "breaking any section must remove the entire refrigerator");
            }
        }
        helper.assertTrue(countDroppedRefrigerators(helper, absoluteMaster) == 0,
                "creative-style removal must not drop a refrigerator item");

        helper.setBlock(relativeMaster, masterState);
        block.setPlacedBy(helper.getLevel(), absoluteMaster, masterState, null, ItemStack.EMPTY);
        helper.getLevel().destroyBlock(upperRight, true);
        helper.assertTrue(countDroppedRefrigerators(helper, absoluteMaster) == 1,
                "survival-style removal must drop exactly one refrigerator item");
        helper.succeed();
    }

    private static int countDroppedRefrigerators(GameTestHelper helper, BlockPos masterPos) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(masterPos).inflate(4),
                        entity -> entity.getItem().is(ModItems.REFRIGERATOR_ITEM.get()))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }
}
