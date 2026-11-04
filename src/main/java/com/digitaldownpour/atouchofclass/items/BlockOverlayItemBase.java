package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.ModNetworking;
import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import com.digitaldownpour.atouchofclass.network.WallOverlayUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base item for placing an overlay on any face of a block.
 */
public abstract class BlockOverlayItemBase extends Item {
    private static final Map<Identifier, Item> ITEMS_BY_TEXTURE =
            new ConcurrentHashMap<>();

    private final Identifier overlayTexture;

    protected BlockOverlayItemBase(Properties properties, Identifier overlayTexture) {
        super(properties);
        this.overlayTexture = overlayTexture;
        registerOverlayTexture(overlayTexture);
    }

    protected final void registerOverlayTexture(Identifier texture) {
        ITEMS_BY_TEXTURE.put(texture, this);
    }

    protected Identifier getOverlayTexture(UseOnContext context) {
        return overlayTexture;
    }

    /**
     * Looks up the overlay item responsible for placing the given overlay
     * texture, so the corresponding item can be dropped when the overlay
     * is removed from a block.
     */
    @Nullable
    public static Item getItemForTexture(@Nullable Identifier texture) {
        return texture == null ? null : ITEMS_BY_TEXTURE.get(texture);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        ServerLevel serverLevel = (ServerLevel) level;
        Identifier overlayTexture = getOverlayTexture(context);

        WallOverlaySavedData.get(serverLevel).setFace(pos, face, overlayTexture);

        boolean remove = overlayTexture == null;
        serverLevel.playSound(null, pos, remove ? SoundEvents.CINNABAR_BREAK : SoundEvents.BRUSH_GENERIC,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        WallOverlayUpdatePacket packet = new WallOverlayUpdatePacket(pos, face, overlayTexture, remove);
        ModNetworking.sendToTracking(serverLevel, pos, packet);

        return InteractionResult.SUCCESS_SERVER;
    }
}
