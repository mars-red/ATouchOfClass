package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.ModNetworking;
import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import com.digitaldownpour.atouchofclass.network.WallOverlayUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WallpaperItem extends Item {
    private final ResourceLocation wallpaperTexture;

    public WallpaperItem(Properties props, ResourceLocation tex) {
        super(props);
        this.wallpaperTexture = tex;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();

        // Only allow N/E/S/W
        if (face.getAxis().isVertical()) {
            return InteractionResult.PASS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        WallOverlaySavedData data = WallOverlaySavedData.get(serverLevel);
        data.setFace(pos, face, wallpaperTexture);

        // Send sync packet to nearby players
        boolean remove = (wallpaperTexture == null);
        WallOverlayUpdatePacket pkt = new WallOverlayUpdatePacket(pos, face, wallpaperTexture, remove);
        ModNetworking.sendToTracking(serverLevel, pos, pkt);

        return InteractionResult.SUCCESS_SERVER;
    }

}
