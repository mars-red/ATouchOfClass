package com.digitaldownpour.atouchofclass;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModComponents {
    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ATouchofClass.MODID);

    public static final Supplier<DataComponentType<KeyableInfo>> KEYABLE_INFO_DATA_COMPONENT_TYPE = REGISTRAR.registerComponentType(
            "keyable_info_data_component_type",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(KeyableInfo.BASIC_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(KeyableInfo.BASIC_STREAM_CODEC)
    );

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }

}
