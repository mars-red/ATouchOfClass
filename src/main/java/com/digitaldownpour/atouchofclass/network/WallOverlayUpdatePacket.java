package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record WallOverlayUpdatePacket(
        BlockPos pos,
        Direction face,
        @Nullable ResourceLocation texture,
        boolean remove
) implements CustomPacketPayload {

    public static final Type<WallOverlayUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "key_cutter_menu_payload"));

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "walloverlay_sync");

    public static final StreamCodec<FriendlyByteBuf, WallOverlayUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, WallOverlayUpdatePacket::pos,      // pos
            Direction.STREAM_CODEC, WallOverlayUpdatePacket::face,    // face
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), (WallOverlayUpdatePacket pkt) -> Optional.ofNullable(pkt.texture), // texture (nullable)
            ByteBufCodecs.BOOL, WallOverlayUpdatePacket::remove,      // remove flag
            (pos, face, texOpt, remove) -> new WallOverlayUpdatePacket(pos, face, texOpt.orElse(null), remove)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static WallOverlayUpdatePacket read(FriendlyByteBuf buf) {
//        BlockPos p = buf.readBlockPos();
//        Direction f = buf.readEnum(Direction.class);
//        boolean rm = buf.readBoolean();
//        ResourceLocation tex = rm ? null : buf.readResourceLocation();
//        return new WallOverlayUpdatePacket(p, f, tex, rm);
        return STREAM_CODEC.decode(buf);
    }

    public static void handle(final WallOverlayUpdatePacket data, final IPayloadContext context) {
        ClientWallOverlayData.setFace(data.pos, data.face, data.texture);
    }

}
