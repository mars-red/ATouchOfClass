package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.ao.EnhancedBlockModelLighter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/** Uses ordinary block lighting, while retaining the overlay's single-quad geometry. */
public final class OverlayLightingRenderer {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final float SURFACE_OFFSET = 0.001F;
    private static final int MAX_CACHED_BLOCKS = 8192;
    private final Map<Long, CachedBlock> blocks = new LinkedHashMap<>(256, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, CachedBlock> entry) {
            return size() > MAX_CACHED_BLOCKS;
        }
    };
    private WeakReference<ClientLevel> cachedLevel = new WeakReference<>(null);
    private BlockModelLighter lighter;

    public List<RenderFace> extract(Minecraft mc, BlockPos pos, WallOverlayData data) {
        if (mc.level == null) return List.of();
        // Event subscribers are initialized during mod construction, before client
        // config is loaded. The lighter also owns thread-local render caches.
        if (lighter == null) lighter = EnhancedBlockModelLighter.newInstance();
        if (cachedLevel.get() != mc.level) {
            blocks.clear();
            cachedLevel = new WeakReference<>(mc.level);
        }
        CachedBlock cached = blocks.computeIfAbsent(pos.asLong(), key -> new CachedBlock());
        boolean changed = cached.updateModel(mc, data);
        BlockState state = mc.level.getBlockState(pos);
        long tick = mc.level.getGameTime();
        boolean smooth = mc.options.ambientOcclusion().get();
        // Refresh each tick so neighboring blocks and propagated light cannot leave stale shadows.
        if (changed || cached.tick != tick || cached.state != state || cached.smooth != smooth) {
            lighter.reset();
            boolean useAO = smooth && state.getLightEmission(mc.level, pos) == 0;
            for (Direction direction : DIRECTIONS) {
                LitFace face = cached.faces[direction.ordinal()];
                if (face == null) continue;
                if (useAO) {
                    lighter.prepareQuadAmbientOcclusion(mc.level, state, pos, face.quad, face.lighting);
                } else {
                    int light = lighter.getLightCoords(state, mc.level, pos.relative(direction));
                    lighter.prepareQuadFlat(mc.level, state, pos, light, face.quad, face.lighting);
                }
            }
            cached.tick = tick;
            cached.state = state;
            cached.smooth = smooth;
            cached.snapshot = cached.snapshotFaces();
        }
        return cached.snapshot;
    }

    private static BakedQuad bakeFace(Direction direction, TextureAtlasSprite sprite) {
        // Match vanilla's canonical winding for its per-corner ambient occlusion calculations.
        FaceInfo face = FaceInfo.fromFacing(direction);
        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f(1, 1, 1);
        Vector3fc[] positions = new Vector3fc[4];
        long[] uvs = new long[4];
        for (int vertex = 0; vertex < 4; vertex++) {
            Vector3f position = face.getVertexInfo(vertex).select(min, max);
            positions[vertex] = position;
            // Preserve the wallpaper orientation and full-face UVs on every direction.
            float u = switch (direction) {
                case NORTH, UP, DOWN -> position.x();
                case SOUTH -> 1 - position.x();
                case EAST -> position.z();
                case WEST -> 1 - position.z();
            };
            float v = direction.getAxis().isVertical() ? position.z() : 1 - position.y();
            uvs[vertex] = UVPair.pack(sprite.getU0() + u * (sprite.getU1() - sprite.getU0()),
                    sprite.getV0() + v * (sprite.getV1() - sprite.getV0()));
        }
        BakedQuad.MaterialInfo material = BakedQuad.MaterialInfo.of(
                new Material.Baked(sprite, false), Transparency.TRANSPARENT, -1, null, 0, true);
        return new BakedQuad(positions[0], positions[1], positions[2], positions[3],
                uvs[0], uvs[1], uvs[2], uvs[3], direction, material);
    }

    private static final class LitFace {
        final BakedQuad quad;
        final QuadInstance lighting = new QuadInstance();

        LitFace(BakedQuad quad) {
            this.quad = quad;
        }
    }

    private static final class CachedBlock {
        final LitFace[] faces = new LitFace[6];
        BlockState state;
        long tick = Long.MIN_VALUE;
        boolean smooth;
        List<RenderFace> snapshot = List.of();

        List<RenderFace> snapshotFaces() {
            List<RenderFace> result = new ArrayList<>(6);
            for (LitFace face : faces) {
                if (face == null) continue;
                int[] colors = new int[4];
                int[] lights = new int[4];
                for (int vertex = 0; vertex < 4; vertex++) {
                    colors[vertex] = face.lighting.getColor(vertex);
                    lights[vertex] = face.lighting.getLightCoords(vertex);
                }
                result.add(new RenderFace(face.quad, colors, lights));
            }
            return List.copyOf(result);
        }

        boolean updateModel(Minecraft mc, WallOverlayData data) {
            boolean changed = false;
            for (Direction direction : DIRECTIONS) {
                int index = direction.ordinal();
                var texture = data.getFace(direction);
                TextureAtlasSprite sprite = texture == null ? null
                        : mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
                LitFace old = faces[index];
                // Sprite identity also changes on resource reload: never retain stale atlas UVs.
                if ((old == null ? null : old.quad.materialInfo().sprite()) != sprite) {
                    faces[index] = sprite == null ? null : new LitFace(bakeFace(direction, sprite));
                    changed = true;
                }
            }
            return changed;
        }

    }

    /** A frame snapshot: drawing never reads the world or the mutable lighting cache. */
    public record RenderFace(BakedQuad quad, int[] colors, int[] lights) {
        public void draw(Matrix4f matrix, VertexConsumer consumer) {
            Direction direction = quad.direction();
            for (int vertex = 0; vertex < 4; vertex++) {
                Vector3fc position = quad.position(vertex);
                // Offset only the drawn surface, not the geometry used for light sampling.
                float x = position.x() + direction.getStepX() * SURFACE_OFFSET;
                float y = position.y() + direction.getStepY() * SURFACE_OFFSET;
                float z = position.z() + direction.getStepZ() * SURFACE_OFFSET;
                long uv = quad.packedUV(vertex);
                consumer.addVertex(matrix, x, y, z)
                        .setColor(colors[vertex])
                        .setUv(UVPair.unpackU(uv), UVPair.unpackV(uv))
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(lights[vertex])
                        .setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            }
        }
    }
}
