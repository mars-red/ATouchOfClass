package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record WallOverlayChunkSyncPacket(
        ChunkPos chunkPos,
        List<Entry> overlays
) implements CustomPacketPayload {
    public record Entry(
            BlockPos pos,
            WallOverlayData data
    ) {
        public static final StreamCodec<
                RegistryFriendlyByteBuf,
                Entry
                > STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                Entry::pos,

                WallOverlayData.STREAM_CODEC,
                Entry::data,

                Entry::new
        );
    }

    private static final int MAX_OVERLAYS_PER_PACKET = 4096;
    public static final CustomPacketPayload.Type<WallOverlayChunkSyncPacket> TYPE = new WallOverlayChunkSyncPacket.Type<>(ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "wall_overlay_chunk_sync_packet"));

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "wall_overlay_chunk_sync");

    private static final StreamCodec<
            RegistryFriendlyByteBuf,
            List<Entry>
            > ENTRY_LIST_STREAM_CODEC =
            Entry.STREAM_CODEC.apply(
                    ByteBufCodecs.list(MAX_OVERLAYS_PER_PACKET)
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            WallOverlayChunkSyncPacket
            > STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.CHUNK_POS,
            WallOverlayChunkSyncPacket::chunkPos,

            ENTRY_LIST_STREAM_CODEC,
            WallOverlayChunkSyncPacket::overlays,

            WallOverlayChunkSyncPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public WallOverlayChunkSyncPacket {
        overlays = List.copyOf(overlays);
    }

    public static WallOverlayChunkSyncPacket read(RegistryFriendlyByteBuf buf) {
        return STREAM_CODEC.decode(buf);
    }

    public static void handle(final WallOverlayChunkSyncPacket data, final IPayloadContext context) {
        Map<BlockPos, WallOverlayData> overlays = new HashMap<>();
        data.overlays.forEach(entry -> overlays.put(entry.pos(), entry.data()));
        ClientWallOverlayData.replaceChunk(data.chunkPos, overlays);
    }

}
