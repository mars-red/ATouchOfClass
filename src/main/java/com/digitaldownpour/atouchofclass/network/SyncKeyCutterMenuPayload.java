package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncKeyCutterMenuPayload(String keyCode, String keyableName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncKeyCutterMenuPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "key_cutter_menu_payload"));

    public static final StreamCodec<ByteBuf, SyncKeyCutterMenuPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncKeyCutterMenuPayload::keyCode,
            ByteBufCodecs.STRING_UTF8,
            SyncKeyCutterMenuPayload::keyableName,
            SyncKeyCutterMenuPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

