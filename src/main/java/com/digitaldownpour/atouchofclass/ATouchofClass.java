package com.digitaldownpour.atouchofclass;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ATouchofClass.MODID)
public class ATouchofClass {
    public static final String MODID = "atouchofclass";

    public ATouchofClass(IEventBus bus) {
        ModComponents.register(bus);
        ModBlocks.register(bus);
        ModEntities.register(bus);
        ModItems.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModMenus.register(bus);
    }
}
