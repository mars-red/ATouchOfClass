package com.digitaldownpour.atouchofclass.components;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class WallOverlaySavedData extends SavedData {
    private final WallOverlayStore store =
            new WallOverlayStore();

    private static final String DATA_NAME = "atouchofclass_wall_overlays";

    private static final String TAG_OVERLAYS = "overlays";
    private static final String TAG_POSITION = "position";
    private static final String TAG_DATA = "data";

    private static final Factory<WallOverlaySavedData> FACTORY =
            new Factory<>(
                    WallOverlaySavedData::new,
                    WallOverlaySavedData::load
            );

//    private final Map<BlockPos, WallOverlayData> overlays = new HashMap<>();

    public static WallOverlaySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                FACTORY,
                DATA_NAME
        );
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

    public Set<Direction> removeAllFaces(BlockPos pos) {
        Set<Direction> faces = store.getFaces(pos);
        if (faces.isEmpty()) {
            return faces;
        }

        for (Direction face : faces) {
            store.setFace(pos, face, null);
        }
        setDirty();
        return faces;
    }

    public Map<BlockPos, WallOverlayData> getChunkSnapshot(ChunkPos chunkPos) {
        return store.getChunkIfPresent(chunkPos);
    }

    public static WallOverlaySavedData load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        WallOverlaySavedData savedData =
                new WallOverlaySavedData();

        ListTag overlaysTag = tag.getList(
                TAG_OVERLAYS,
                Tag.TAG_COMPOUND
        );

        for (Tag element : overlaysTag) {
            if (!(element instanceof CompoundTag entryTag)) {
                continue;
            }

            if (!entryTag.contains(TAG_POSITION)
                    || !entryTag.contains(TAG_DATA)) {
                continue;
            }

            BlockPos pos = BlockPos.of(
                    entryTag.getLong(TAG_POSITION)
            );

            WallOverlayData overlay = WallOverlayData.load(
                    entryTag.getCompound(TAG_DATA)
            );

            if (overlay.hasAny()) {
                savedData.store.setOverlay(pos, overlay);
            }
        }

        return savedData;
    }

    @Override
    public @NotNull CompoundTag save(
            @NotNull CompoundTag tag,
            HolderLookup.@NotNull Provider registries
    ) {
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
