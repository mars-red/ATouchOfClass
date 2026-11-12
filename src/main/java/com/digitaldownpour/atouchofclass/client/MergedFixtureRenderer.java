package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import com.digitaldownpour.atouchofclass.entities.MergedFixtureBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class MergedFixtureRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, MergedFixtureRenderer.State> {
    private static final RenderType FIXTURE_RENDER_TYPE = RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS);

    private final boolean cabinetAttachments;

    public MergedFixtureRenderer(BlockEntityRendererProvider.Context context, boolean cabinetAttachments) {
        this.cabinetAttachments = cabinetAttachments;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        // An attachment can be in a different visible section than its cabinet or its partner.
        return cabinetAttachments;
    }

    @Override
    public boolean shouldRender(T entity, Vec3 cameraPos) {
        return MergedFixtureBlockEntity.part(fixtureState(entity)) != ChestType.SINGLE
                && BlockEntityRenderer.super.shouldRender(entity, cameraPos);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public AABB getRenderBoundingBox(T entity) {
        BlockPos pos = fixturePos(entity.getBlockPos());
        AABB bounds = new AABB(pos);
        BlockState state = fixtureState(entity);
        if (MergedFixtureBlockEntity.part(state) == ChestType.LEFT) {
            return bounds.minmax(new AABB(pos.relative(rightOf(state))));
        }
        return bounds;
    }

    @Override
    public void extractRenderState(T entity, State renderState, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(entity, renderState, breakProgress);
        BlockState state = fixtureState(entity);
        ChestType part = MergedFixtureBlockEntity.part(state);
        if (part == ChestType.SINGLE || !(entity.getLevel() instanceof ClientLevel level)) {
            return;
        }

        BlockPos ownerPos = entity.getBlockPos();
        BlockPos pos = fixturePos(ownerPos);
        Direction right = rightOf(state);
        BlockPos otherOwnerPos = ownerPos.relative(part == ChestType.LEFT ? right : right.getOpposite());
        BlockPos otherPos = fixturePos(otherOwnerPos);
        BlockState other = cabinetAttachments
                ? level.getBlockEntity(otherOwnerPos) instanceof CabinetBlockEntity cabinet
                        ? cabinet.getAttachment() : Blocks.AIR.defaultBlockState()
                : level.getBlockState(otherPos);
        boolean paired = other.is(state.getBlock()) && MergedFixtureBlockEntity.part(other) == part.getOpposite()
                && rightOf(other) == right;

        renderState.right = right;
        renderState.lightCoords = LightCoordsUtil.getLightCoords(level, pos);
        if (part == ChestType.RIGHT && paired) {
            if (breakProgress != null) {
                renderState.breakingParts = collectParts(level, pos, state);
            }
            return;
        }

        renderState.first = collectParts(level, pos, state);
        if (breakProgress != null) {
            renderState.breakingParts = renderState.first;
        }
        if (paired) {
            renderState.second = collectParts(level, otherPos, other);
            int otherLight = LightCoordsUtil.getLightCoords(level, otherPos);
            renderState.lightCoords = averageLight(renderState.lightCoords, otherLight);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        if (cabinetAttachments) {
            poseStack.translate(0, 1, 0);
        }
        submitHalf(collector, poseStack, state.first, state.lightCoords);
        if (!state.second.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(state.right.getStepX(), 0, state.right.getStepZ());
            submitHalf(collector, poseStack, state.second, state.lightCoords);
            poseStack.popPose();
        }
        poseStack.popPose();
        if (state.breakProgress != null && !state.breakingParts.isEmpty()) {
            collector.submitBreakingBlockModel(poseStack, state.breakingParts, state.breakProgress.progress(), false);
        }
    }

    private static BlockState fixtureState(BlockEntity entity) {
        return entity instanceof CabinetBlockEntity cabinet ? cabinet.getAttachment() : entity.getBlockState();
    }

    private BlockPos fixturePos(BlockPos owner) {
        return cabinetAttachments ? owner.above() : owner;
    }

    private static Direction rightOf(BlockState state) {
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
    }

    private static List<BlockStateModelPart> collectParts(ClientLevel level, BlockPos pos, BlockState state) {
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(level, pos, state, RandomSource.create(state.getSeed(pos)), parts);
        return List.copyOf(parts);
    }

    private static int averageLight(int left, int right) {
        int block = (LightCoordsUtil.smoothBlock(left) + LightCoordsUtil.smoothBlock(right)) / 2;
        int sky = (LightCoordsUtil.smoothSky(left) + LightCoordsUtil.smoothSky(right)) / 2;
        return LightCoordsUtil.smoothPack(block, sky);
    }

    private static void submitHalf(SubmitNodeCollector collector, PoseStack poseStack,
                                   List<BlockStateModelPart> parts, int light) {
        if (parts.isEmpty()) {
            return;
        }
        collector.submitCustomGeometry(poseStack, FIXTURE_RENDER_TYPE, (pose, consumer) -> {
            QuadInstance lighting = new QuadInstance();
            lighting.setLightCoords(light);
            for (BlockStateModelPart part : parts) {
                for (BakedQuad quad : part.getQuads(null)) {
                    consumer.putBakedQuad(pose, quad, lighting);
                }
                for (Direction direction : Direction.values()) {
                    for (BakedQuad quad : part.getQuads(direction)) {
                        consumer.putBakedQuad(pose, quad, lighting);
                    }
                }
            }
        });
    }

    public static final class State extends BlockEntityRenderState {
        private Direction right = Direction.EAST;
        private List<BlockStateModelPart> first = List.of();
        private List<BlockStateModelPart> second = List.of();
        private List<BlockStateModelPart> breakingParts = List.of();
    }
}
