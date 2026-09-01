package com.digitaldownpour.atouchofclass.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static javax.swing.UIManager.get;

public class WallOverlayStore {
    protected final Map<
        ChunkPos,
        Map<BlockPos, WallOverlayData>
        > overlaysByChunk = new HashMap<>();

    public void setFace(
            BlockPos pos,
            Direction face,
            @Nullable ResourceLocation texture
    ) {
        if (face.getAxis().isVertical()) {
            return;
        }

        BlockPos immutablePos = pos.immutable();

        WallOverlayData overlay = getOrCreateOverlay(immutablePos);
        overlay.setFace(face, texture);

        /*
         * Avoid retaining an empty WallOverlayData after its final face
         * has been removed.
         */
        if (!overlay.hasAny()) {
            removeOverlay(immutablePos);
        }
    }

    public Map<BlockPos, WallOverlayData> getChunk(
            ChunkPos chunkPos
    ) {
        return overlaysByChunk.computeIfAbsent(chunkPos, ignored -> new HashMap<>());
    }

    public Map<ChunkPos, Map<BlockPos, WallOverlayData>> getChunks() {
        return overlaysByChunk;
    }

//    @javax.annotation.Nullable
//    public WallOverlayData getOverlay(BlockPos pos) {
//        BlockPos immutablePos = pos.immutable();
//        return getChunk(new ChunkPos(immutablePos)).get(pos.immutable());
//    }

    private WallOverlayData getOrCreateOverlay(BlockPos immutablePos) {

        return getChunk(new ChunkPos(immutablePos)).computeIfAbsent(
                immutablePos,
                ignored -> new WallOverlayData()
        );
    }

    public void setOverlay(BlockPos pos, WallOverlayData overlay) {
        BlockPos immutablePos = pos.immutable();
        Map<BlockPos, WallOverlayData> chunkOverlays = overlaysByChunk.computeIfAbsent(new ChunkPos(immutablePos), ignored -> new HashMap<>());

        if (overlay.hasAny()) {
            chunkOverlays.put(immutablePos, overlay);
        } else {
            chunkOverlays.remove(immutablePos);
        }
    }

    private void removeOverlay(BlockPos immutablePos) {
        ChunkPos chunkPos = new ChunkPos(immutablePos);
        Map<BlockPos, WallOverlayData> chunkOverlays = overlaysByChunk.get(chunkPos);

        if (chunkOverlays != null) {
            chunkOverlays.remove(immutablePos);

            if (chunkOverlays.isEmpty()) {
                overlaysByChunk.remove(chunkPos);
            }
        }
    }
}
