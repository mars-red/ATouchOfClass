package com.digitaldownpour.atouchofclass.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientWallOverlayData {
    private final static WallOverlayStore store =
            new WallOverlayStore();

    private static final Set<SectionPos> dirtySections = new HashSet<>();

    public static Map<ChunkPos, Map<BlockPos, WallOverlayData>> getChunks() {
        return store.getChunks();
    }

    public static void setFace(BlockPos pos, Direction face, @org.jetbrains.annotations.Nullable Identifier texture) {
        store.setFace(pos, face, texture);
        dirtySections.add(SectionPos.of(pos));
    }

    public static void replaceChunk(
            ChunkPos chunkPos,
            Map<BlockPos, WallOverlayData> overlays
    ) {
        // Include removed faces so a replacement sync also erases old baked geometry.
        store.getChunkIfPresent(chunkPos).keySet().forEach(pos -> dirtySections.add(SectionPos.of(pos)));
        store.replaceChunk(chunkPos, overlays);
        store.getChunkIfPresent(chunkPos).keySet().forEach(pos -> dirtySections.add(SectionPos.of(pos)));
    }

    public static Set<SectionPos> drainDirtySections() {
        Set<SectionPos> result = Set.copyOf(dirtySections);
        dirtySections.clear();
        return result;
    }

    public static void forgetChunk(ChunkPos chunkPos) {
        store.replaceChunk(chunkPos, Map.of());
        dirtySections.removeIf(section -> section.x() == chunkPos.x() && section.z() == chunkPos.z());
    }

    public static void clear() {
        store.getChunks().clear();
        dirtySections.clear();
    }
}
