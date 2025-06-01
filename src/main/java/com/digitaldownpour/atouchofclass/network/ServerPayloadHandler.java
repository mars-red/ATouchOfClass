package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.KeyCutterMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handleDataOnMain(final SyncKeyCutterMenuPayload data, final IPayloadContext context) {
        if (context.player().containerMenu instanceof KeyCutterMenu menu) {
            menu.craft(data.keyCode(), data.keyableName());
        }
    }
}
