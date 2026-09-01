package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import com.digitaldownpour.atouchofclass.network.WallOverlayChunkSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

import java.util.List;

@EventBusSubscriber(
        modid = ATouchofClass.MODID,
        bus = EventBusSubscriber.Bus.GAME
)
public final class WallOverlayChunkEvents {
    private WallOverlayChunkEvents() {
    }

    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        List<WallOverlayChunkSyncPacket.Entry> overlays = WallOverlaySavedData
                .get(event.getLevel())
                .getChunkSnapshot(event.getPos())
                .entrySet()
                .stream()
                .map(entry -> new WallOverlayChunkSyncPacket.Entry(entry.getKey(), entry.getValue()))
                .toList();

        ModNetworking.sendToPlayer(
                event.getPlayer(),
                new WallOverlayChunkSyncPacket(event.getPos(), overlays)
        );
    }
}
