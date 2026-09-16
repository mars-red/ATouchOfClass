package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.gui.KeyCutterMenuScreen;
import com.digitaldownpour.atouchofclass.gui.KeyRingScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, ATouchofClass.MODID);
    public static final Supplier<MenuType<KeyCutterMenu>> KEY_CUTTER_MENU =
            MENU_TYPES.register("key_cutter_menu", () -> new MenuType<>(KeyCutterMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final Supplier<MenuType<KeyRingMenu>> KEY_RING_MENU =
            MENU_TYPES.register("key_ring", () -> IMenuTypeExtension.create(KeyRingMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(KEY_CUTTER_MENU.get(), KeyCutterMenuScreen::new);
        event.register(KEY_RING_MENU.get(), KeyRingScreen::new);
    }
}
