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

/**
 * Base item for placing an overlay on any face of a block.
 */
public abstract class BlockOverlayItemBase extends Item {
    private final ResourceLocation overlayTexture;

    protected BlockOverlayItemBase(Properties properties, ResourceLocation overlayTexture) {
        super(properties);
        this.overlayTexture = overlayTexture;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide) {
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
