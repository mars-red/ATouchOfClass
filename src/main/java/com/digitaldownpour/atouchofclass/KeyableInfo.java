package com.digitaldownpour.atouchofclass;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record KeyableInfo(String keyCode, String keyableName) {
    public static final Codec<KeyableInfo> BASIC_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("value1").forGetter(KeyableInfo::keyCode),
                    Codec.STRING.fieldOf("value2").forGetter(KeyableInfo::keyableName)
            ).apply(instance, KeyableInfo::new)
    );
    public static final StreamCodec<ByteBuf, KeyableInfo> BASIC_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(16), KeyableInfo::keyCode,
            ByteBufCodecs.stringUtf8(32), KeyableInfo::keyableName,
            KeyableInfo::new
    );

}
