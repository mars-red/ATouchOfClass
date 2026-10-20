package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.digitaldownpour.atouchofclass.gui.ElevatorFloorSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public class ClientPayloadHandler {
    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(WallOverlayUpdatePacket.TYPE, ClientPayloadHandler::handleDataOnMain);
        event.register(WallOverlayChunkSyncPacket.TYPE, ClientPayloadHandler::handleDataOnMain);
        event.register(OpenElevatorFloorsPayload.TYPE, ClientPayloadHandler::handleDataOnMain);
    }

    public static void handleDataOnMain(final WallOverlayUpdatePacket data, final IPayloadContext context) {
        ClientWallOverlayData.setFace(data.pos(), data.face(), data.texture());
    }

    public static void handleDataOnMain(final WallOverlayChunkSyncPacket data, final IPayloadContext context) {
        Map<BlockPos, WallOverlayData> overlays = new HashMap<>();
        data.overlays().forEach(entry -> overlays.put(entry.pos(), entry.data()));
        ClientWallOverlayData.replaceChunk(data.chunkPos(), overlays);
    }

    public static void handleDataOnMain(final OpenElevatorFloorsPayload data, final IPayloadContext context) {
        Minecraft.getInstance().gui.setScreen(
                new ElevatorFloorSelectScreen(data.controlPos(), data.floorIndices(), data.currentFloorIndex()));
    }

}
