package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.client.OverlayLightingRenderer;
import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.model.ao.EnhancedBlockModelLighter;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class ModRenderers {
    private record OverlayBlock(BlockPos pos, List<BakedQuad> faces) {}

    @SubscribeEvent
    public static void addSectionGeometry(AddSectionGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getLevel() != mc.level) return;
        BlockPos origin = event.getSectionOrigin();
        var overlays = ClientWallOverlayData.getChunks().get(ChunkPos.containing(origin));
        if (overlays == null || overlays.isEmpty()) return;

        // Capture all mutable client data before handing the work to the meshing thread.
        List<OverlayBlock> blocks = new ArrayList<>();
        overlays.forEach((pos, data) -> {
            if ((pos.getY() >> 4) == (origin.getY() >> 4) && data.hasAny()) {
                blocks.add(new OverlayBlock(pos.immutable(), OverlayLightingRenderer.snapshot(mc, data)));
            }
        });
        if (blocks.isEmpty()) return;
        var snapshot = List.copyOf(blocks);
        boolean smooth = mc.options.ambientOcclusion().get();
        event.addRenderer(context -> {
            // Lighters own thread-local caches: construct one on the executing worker.
            var lighter = EnhancedBlockModelLighter.newInstance();
            var consumer = context.getOrCreateChunkBuffer(ChunkSectionLayer.CUTOUT);
            for (OverlayBlock block : snapshot) {
                OverlayLightingRenderer.draw(context.getRegion(), block.pos(), block.faces(), smooth, lighter, consumer);
            }
        });
    }

    @SubscribeEvent
    public static void flushOverlayUpdates(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        var sections = ClientWallOverlayData.drainDirtySections();
        if (mc.level == null) return;
        for (var section : sections) {
            mc.level.setSectionRangeDirty(section.x(), section.y(), section.z(),
                    section.x(), section.y(), section.z());
        }
    }

    @SubscribeEvent
    public static void unloadChunk(ChunkEvent.Unload event) {
        if (event.getLevel() == Minecraft.getInstance().level) {
            ClientWallOverlayData.forgetChunk(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void unloadLevel(LevelEvent.Unload event) {
        if (event.getLevel() == Minecraft.getInstance().level) {
            ClientWallOverlayData.clear();
        }
    }
}
