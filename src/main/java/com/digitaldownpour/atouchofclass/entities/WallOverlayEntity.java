package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.Nullable;

public class WallOverlayEntity extends BlockEntity {
    private ResourceLocation overlayTexture = ResourceLocation.fromNamespaceAndPath("atouchofclass", "block/wallpaper_default");

    public WallOverlayEntity(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(ModEntities.WALL_OVERLAY.get(), pos, state);
    }

    public void setTexture(ResourceLocation texture) {
        overlayTexture = texture;
        setChanged();
    }

    public ResourceLocation getTexture() {
        return overlayTexture;
    }
}
