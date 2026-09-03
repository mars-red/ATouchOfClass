package com.digitaldownpour.atouchofclass.components;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

public class WallOverlayData {

    private final EnumMap<Direction, ResourceLocation> faces =
            new EnumMap<>(Direction.class);

    private static final String TAG_FACES = "faces";

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag facesTag = new CompoundTag();

        for (Direction direction : Direction.values()) {
            ResourceLocation texture = faces.get(direction);

            if (texture != null) {
                facesTag.putString(
                        direction.getSerializedName(),
                        texture.toString()
                );
            }
        }

        tag.put(TAG_FACES, facesTag);
        return tag;
    }

    public static WallOverlayData load(CompoundTag tag) {
        WallOverlayData data = new WallOverlayData();

        if (!tag.contains(TAG_FACES)) {
            return data;
        }

        CompoundTag facesTag = tag.getCompound(TAG_FACES);

        for (Direction direction : Direction.values()) {
            String key = direction.getSerializedName();

            if (!facesTag.contains(key)) {
                continue;
            }

            ResourceLocation texture =
                    ResourceLocation.tryParse(facesTag.getString(key));

            if (texture != null) {
                data.setFace(direction, texture);
            }
        }

        return data;
    }

    private static final StreamCodec<ByteBuf, Direction>
            DIRECTION_STREAM_CODEC = Direction.STREAM_CODEC;

    public record FaceEntry(
            Direction direction,
            ResourceLocation texture
    ) {
        public static final StreamCodec<ByteBuf, FaceEntry> STREAM_CODEC =
                StreamCodec.composite(
                        Direction.STREAM_CODEC,
                        FaceEntry::direction,

                        ResourceLocation.STREAM_CODEC,
                        FaceEntry::texture,

                        FaceEntry::new
                );
    }

    private static final StreamCodec<ByteBuf, List<FaceEntry>>
            FACE_LIST_STREAM_CODEC =
            FaceEntry.STREAM_CODEC.apply(
                    ByteBufCodecs.list(6)
            );

    public static final StreamCodec<ByteBuf, WallOverlayData> STREAM_CODEC =
            FACE_LIST_STREAM_CODEC.map(
                    WallOverlayData::fromNetworkEntries,
                    WallOverlayData::toNetworkEntries
            );

    public void setFace(
            Direction direction,
            @Nullable ResourceLocation texture
    ) {
        if (texture == null) {
            faces.remove(direction);
        } else {
            faces.put(direction, texture);
        }
    }

    @Nullable
    public ResourceLocation getFace(Direction direction) {
        return faces.get(direction);
    }

    public boolean hasAny() {
        return !faces.isEmpty();
    }

    public Set<Direction> getFaces() {
        return Collections.unmodifiableSet(faces.keySet());
    }

    private List<FaceEntry> toNetworkEntries() {
        return faces.entrySet()
                .stream()
                .map(entry -> new FaceEntry(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    private static WallOverlayData fromNetworkEntries(
            List<FaceEntry> entries
    ) {
        WallOverlayData data = new WallOverlayData();

        for (FaceEntry entry : entries) {
            Direction direction = entry.direction();

            data.setFace(
                    direction,
                    entry.texture()
            );
        }

        return data;
    }
}
