package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import com.mojang.math.Quadrant;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.data.AtlasIds;

/** Uses the ordinary block renderer for lighting, while retaining the overlay's single-quad geometry. */
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
    private final RandomSource random = RandomSource.create();
    private final PoseStack lightingPose = new PoseStack();

    public void render(Minecraft mc, BlockPos pos, WallOverlayData data, PoseStack pose, VertexConsumer consumer) {
        if (mc.level == null) return;
        if (cachedLevel.get() != mc.level) {
            blocks.clear();
            cachedLevel = new WeakReference<>(mc.level);
        }
        CachedBlock cached = blocks.computeIfAbsent(pos.asLong(), key -> new CachedBlock());
        boolean changed = cached.updateModel(mc, data);
        BlockState state = mc.level.getBlockState(pos);
        long tick = mc.level.getGameTime();
        boolean smooth = Minecraft.useAmbientOcclusion();
        // Reuse lighting across frames, but refresh each game tick so neighboring blocks and
        // propagated light changes cannot leave permanent stale shadows. No per-frame AO work.
        if (changed || cached.tick != tick || cached.state != state || cached.smooth != smooth) {
            lightingPose.pushPose();
            try {
                mc.getBlockRenderer().getModelRenderer().tesselateBlock(
                        mc.level, cached.parts, state, pos, lightingPose, layer -> cached, false,
                        OverlayTexture.NO_OVERLAY);
            } finally {
                lightingPose.popPose();
            }
            cached.tick = tick;
            cached.state = state;
            cached.smooth = smooth;
        }
        cached.draw(pose.last().pose(), consumer);
    }

    private static BakedQuad bakeFace(Direction direction, TextureAtlasSprite sprite) {
        // FaceBakery supplies the canonical winding required by vanilla's per-corner AO mapping.
        BakedQuad quad = FaceBakery.bakeQuad(new Vector3f(), new Vector3f(16, 16, 16),
                new BlockElementFace(direction, -1, "", new BlockElementFace.UVs(0, 0, 16, 16), Quadrant.R0),
                sprite, direction, BlockModelRotation.X0_Y0, null, true, 0);
        int[] vertices = quad.vertices();
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 8;
            float x = Float.intBitsToFloat(vertices[offset]);
            float y = Float.intBitsToFloat(vertices[offset + 1]);
            float z = Float.intBitsToFloat(vertices[offset + 2]);
            // Preserve existing wallpaper orientation and full-face UVs on every direction.
            float u = switch (direction) {
                case NORTH, UP, DOWN -> x;
                case SOUTH -> 1 - x;
                case EAST -> z;
                case WEST -> 1 - z;
            };
            float v = direction.getAxis().isVertical() ? z : 1 - y;
            vertices[offset + 4] = Float.floatToRawIntBits(sprite.getU0() + u * (sprite.getU1() - sprite.getU0()));
            vertices[offset + 5] = Float.floatToRawIntBits(sprite.getV0() + v * (sprite.getV1() - sprite.getV0()));
        }
        return quad;
    }

    private static final class LitFace {
        final BakedQuad quad;
        final float[] brightness = new float[4];
        final int[] lightmap = new int[4];

        LitFace(BakedQuad quad) {
            this.quad = quad;
        }
    }

    /** Captures the vanilla renderer's final vertex lighting without uploading extra geometry. */
    private static final class CachedBlock implements VertexConsumer {
        final LitFace[] faces = new LitFace[6];
        List<BlockModelPart> parts;
        BlockState state;
        long tick = Long.MIN_VALUE;
        boolean smooth;

        boolean updateModel(Minecraft mc, WallOverlayData data) {
            boolean changed = parts == null;
            for (Direction direction : DIRECTIONS) {
                int index = direction.ordinal();
                var texture = data.getFace(direction);
                TextureAtlasSprite sprite = texture == null ? null
                        : mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(texture);
                LitFace old = faces[index];
                // Sprite identity also changes on resource reload: never retain stale atlas UVs.
                if ((old == null ? null : old.quad.sprite()) != sprite) {
                    faces[index] = sprite == null ? null : new LitFace(bakeFace(direction, sprite));
                    changed = true;
                }
            }
            if (changed) {
                QuadCollection.Builder builder = new QuadCollection.Builder();
                TextureAtlasSprite particle = null;
                for (Direction direction : DIRECTIONS) {
                    LitFace face = faces[direction.ordinal()];
                    if (face != null) {
                        particle = face.quad.sprite();
                        builder.addCulledFace(direction, face.quad);
                    }
                }
                parts = particle == null ? List.of() : List.of(
                        new SimpleModelWrapper(builder.build(), true, particle,
                                ChunkSectionLayer.CUTOUT_MIPPED));
            }
            return changed;
        }

        @Override
        public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness,
                                float red, float green, float blue, float alpha, int[] lightmap,
                                int overlay, boolean readExistingColor) {
            LitFace face = faces[quad.direction().ordinal()];
            System.arraycopy(brightness, 0, face.brightness, 0, 4);
            System.arraycopy(lightmap, 0, face.lightmap, 0, 4);
        }

        void draw(Matrix4f matrix, VertexConsumer consumer) {
            for (Direction direction : DIRECTIONS) {
                LitFace face = faces[direction.ordinal()];
                if (face == null) continue;
                int[] vertices = face.quad.vertices();
                for (int vertex = 0; vertex < 4; vertex++) {
                    int offset = vertex * 8;
                    // Offset only the drawn surface, not the geometry used for light sampling.
                    float x = Float.intBitsToFloat(vertices[offset]) + direction.getStepX() * SURFACE_OFFSET;
                    float y = Float.intBitsToFloat(vertices[offset + 1]) + direction.getStepY() * SURFACE_OFFSET;
                    float z = Float.intBitsToFloat(vertices[offset + 2]) + direction.getStepZ() * SURFACE_OFFSET;
                    float shade = face.brightness[vertex];
                    consumer.addVertex(matrix, x, y, z)
                            .setColor(shade, shade, shade, 1.0F)
                            .setUv(Float.intBitsToFloat(vertices[offset + 4]), Float.intBitsToFloat(vertices[offset + 5]))
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(face.lightmap[vertex])
                            .setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
                }
            }
        }

        // The block renderer delivers complete baked quads through putBulkData above.
        @Override public VertexConsumer addVertex(float x, float y, float z) { throw new UnsupportedOperationException("Expected baked quad"); }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { throw new UnsupportedOperationException("Expected baked quad"); }
        @Override public VertexConsumer setUv(float u, float v) { throw new UnsupportedOperationException("Expected baked quad"); }
        @Override public VertexConsumer setUv1(int u, int v) { throw new UnsupportedOperationException("Expected baked quad"); }
        @Override public VertexConsumer setUv2(int u, int v) { throw new UnsupportedOperationException("Expected baked quad"); }
        @Override public VertexConsumer setNormal(float x, float y, float z) { throw new UnsupportedOperationException("Expected baked quad"); }
    }
}
