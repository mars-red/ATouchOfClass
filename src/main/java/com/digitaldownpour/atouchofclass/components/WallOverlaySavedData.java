package com.digitaldownpour.atouchofclass.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.Set;
import java.util.EnumMap;
import net.minecraft.world.level.saveddata.SavedDataType;

public class WallOverlaySavedData extends SavedData {
    private final WallOverlayStore store =
            new WallOverlayStore();

    private static final String DATA_NAME = "atouchofclass_wall_overlays";

    private static final String TAG_OVERLAYS = "overlays";
    private static final String TAG_POSITION = "position";
    private static final String TAG_DATA = "data";

    private static final SavedDataType<WallOverlaySavedData> TYPE =
            new SavedDataType<>(DATA_NAME, WallOverlaySavedData::new,
                    CompoundTag.CODEC.xmap(WallOverlaySavedData::load, WallOverlaySavedData::save));

//    private final Map<BlockPos, WallOverlayData> overlays = new HashMap<>();

    public static WallOverlaySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }



    public void setFace(
            BlockPos pos,
            Direction face,
            @org.jetbrains.annotations.Nullable ResourceLocation texture
    ) {
        store.setFace(pos, face, texture);
        setDirty();
    }

    public boolean hasFace(BlockPos pos, Direction face) {
        return store.getFace(pos, face) != null;
    }

    @org.jetbrains.annotations.Nullable
    public ResourceLocation getFaceTexture(BlockPos pos, Direction face) {
        return store.getFace(pos, face);
    }

    /**
     * Removes every overlay face at the given position, returning the
     * texture that was present on each removed face so callers can
     * determine which items to drop.
     */
    public Map<Direction, ResourceLocation> removeAllFaces(BlockPos pos) {
        Set<Direction> faces = store.getFaces(pos);
        if (faces.isEmpty()) {
            return Map.of();
        }

        Map<Direction, ResourceLocation> removed = new EnumMap<>(Direction.class);
        for (Direction face : faces) {
            ResourceLocation texture = store.getFace(pos, face);
            if (texture != null) {
                removed.put(face, texture);
            }
            store.setFace(pos, face, null);
        }
        setDirty();
        return removed;
    }

    public Map<BlockPos, WallOverlayData> getChunkSnapshot(ChunkPos chunkPos) {
        return store.getChunkIfPresent(chunkPos);
    }

    public static WallOverlaySavedData load(
            CompoundTag tag
    ) {
        WallOverlaySavedData savedData =
                new WallOverlaySavedData();

        ListTag overlaysTag = tag.getListOrEmpty(TAG_OVERLAYS);

        for (Tag element : overlaysTag) {
            if (!(element instanceof CompoundTag entryTag)) {
                continue;
            }

            if (!entryTag.contains(TAG_POSITION)
                    || !entryTag.contains(TAG_DATA)) {
                continue;
            }

            BlockPos pos = BlockPos.of(
                    entryTag.getLongOr(TAG_POSITION, 0L)
            );

            WallOverlayData overlay = WallOverlayData.load(
                    entryTag.getCompoundOrEmpty(TAG_DATA)
            );

            if (overlay.hasAny()) {
                savedData.store.setOverlay(pos, overlay);
            }
        }

        return savedData;
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag overlaysTag = new ListTag();

        for (Map.Entry<ChunkPos, Map<BlockPos, WallOverlayData>> chunkEntries
                : store.getChunks().entrySet()) {

            for (Map.Entry<BlockPos, WallOverlayData> entry
                    : chunkEntries.getValue().entrySet()) {

                WallOverlayData overlay = entry.getValue();

                if (!overlay.hasAny()) {
                    continue;
                }

                CompoundTag entryTag = new CompoundTag();

                entryTag.putLong(
                        TAG_POSITION,
                        entry.getKey().asLong()
                );

                entryTag.put(
                        TAG_DATA,
                        overlay.save()
                );

                overlaysTag.add(entryTag);
            }
        }

        tag.put(TAG_OVERLAYS, overlaysTag);
        return tag;
    }
}
