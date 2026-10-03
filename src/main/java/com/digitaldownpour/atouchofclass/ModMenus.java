package com.digitaldownpour.atouchofclass;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, ATouchofClass.MODID);
    public static final Supplier<MenuType<KeyCutterMenu>> KEY_CUTTER_MENU =
            MENU_TYPES.register("key_cutter_menu", () -> new MenuType<>(KeyCutterMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final Supplier<MenuType<KeyRingMenu>> KEY_RING_MENU =
            MENU_TYPES.register("key_ring", () -> IMenuTypeExtension.create(KeyRingMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
