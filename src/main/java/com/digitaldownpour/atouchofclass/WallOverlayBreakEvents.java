package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import com.digitaldownpour.atouchofclass.network.WallOverlayUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(
        modid = ATouchofClass.MODID,
        bus = EventBusSubscriber.Bus.GAME
)
public final class WallOverlayBreakEvents {
    private WallOverlayBreakEvents() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Direction face = event.getFace();
        if (face == null) {
            return;
        }

        BlockPos pos = event.getPos();
        WallOverlaySavedData overlays = WallOverlaySavedData.get(level);
        if (!overlays.hasFace(pos, face)) {
            return;
        }

        event.setCanceled(true);
        overlays.setFace(pos, face, null);
        ModNetworking.sendToTracking(
                level,
                pos,
                new WallOverlayUpdatePacket(pos, face, null, true)
        );
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        for (Direction face : WallOverlaySavedData.get(level).removeAllFaces(pos)) {
            ModNetworking.sendToTracking(
                    level,
                    pos,
                    new WallOverlayUpdatePacket(pos, face, null, true)
            );
        }
    }
}
