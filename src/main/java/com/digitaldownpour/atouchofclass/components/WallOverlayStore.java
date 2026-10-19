package com.digitaldownpour.atouchofclass.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WallOverlayStore {
    protected final Map<
        ChunkPos,
        Map<BlockPos, WallOverlayData>
        > overlaysByChunk = new HashMap<>();

    public void setFace(
            BlockPos pos,
            Direction face,
            @Nullable Identifier texture
    ) {
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

    public Map<BlockPos, WallOverlayData> getChunkIfPresent(ChunkPos chunkPos) {
        Map<BlockPos, WallOverlayData> overlays = overlaysByChunk.get(chunkPos);
        return overlays == null ? Map.of() : Map.copyOf(overlays);
    }

    public void replaceChunk(
            ChunkPos chunkPos,
            Map<BlockPos, WallOverlayData> overlays
    ) {
        if (overlays.isEmpty()) {
            overlaysByChunk.remove(chunkPos);
            return;
        }

        Map<BlockPos, WallOverlayData> replacement = new HashMap<>();
        overlays.forEach((pos, overlay) -> {
            BlockPos immutablePos = pos.immutable();
            if (ChunkPos.containing(immutablePos).equals(chunkPos) && overlay.hasAny()) {
                replacement.put(immutablePos, overlay);
            }
        });

        if (replacement.isEmpty()) {
            overlaysByChunk.remove(chunkPos);
        } else {
            overlaysByChunk.put(chunkPos, replacement);
        }
    }

    public Map<ChunkPos, Map<BlockPos, WallOverlayData>> getChunks() {
        return overlaysByChunk;
    }

    @Nullable
    public Identifier getFace(BlockPos pos, Direction face) {
        WallOverlayData overlay = getOverlay(pos);
        return overlay == null ? null : overlay.getFace(face);
    }

    public Set<Direction> getFaces(BlockPos pos) {
        WallOverlayData overlay = getOverlay(pos);
        return overlay == null ? Set.of() : Set.copyOf(overlay.getFaces());
    }

    @Nullable
    private WallOverlayData getOverlay(BlockPos pos) {
        Map<BlockPos, WallOverlayData> overlays = overlaysByChunk.get(ChunkPos.containing(pos));
        return overlays == null ? null : overlays.get(pos);
    }

    private WallOverlayData getOrCreateOverlay(BlockPos immutablePos) {

        return getChunk(ChunkPos.containing(immutablePos)).computeIfAbsent(
                immutablePos,
                ignored -> new WallOverlayData()
        );
    }

    public void setOverlay(BlockPos pos, WallOverlayData overlay) {
        BlockPos immutablePos = pos.immutable();

        if (overlay.hasAny()) {
            Map<BlockPos, WallOverlayData> chunkOverlays = overlaysByChunk.computeIfAbsent(ChunkPos.containing(immutablePos), ignored -> new HashMap<>());
            chunkOverlays.put(immutablePos, overlay);
        } else {
            removeOverlay(immutablePos);
        }
    }

    private void removeOverlay(BlockPos immutablePos) {
        ChunkPos chunkPos = ChunkPos.containing(immutablePos);
        Map<BlockPos, WallOverlayData> chunkOverlays = overlaysByChunk.get(chunkPos);

        if (chunkOverlays != null) {
            chunkOverlays.remove(immutablePos);

            if (chunkOverlays.isEmpty()) {
                overlaysByChunk.remove(chunkPos);
            }
        }
    }
}
