package com.digitaldownpour.atouchofclass.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class ClientWallOverlayData {
    private final static WallOverlayStore store =
            new WallOverlayStore();

    public static Map<ChunkPos, Map<BlockPos, WallOverlayData>> getChunks() {
        return store.getChunks();
    }

    public static void setFace(BlockPos pos, Direction face, ResourceLocation texture) {
        store.setFace(pos, face, texture);
    }
}
