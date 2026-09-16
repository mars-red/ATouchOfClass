package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.KeyRingMenu;
import com.digitaldownpour.atouchofclass.ModComponents;
import com.digitaldownpour.atouchofclass.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.List;

public class KeyRingItem extends Item {
    public static final int CAPACITY = 16;

    public KeyRingItem(Properties properties) {
        super(properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    }

    public static boolean isCodedKey(ItemStack stack) {
        var info = stack.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE);
        return stack.is(ModItems.DOOR_KEY.get()) && info != null && !info.keyCode().isEmpty();
    }

    public static boolean matchesCode(ItemStack ring, String code) {
        return code != null && !code.isEmpty() && ring.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .stream().limit(CAPACITY).anyMatch(key -> isCodedKey(key)
                        && code.equals(key.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE).keyCode()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new KeyRingMenu(id, inventory, slot),
                    player.getItemInHand(hand).getHoverName()), buffer -> buffer.writeVarInt(slot));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        long count = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyStream().count();
        tooltip.add(Component.translatable("tooltip.atouchofclass.key_ring.contents", count, CAPACITY));
        tooltip.add(Component.translatable("tooltip.atouchofclass.key_ring.use"));
    }
}
