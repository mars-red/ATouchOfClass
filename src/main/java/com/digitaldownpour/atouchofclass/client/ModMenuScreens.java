package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.ModMenus;
import com.digitaldownpour.atouchofclass.gui.KeyCutterMenuScreen;
import com.digitaldownpour.atouchofclass.gui.KeyRingScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ModMenuScreens {
    private ModMenuScreens() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.KEY_CUTTER_MENU.get(), KeyCutterMenuScreen::new);
        event.register(ModMenus.KEY_RING_MENU.get(), KeyRingScreen::new);
    }
}
