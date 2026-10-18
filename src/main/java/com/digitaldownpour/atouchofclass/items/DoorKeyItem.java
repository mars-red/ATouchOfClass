package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class DoorKeyItem extends Item {

    public DoorKeyItem(Properties properties) {
        super(properties);
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
}
