package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/** Builds overlay quads on the main thread and lights them on the section meshing thread. */
public final class OverlayLightingRenderer {
    private static final float SURFACE_OFFSET = 0.001F;

    public static List<BakedQuad> snapshot(Minecraft mc, WallOverlayData data) {
        List<BakedQuad> faces = new ArrayList<>(6);
        for (Direction direction : Direction.values()) {
            var texture = data.getFace(direction);
            if (texture == null) continue;
            var sprite = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
            faces.add(bakeFace(direction, sprite));
        }
        return List.copyOf(faces);
    }

    public static void draw(BlockAndTintGetter region, BlockPos pos, List<BakedQuad> faces,
                            boolean smooth, BlockModelLighter lighter, VertexConsumer consumer) {
        BlockState state = region.getBlockState(pos);
        boolean useAO = smooth && state.getLightEmission(region, pos) == 0;
        lighter.reset();
        QuadInstance lighting = new QuadInstance();
        Matrix4f transform = new Matrix4f().translation(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        for (BakedQuad quad : faces) {
            Direction direction = quad.direction();
            if (useAO) {
                lighter.prepareQuadAmbientOcclusion(region, state, pos, quad, lighting);
            } else {
                int light = lighter.getLightCoords(state, region, pos.relative(direction));
                lighter.prepareQuadFlat(region, state, pos, light, quad, lighting);
            }
            for (int vertex = 0; vertex < 4; vertex++) {
                Vector3fc position = quad.position(vertex);
                long uv = quad.packedUV(vertex);
                consumer.addVertex(transform,
                                position.x() + direction.getStepX() * SURFACE_OFFSET,
                                position.y() + direction.getStepY() * SURFACE_OFFSET,
                                position.z() + direction.getStepZ() * SURFACE_OFFSET)
                        .setColor(lighting.getColor(vertex))
                        .setUv(UVPair.unpackU(uv), UVPair.unpackV(uv))
                        .setLight(lighting.getLightCoords(vertex))
                        .setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            }
        }
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

}
