package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.ModComponents;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class TallDoorItem extends BlockItem {

    public TallDoorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        KeyableInfo keyableInfo = stack.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE);

        if (keyableInfo != null) {
            String name = keyableInfo.keyableName();

            if (!(name == null || name.isEmpty())) {
                tooltipComponents.accept(Component.literal(name));
            }
        }
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);

        if (result.consumesAction()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            ItemStack stack = context.getItemInHand();

            if (stack.has(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE)) {
                if(level.getBlockEntity(pos) instanceof TallDoorBlockEntity be) {
                    be.setKeyableInfo(stack.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE));
                }
            }
        }

        return result;
    }
}
