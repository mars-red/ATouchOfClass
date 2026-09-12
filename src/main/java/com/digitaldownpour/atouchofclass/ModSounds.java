package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ATouchofClass.MODID);

    public static final Holder<SoundEvent> TALL_DOOR_OPEN =
            SOUNDS.register("tall_door_open", SoundEvent::createVariableRangeEvent);

    public static final Holder<SoundEvent> TALL_DOOR_CLOSE =
            SOUNDS.register("tall_door_close", SoundEvent::createVariableRangeEvent);

    public static final Holder<SoundEvent> ELEVATOR_DING =
            SOUNDS.register("elevator_ding", SoundEvent::createVariableRangeEvent);
}
