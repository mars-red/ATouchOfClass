package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import com.digitaldownpour.atouchofclass.items.BlockOverlayItemBase;
import com.digitaldownpour.atouchofclass.network.WallOverlayUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(
        modid = ATouchofClass.MODID
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
        Identifier texture = overlays.getFaceTexture(pos, face);
        if (texture == null) {
            return;
        }

        event.setCanceled(true);
        overlays.setFace(pos, face, null);
        level.playSound(null, pos, SoundEvents.CINNABAR_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        ModNetworking.sendToTracking(
                level,
                pos,
                new WallOverlayUpdatePacket(pos, face, null, true)
        );

        dropOverlayItem(level, pos, texture, event.getEntity());
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        var removedFaces = WallOverlaySavedData.get(level).removeAllFaces(pos);
        if (!removedFaces.isEmpty()) {
            level.playSound(null, pos, SoundEvents.CINNABAR_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        for (var entry : removedFaces.entrySet()) {
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
     * unless it is consumed plaster or the responsible player is in creative mode.
     */
    private static void dropOverlayItem(
            ServerLevel level,
            BlockPos pos,
            Identifier texture,
            @org.jetbrains.annotations.Nullable Player player
    ) {
        if (player != null && player.getAbilities().instabuild) {
            return;
        }

        Item item = BlockOverlayItemBase.getItemForTexture(texture);
        // Applied plaster is consumed, whether stripped off or broken with its block.
        if (item == null || item == ModItems.BUCKET_OF_PLASTER.get()) {
            return;
        }

        Block.popResource(level, pos, new ItemStack(item));
    }
}
