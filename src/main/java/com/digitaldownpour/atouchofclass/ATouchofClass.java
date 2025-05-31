package com.digitaldownpour.atouchofclass;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;

@Mod(ATouchofClass.MODID)
public class ATouchofClass {
    public static final String MODID = "atouchofclass";

    public ATouchofClass(IEventBus bus) {
        ModComponents.register(bus);
        ModBlocks.register(bus);
        ModItems.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModMenus.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
