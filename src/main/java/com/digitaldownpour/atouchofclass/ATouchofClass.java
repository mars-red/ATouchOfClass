package com.digitaldownpour.atouchofclass;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(ATouchofClass.MODID)
public class ATouchofClass {
    public static final String MODID = "atouchofclass";

    public ATouchofClass(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC);
        ModComponents.register(bus);
        ModBlocks.register(bus);
        ModEntities.register(bus);
        ModItems.register(bus);
        ModCreativeTabs.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModMenus.register(bus);
        ModGameTests.register(bus);
    }
}
