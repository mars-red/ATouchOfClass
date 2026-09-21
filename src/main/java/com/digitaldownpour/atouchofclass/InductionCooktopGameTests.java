package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.InductionCooktopBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class InductionCooktopGameTests {
    @GameTest(template = "chandelier_test")
    public static void cooktopsJoinInPairsAndRejectAThirdConnection(GameTestHelper helper) {
        BlockPos firstBase = new BlockPos(2, 1, 3);
        BlockPos secondBase = firstBase.east();
        BlockPos thirdBase = secondBase.east();
        for (BlockPos base : new BlockPos[]{firstBase, secondBase, thirdBase}) {
            helper.setBlock(base, Blocks.STONE);
        }

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setYRot(0.0F);
        placeCooktop(helper, player, firstBase);
        placeCooktop(helper, player, secondBase);

        BlockPos first = helper.absolutePos(firstBase.above());
        BlockPos second = helper.absolutePos(secondBase.above());
        BlockState firstState = helper.getLevel().getBlockState(first);
        BlockState secondState = helper.getLevel().getBlockState(second);
        helper.assertTrue(firstState.is(ModBlocks.INDUCTION_COOKTOP.get())
                        && secondState.is(ModBlocks.INDUCTION_COOKTOP.get()),
                "both cooktops must be placed");
        helper.assertTrue(firstState.getValue(InductionCooktopBlock.TYPE) != ChestType.SINGLE
                        && secondState.getValue(InductionCooktopBlock.TYPE)
                        == firstState.getValue(InductionCooktopBlock.TYPE).getOpposite(),
                "two side-by-side cooktops must become matching halves");
        helper.assertTrue(first.relative(InductionCooktopBlock.getConnectedDirection(firstState)).equals(second)
                        && second.relative(InductionCooktopBlock.getConnectedDirection(secondState)).equals(first),
                "each joined half must point to the other half");

        placeCooktop(helper, player, thirdBase);
        BlockPos third = helper.absolutePos(thirdBase.above());
        BlockState thirdState = helper.getLevel().getBlockState(third);
        helper.assertTrue(thirdState.is(ModBlocks.INDUCTION_COOKTOP.get())
                        && thirdState.getValue(InductionCooktopBlock.TYPE) == ChestType.SINGLE,
                "a third adjacent cooktop must remain single");
        helper.assertTrue(helper.getLevel().getBlockState(second)
                        .getValue(InductionCooktopBlock.TYPE) != ChestType.SINGLE,
                "placing a third cooktop must not disturb the existing pair");

        helper.setBlock(firstBase.above(), Blocks.AIR);
        helper.assertTrue(helper.getLevel().getBlockState(second)
                        .getValue(InductionCooktopBlock.TYPE) == ChestType.SINGLE,
                "the remaining half must return to its single model when its partner is removed");
        helper.succeed();
    }

    private static void placeCooktop(GameTestHelper helper,
                                     net.minecraft.world.entity.player.Player player,
                                     BlockPos base) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.INDUCTION_COOKTOP_ITEM.get()));
        BlockPos absoluteBase = helper.absolutePos(base);
        helper.useBlock(base, player, new BlockHitResult(
                Vec3.atLowerCornerOf(absoluteBase).add(0.5, 1.0, 0.5),
                Direction.UP, absoluteBase, false));
    }
}
