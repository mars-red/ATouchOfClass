package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.blocks.OutdoorUplightBlock;
import com.digitaldownpour.atouchofclass.entities.OutdoorUplightBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public final class OutdoorUplightRenderer implements BlockEntityRenderer<OutdoorUplightBlockEntity, OutdoorUplightRenderer.State> {
    private static final int HEIGHT = 4;
    private static final int HALF_WIDTH = 1;
    private static final int WIDTH = 2 * HALF_WIDTH + 1;
    private static final double SURFACE_OFFSET = 0.003;
    private static final RenderType WASH = RenderTypes.eyes(Identifier.fromNamespaceAndPath(
            ATouchofClass.MODID, "textures/misc/outdoor_uplight_wash.png"));

    private static float widthScale(BlockState state) {
        return 1.0F + (state.getValue(OutdoorUplightBlock.BEAM_WIDTH) - 1) / 3.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public AABB getRenderBoundingBox(OutdoorUplightBlockEntity entity) {
        AABB fixture = new AABB(entity.getBlockPos());
        if (OutdoorUplightSpotlight.isRequested()) {
            int radius = OutdoorUplightSpotlight.horizontalRadius(widthScale(entity.getBlockState()));
            return fixture.inflate(radius, 0, radius).expandTowards(0, 9, 0);
        }
        return fixture.inflate(1, 0, 1).expandTowards(0, HEIGHT - 1, 0);
    }

    @Override
    public void extractRenderState(OutdoorUplightBlockEntity entity, State state, float partialTicks, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(entity, state, breakProgress);
        state.visibleFaces = 0;
        state.aim = null;
        Level level = entity.getLevel();
        if (level == null) return;

        BlockPos fixture = entity.getBlockPos();
        Direction aim = entity.getBlockState().getValue(OutdoorUplightBlock.FACING);
        state.aim = aim;
        state.widthScale = widthScale(entity.getBlockState());
        state.normal = aim.getOpposite();
        state.right = aim.getClockWise();
        Vec3 forward = Vec3.atLowerCornerOf(aim.getUnitVec3i());
        Vec3 lens = Vec3.atBottomCenterOf(fixture).add(0, 0.65, 0).add(forward.scale(0.14));
        // Check each column's chunks once. Never load a chunk during render extraction.
        for (int column = 0; column < WIDTH; column++) {
            BlockPos wallBase = fixture.relative(aim).relative(state.right, column - HALF_WIDTH);
            BlockPos frontBase = wallBase.relative(state.normal);
            if (!hasChunk(level, wallBase) || !hasChunk(level, frontBase)) continue;
            for (int row = 0; row < HEIGHT; row++) {
                BlockPos wall = wallBase.above(row);
                BlockPos front = frontBase.above(row);
                if (!level.getBlockState(wall).isSolidRender()) continue;
                if (!front.equals(fixture) && !level.getBlockState(front).getCollisionShape(level, front).isEmpty()) continue;

                Vec3 target = Vec3.atCenterOf(wall).subtract(forward.scale(0.5 + SURFACE_OFFSET));
                if (row == 0) target = target.add(0, 0.25, 0);
                var ray = new ClipContext(lens, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                        CollisionContext.empty());
                if (level.clip(ray).getType() == HitResult.Type.MISS) {
                    state.visibleFaces |= 1 << (row * WIDTH + column);
                }
            }
        }
        // Give the wash more contrast in darkness and indoors.
        int sky = level.getBrightness(LightLayer.SKY, fixture) - level.getSkyDarken();
        state.opacity = 255 - Math.clamp(sky, 0, 15) * 12;
    }

    private static boolean hasChunk(Level level, BlockPos pos) {
        return level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int faces = state.visibleFaces;
        if (faces == 0 || OutdoorUplightSpotlight.isActive()) return;
        Direction normal = state.normal;
        Direction right = state.right;
        int color = (state.opacity << 24) | 0xFFFFFF;
        collector.submitCustomGeometry(poseStack, WASH, (pose, consumer) -> {
            for (int row = 0; row < HEIGHT; row++) {
                for (int column = 0; column < WIDTH; column++) {
                    if ((faces & (1 << (row * WIDTH + column))) == 0) continue;
                    vertex(consumer, pose, normal, right, column, row, color);
                    vertex(consumer, pose, normal, right, column + 1, row, color);
                    vertex(consumer, pose, normal, right, column + 1, row + 1, color);
                    vertex(consumer, pose, normal, right, column, row + 1, color);
                }
            }
        });
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Direction normal, Direction right,
                               int column, int row, int color) {
        float lateral = column - WIDTH * 0.5F;
        float depth = (float) (0.5 - SURFACE_OFFSET);
        float x = 0.5F - normal.getStepX() * depth + right.getStepX() * lateral;
        float z = 0.5F - normal.getStepZ() * depth + right.getStepZ() * lateral;
        consumer.addVertex(pose, x, row, z)
                .setColor(color)
                .setUv((float) column / WIDTH, 1.0F - (float) row / HEIGHT)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, normal.getStepX(), 0, normal.getStepZ());
    }

    public static final class State extends BlockEntityRenderState {
        private Direction normal = Direction.SOUTH;
        private Direction right = Direction.EAST;
        private int visibleFaces;
        private int opacity;
        float widthScale;
        private @Nullable Direction aim;

        @Nullable Direction spotlightAim() {
            return aim;
        }

        float spotlightStrength() {
            return opacity / 255.0F;
        }
    }
}
