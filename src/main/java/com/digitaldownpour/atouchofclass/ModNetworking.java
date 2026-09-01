package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.network.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    @SubscribeEvent // on the mod event bus
    public static void register(RegisterPayloadHandlersEvent event) {

        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                WallOverlayUpdatePacket.TYPE,
                WallOverlayUpdatePacket.STREAM_CODEC,
                WallOverlayUpdatePacket::handle
        );

        registrar.playToClient(
                WallOverlayChunkSyncPacket.TYPE,
                WallOverlayChunkSyncPacket.STREAM_CODEC,
                WallOverlayChunkSyncPacket::handle
        );
    }

    /**
     * Sends an authoritative update to every player currently tracking
     * the chunk containing the affected block.
     */
    public static void sendToTracking(
            ServerLevel level,
            BlockPos pos,
            CustomPacketPayload payload
    ) {
        PacketDistributor.sendToPlayersTrackingChunk(
                level,
                new ChunkPos(pos),
                payload
        );
    }

    /**
     * Sends an authoritative update to every player currently tracking
     * the specified chunk.
     */
    public static void sendToTracking(
            ServerLevel level,
            ChunkPos chunkPos,
            CustomPacketPayload payload
    ) {
        PacketDistributor.sendToPlayersTrackingChunk(
                level,
                chunkPos,
                payload
        );
    }

    /**
     * Sends a payload to one particular player.
     *
     * This is primarily useful for initial chunk snapshots.
     */
    public static void sendToPlayer(
            ServerPlayer player,
            CustomPacketPayload payload
    ) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
