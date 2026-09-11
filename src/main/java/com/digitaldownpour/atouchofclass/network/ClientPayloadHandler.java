package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.gui.ElevatorFloorSelectScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleDataOnMain(final SyncKeyCutterMenuPayload data, final IPayloadContext context) {
        //
    }

    public static void handleDataOnMain(final OpenElevatorFloorsPayload data, final IPayloadContext context) {
        Minecraft.getInstance().setScreen(
                new ElevatorFloorSelectScreen(data.controlPos(), data.floorIndices(), data.currentFloorIndex()));
    }

}
