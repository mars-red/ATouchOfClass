package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.blocks.RefrigeratorBlock;
import com.digitaldownpour.atouchofclass.blocks.RefrigeratorBlock.Layer;
import com.digitaldownpour.atouchofclass.blocks.RefrigeratorBlock.Side;
import com.digitaldownpour.atouchofclass.entities.RefrigeratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RefrigeratorRenderer implements BlockEntityRenderer<RefrigeratorBlockEntity, RefrigeratorRenderer.State> {
    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public AABB getRenderBoundingBox(RefrigeratorBlockEntity entity) {
        BlockPos pos = entity.getBlockPos();
        Direction facing = entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return new AABB(pos).minmax(new AABB(RefrigeratorBlock.partPos(pos, facing, Layer.UPPER, Side.RIGHT)));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        // The lower-left anchor can be outside the visible chunk section.
        return true;
    }

    @Override
    public void extractRenderState(RefrigeratorBlockEntity entity, State state, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(entity, state, breakProgress);
        state.sections.clear();
        if (!(entity.getLevel() instanceof ClientLevel level)) {
            return;
        }

        BlockPos anchor = entity.getBlockPos();
        BlockState refrigerator = entity.getBlockState();
        Direction facing = refrigerator.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int blockLight = 0;
        int skyLight = 0;
        for (Layer layer : Layer.values()) {
            for (Side side : Side.values()) {
                BlockPos pos = RefrigeratorBlock.partPos(anchor, facing, layer, side);
                BlockState part = level.getBlockState(pos);
                if (part != refrigerator.setValue(RefrigeratorBlock.LAYER, layer).setValue(RefrigeratorBlock.SIDE, side)) {
                    continue;
                }

                state.sections.add(new Section(pos.subtract(anchor), MergedFixtureRenderer.collectParts(level, pos, part)));
                int light = LightCoordsUtil.getLightCoords(level, pos);
                blockLight += LightCoordsUtil.smoothBlock(light);
                skyLight += LightCoordsUtil.smoothSky(light);
            }
        }
        int count = state.sections.size();
        if (count > 0) {
            state.lightCoords = LightCoordsUtil.smoothPack(blockLight / count, skyLight / count);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Section section : state.sections) {
            poseStack.pushPose();
            BlockPos offset = section.offset();
            poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
            MergedFixtureRenderer.submitParts(collector, poseStack, section.parts(), state.lightCoords);
            poseStack.popPose();

            if (state.breakProgress != null && offset.equals(BlockPos.ZERO)) {
                collector.submitBreakingBlockModel(poseStack, section.parts(), state.breakProgress.progress(), false);
            }
        }
    }

    private record Section(BlockPos offset, List<BlockStateModelPart> parts) {}

    public static final class State extends BlockEntityRenderState {
        private final List<Section> sections = new ArrayList<>();
    }
}
