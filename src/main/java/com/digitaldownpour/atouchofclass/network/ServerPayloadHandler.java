package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.KeyCutterMenu;
import com.digitaldownpour.atouchofclass.ModSounds;
import com.digitaldownpour.atouchofclass.elevator.ElevatorFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handleDataOnMain(final SyncKeyCutterMenuPayload data, final IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player
                && player.containerMenu instanceof KeyCutterMenu menu
                && menu.craft(data.keyCode(), data.keyableName())) {
            player.connection.send(new ClientboundSoundPacket(ModSounds.KEY_CUTTER, SoundSource.BLOCKS,
                    player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F, player.getRandom().nextLong()));
        }
    }

    @SuppressWarnings("EmptyMethod") // Client-bound packet; no server-side action is required.
    public static void handleDataOnMain(final WallOverlayChunkSyncPacket data, final IPayloadContext context) {
        //
    }

    public static void handleDataOnMain(final SelectElevatorFloorPayload data, final IPayloadContext context) {
        // Re-validated entirely fresh in ElevatorFlow - the client's selection is never trusted blindly.
        if (context.player() instanceof ServerPlayer serverPlayer) {
            ElevatorFlow.handleFloorSelected(serverPlayer.level(), data.controlPos(), data.targetFloorIndex());
        }
    }
}
