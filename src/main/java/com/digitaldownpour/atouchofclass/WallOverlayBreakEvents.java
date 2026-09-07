package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import com.digitaldownpour.atouchofclass.items.BlockOverlayItemBase;
import com.digitaldownpour.atouchofclass.network.WallOverlayUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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
        ResourceLocation texture = overlays.getFaceTexture(pos, face);
        if (texture == null) {
            return;
        }

        event.setCanceled(true);
        overlays.setFace(pos, face, null);
        ModNetworking.sendToTracking(
                level,
                pos,
                new WallOverlayUpdatePacket(pos, face, null, true)
        );

        dropOverlayItem(level, pos, texture, event.getEntity());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        for (var entry : WallOverlaySavedData.get(level).removeAllFaces(pos).entrySet()) {
            Direction face = entry.getKey();
            ModNetworking.sendToTracking(
                    level,
                    pos,
                    new WallOverlayUpdatePacket(pos, face, null, true)
            );

            dropOverlayItem(level, pos, entry.getValue(), event.getPlayer());
        }
    }

    /**
     * Drops the item corresponding to the given overlay texture at pos,
     * unless the responsible player is in creative mode.
     */
    private static void dropOverlayItem(
            ServerLevel level,
            BlockPos pos,
            ResourceLocation texture,
            Player player
    ) {
        if (player != null && player.getAbilities().instabuild) {
            return;
        }

        Item item = BlockOverlayItemBase.getItemForTexture(texture);
        if (item == null) {
            return;
        }

        Block.popResource(level, pos, new ItemStack(item));
    }
}
