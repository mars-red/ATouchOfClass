package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.model.ao.EnhancedBlockModelLighter;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

/** Snapshot attachments on the main thread and mesh them with their owning cabinet section. */
@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class CabinetAttachmentRenderer {
    private record Surface(BlockPos owner, BlockState state, List<BakedQuad> quads) {}

    @SubscribeEvent
    public static void geometry(AddSectionGeometryEvent event) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || event.getLevel() != mc.level) return;
        var origin = event.getSectionOrigin();
        var surfaces = new ArrayList<Surface>();
        for (var be : mc.level.getChunkAt(origin).getBlockEntities().values()) {
            if (!(be instanceof CabinetBlockEntity cabinet) || (be.getBlockPos().getY() >> 4) != (origin.getY() >> 4)) continue;
            var state = cabinet.getAttachment();
            if (state.isAir()) continue;
            var model = mc.getModelManager().getBlockStateModelSet().get(state);
            var parts = new ArrayList<BlockStateModelPart>();
            model.collectParts(mc.level, be.getBlockPos().above(), state, RandomSource.create(42), parts);
            var quads = new ArrayList<BakedQuad>();
            for (var part : parts) {
                quads.addAll(part.getQuads(null));
                for (Direction face : Direction.values()) quads.addAll(part.getQuads(face));
            }
            surfaces.add(new Surface(be.getBlockPos().immutable(), state, List.copyOf(quads)));
        }
        if (surfaces.isEmpty()) return;
        var snapshot = List.copyOf(surfaces);
        event.addRenderer(context -> {
            var lighter = EnhancedBlockModelLighter.newInstance();
            var lighting = new QuadInstance();
            var consumer = context.getOrCreateChunkBuffer(ChunkSectionLayer.CUTOUT);
            for (var surface : snapshot) {
                var pos = surface.owner().above();
                // Translate from the owner's section, including a cabinet at the very top of a section.
                var transform = new Matrix4f().translation(surface.owner().getX() & 15,
                        (surface.owner().getY() & 15) + 1, surface.owner().getZ() & 15);
                lighter.reset();
                for (var quad : surface.quads()) {
                    int light = lighter.getLightCoords(surface.state(), context.getRegion(), pos);
                    lighter.prepareQuadFlat(context.getRegion(), surface.state(), pos, light, quad, lighting);
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
                }
            }
        });
    }
}
