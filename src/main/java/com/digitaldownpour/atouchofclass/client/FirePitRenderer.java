package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.blocks.FirePitBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws two axis-aligned, full-bright flame planes through the pit centre. A plane runs out to the
 * block edge wherever a neighbouring pit joins, and the flame texture tiles horizontally, so joined
 * pits show one continuous flame. All pits share the same frame so neighbouring planes line up.
 */
public final class FirePitRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, FirePitRenderer.State> {
    private static final float BASE_Y = 9.0F / 16.0F;
    private static final float HEIGHT = FirePitFlames.FRAME_HEIGHT / 16.0F;
    private static final float INSET = 4.0F / 16.0F;
    private static final float DEPTH = 2.0F / 16.0F;
    private static final int TICKS_PER_FRAME = 2;

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public boolean shouldRender(T entity, Vec3 cameraPos) {
        return entity.getBlockState().getValue(FirePitBlock.LIT) && BlockEntityRenderer.super.shouldRender(entity, cameraPos);
    }

    @Override
    public AABB getRenderBoundingBox(T entity) {
        return new AABB(entity.getBlockPos()).expandTowards(0, 0.75, 0);
    }

    @Override
    public void extractRenderState(T entity, State renderState, float partialTicks, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(entity, renderState, breakProgress);
        var block = entity.getBlockState();
        renderState.northSouthMin = block.getValue(FirePitBlock.EDGES[0]) ? 0.0F : INSET;
        renderState.eastWestMax = block.getValue(FirePitBlock.EDGES[1]) ? 1.0F : 1.0F - INSET;
        renderState.northSouthMax = block.getValue(FirePitBlock.EDGES[2]) ? 1.0F : 1.0F - INSET;
        renderState.eastWestMin = block.getValue(FirePitBlock.EDGES[3]) ? 0.0F : INSET;
        long time = entity.getLevel() == null ? 0 : entity.getLevel().getGameTime();
        int tick = (int) ((time + partialTicks) / TICKS_PER_FRAME);
        for (int i = 0; i < 4; i++) {
            renderState.frames[i] = Math.floorMod(tick + i * FirePitFlames.FRAME_COUNT / 4, FirePitFlames.FRAME_COUNT);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitCustomGeometry(poseStack, FirePitFlames.renderType(), (pose, consumer) -> {
            // Two planes along X and two along Z, DEPTH apart in total; u follows the block-local coordinate.
            float near = 0.5F - DEPTH / 2.0F;
            float far = 0.5F + DEPTH / 2.0F;
            quad(consumer, pose, state.frames[0], state.eastWestMin, near, state.eastWestMax, near);
            quad(consumer, pose, state.frames[1], state.eastWestMin, far, state.eastWestMax, far);
            quad(consumer, pose, state.frames[2], near, state.northSouthMin, near, state.northSouthMax);
            quad(consumer, pose, state.frames[3], far, state.northSouthMin, far, state.northSouthMax);
        });
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int frame,
                             float x0, float z0, float x1, float z1) {
        boolean alongX = z0 == z1;
        float u0 = alongX ? x0 : z0;
        float u1 = alongX ? x1 : z1;
        float v0 = (float) frame / FirePitFlames.FRAME_COUNT;
        float v1 = (float) (frame + 1) / FirePitFlames.FRAME_COUNT;
        float top = BASE_Y + HEIGHT;
        // Both windings: the eyes pipeline culls back faces.
        vertex(consumer, pose, x0, BASE_Y, z0, u0, v1);
        vertex(consumer, pose, x1, BASE_Y, z1, u1, v1);
        vertex(consumer, pose, x1, top, z1, u1, v0);
        vertex(consumer, pose, x0, top, z0, u0, v0);
        vertex(consumer, pose, x0, top, z0, u0, v0);
        vertex(consumer, pose, x1, top, z1, u1, v0);
        vertex(consumer, pose, x1, BASE_Y, z1, u1, v1);
        vertex(consumer, pose, x0, BASE_Y, z0, u0, v1);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    public static final class State extends BlockEntityRenderState {
        private final int[] frames = new int[4];
        private float eastWestMin;
        private float eastWestMax;
        private float northSouthMin;
        private float northSouthMax;
    }
}
