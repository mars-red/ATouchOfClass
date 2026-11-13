package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import com.digitaldownpour.atouchofclass.entities.MergedFixtureBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

/** Snapshot attachments on the main thread and mesh them with their owning cabinet section. */
@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class CabinetAttachmentRenderer {
    private record Surface(BlockPos owner, BlockState state, BlockStateModel model) {}

    /** Preserve model-part AO settings and culled-face groups across the meshing thread boundary. */
    static BlockStateModel snapshot(List<BlockStateModelPart> parts) {
        var captured = List.copyOf(parts);
        return new BlockStateModel() {
            @Override
            public void collectParts(RandomSource random, List<BlockStateModelPart> output) { output.addAll(captured); }
            @Override
            public Material.Baked particleMaterial() { return captured.getFirst().particleMaterial(); }
            @Override
            public int materialFlags() { return captured.getFirst().materialFlags(); }
        };
    }

    static void tesselate(ModelBlockRenderer renderer, BlockQuadOutput output, BlockAndTintGetter region,
                          BlockPos owner, BlockState state, BlockStateModel model) {
        // The model belongs above the cabinet, but its vertices are relative to the owner's section.
        renderer.tesselateBlock(output, owner.getX() & 15, (owner.getY() & 15) + 1, owner.getZ() & 15,
                region, owner.above(), state, model, state.getSeed(owner.above()));
    }

    @SubscribeEvent
    public static void geometry(AddSectionGeometryEvent event) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || event.getLevel() != mc.level) return;
        var origin = event.getSectionOrigin();
        var surfaces = new ArrayList<Surface>();
        for (var be : mc.level.getChunkAt(origin).getBlockEntities().values()) {
            if (!(be instanceof CabinetBlockEntity cabinet) || (be.getBlockPos().getY() >> 4) != (origin.getY() >> 4)) continue;
            var state = cabinet.getAttachment();
            if (state.isAir() || MergedFixtureBlockEntity.part(state) != ChestType.SINGLE) continue;
            var model = mc.getModelManager().getBlockStateModelSet().get(state);
            var parts = new ArrayList<BlockStateModelPart>();
            model.collectParts(mc.level, be.getBlockPos().above(), state, RandomSource.create(42), parts);
            if (!parts.isEmpty()) surfaces.add(new Surface(be.getBlockPos().immutable(), state, snapshot(parts)));
        }
        if (surfaces.isEmpty()) return;
        var snapshot = List.copyOf(surfaces);
        boolean smooth = mc.options.ambientOcclusion().get();
        var blockColors = mc.getBlockColors();
        event.addRenderer(context -> {
            // Use the same AO selection, directional light sampling, and tinting as placed blocks.
            // Keep face culling disabled so ordinary blocks can still overlap decorative attachments.
            var renderer = new ModelBlockRenderer(smooth, false, blockColors);
            var consumer = context.getOrCreateChunkBuffer(ChunkSectionLayer.CUTOUT);
            for (var surface : snapshot) {
                tesselate(renderer, (x, y, z, quad, lighting) -> {
                    var transform = new Matrix4f().translation(x, y, z);
                    var direction = quad.direction();
                    for (int i = 0; i < 4; i++) {
                        var vertex = quad.position(i);
                        long uv = quad.packedUV(i);
                        consumer.addVertex(transform, vertex.x(), vertex.y(), vertex.z())
                                .setColor(lighting.getColor(i))
                                .setUv(UVPair.unpackU(uv), UVPair.unpackV(uv))
                                .setLight(lighting.getLightCoords(i))
                                .setNormal(direction.getStepX(), direction.getStepY(), direction.getStepZ());
                    }
                }, context.getRegion(), surface.owner(), surface.state(), surface.model());
            }
        });
    }
}
