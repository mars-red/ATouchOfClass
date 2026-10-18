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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base item for placing an overlay on any face of a block.
 */
public abstract class BlockOverlayItemBase extends Item {
    private static final Map<ResourceLocation, Item> ITEMS_BY_TEXTURE =
            new ConcurrentHashMap<>();

    private final ResourceLocation overlayTexture;

    protected BlockOverlayItemBase(Properties properties, ResourceLocation overlayTexture) {
        super(properties);
        this.overlayTexture = overlayTexture;
        ITEMS_BY_TEXTURE.put(overlayTexture, this);
    }

    /**
     * Looks up the overlay item responsible for placing the given overlay
     * texture, so the corresponding item can be dropped when the overlay
     * is removed from a block.
     */
    @Nullable
    public static Item getItemForTexture(@Nullable ResourceLocation texture) {
        return texture == null ? null : ITEMS_BY_TEXTURE.get(texture);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        ServerLevel serverLevel = (ServerLevel) level;

        WallOverlaySavedData.get(serverLevel).setFace(pos, face, overlayTexture);

        boolean remove = overlayTexture == null;
        WallOverlayUpdatePacket packet = new WallOverlayUpdatePacket(pos, face, overlayTexture, remove);
        ModNetworking.sendToTracking(serverLevel, pos, packet);

        return InteractionResult.SUCCESS_SERVER;
    }
}
