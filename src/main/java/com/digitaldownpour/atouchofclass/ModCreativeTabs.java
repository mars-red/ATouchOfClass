package com.digitaldownpour.atouchofclass;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ATouchofClass.MODID);

    // Icon-only item: not part of ModItems.ITEMS, so it is never pulled into the tab contents below.
    private static final DeferredRegister.Items ICON_ITEMS = DeferredRegister.createItems(ATouchofClass.MODID);
    private static final DeferredItem<Item> TAB_ICON =
            ICON_ITEMS.registerSimpleItem("tab_icon");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> A_TOUCH_OF_CLASS_TAB = CREATIVE_MODE_TABS.register(
            "a_touch_of_class",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.atouchofclass.a_touch_of_class"))
                    .icon(TAB_ICON::toStack)
                    .displayItems((parameters, output) ->
                        ModItems.ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
                    .build());

    public static void register(IEventBus eventBus) {
        ICON_ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
