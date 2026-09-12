package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.KeyCutterMenu;
import com.digitaldownpour.atouchofclass.elevator.ElevatorFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handleDataOnMain(final SyncKeyCutterMenuPayload data, final IPayloadContext context) {
        if (context.player().containerMenu instanceof KeyCutterMenu menu) {
            menu.craft(data.keyCode(), data.keyableName());
        }
    }

    public static void handleDataOnMain(final WallOverlayChunkSyncPacket data, final IPayloadContext context) {
        //
    }

    public static void handleDataOnMain(final SelectElevatorFloorPayload data, final IPayloadContext context) {
        // Re-validated entirely fresh in ElevatorFlow - the client's selection is never trusted blindly.
        if (context.player() instanceof ServerPlayer serverPlayer && serverPlayer.level() instanceof ServerLevel serverLevel) {
            ElevatorFlow.handleFloorSelected(serverLevel, data.controlPos(), data.targetFloorIndex());
        }
    }
}
