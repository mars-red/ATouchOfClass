package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.common.DataMapHooks;

final class CopperApplianceFinish {
    private CopperApplianceFinish() {}

    static boolean isFinishChange(Block oldBlock, Block newBlock) {
        return DataMapHooks.getBlockWaxed(oldBlock) == newBlock
                || DataMapHooks.getBlockUnwaxed(oldBlock) == newBlock
                || WeatheringCopper.getNext(oldBlock).orElse(null) == newBlock
                || WeatheringCopper.getNext(newBlock).orElse(null) == oldBlock;
    }

    static boolean canApply(ItemStack stack, BlockState state) {
        if (stack.is(Items.HONEYCOMB)) {
            return HoneycombItem.getWaxed(state).isPresent();
        }
        return stack.is(ItemTags.AXES) && (DataMapHooks.getBlockUnwaxed(state.getBlock()) != null
                || WeatheringCopper.getPrevious(state).isPresent());
    }

    static void updatePartner(Level level, BlockPos pos, BlockState state, BlockState oldState,
                              EnumProperty<ChestType> type, Direction partnerDirection) {
        if (level.isClientSide() || state.getValue(type) == ChestType.SINGLE
                || !isFinishChange(oldState.getBlock(), state.getBlock())) {
            return;
        }
        BlockPos partnerPos = pos.relative(partnerDirection);
        BlockState partner = level.getBlockState(partnerPos);
        if (partner.is(oldState.getBlock()) && partner.getValue(type) == state.getValue(type).getOpposite()
                && partner.getValue(HorizontalDirectionalBlock.FACING)
                == state.getValue(HorizontalDirectionalBlock.FACING)) {
            replacePart(level, partnerPos, state.getBlock(), partner);
        }
    }

    static void replacePart(Level level, BlockPos pos, Block finish, BlockState part) {
        // The initiating part sends the neighbor updates after the assembly has the new finish.
        level.setBlock(pos, finish.withPropertiesOf(part),
                Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SKIP_ON_PLACE);
    }
}
