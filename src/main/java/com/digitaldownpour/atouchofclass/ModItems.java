package com.digitaldownpour.atouchofclass;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ATouchofClass.MODID);

    //public static final DeferredItem<Item> DOOR_KEY = ITEMS.registerSimpleItem("door_key");
    public static final DeferredItem<DoorKeyItem> DOOR_KEY = ITEMS.registerItem(
            "door_key",
            DoorKeyItem::new,
            new Item.Properties()
                    //.component(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE.get(), new KeyableInfo("", ""))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

        eventBus.addListener(ModItems::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DOOR_KEY);
        }
    }
}
