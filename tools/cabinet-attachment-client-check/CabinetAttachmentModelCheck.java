package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.CabinetAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/** Opt-in client-baked model validation, excluded from the normal mod build. */
@EventBusSubscriber(modid=ATouchofClass.MODID, value=Dist.CLIENT)
public final class CabinetAttachmentModelCheck {
    @SubscribeEvent
    public static void baked(ModelEvent.BakingCompleted event) {
        String result;
        try {
            int count = 0;
            int shadedModels = 0;
            for (var block : BuiltInRegistries.BLOCK) {
                if (!CabinetAttachments.isAttachment(block.defaultBlockState())) continue;
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    var model = event.getBakingResult().getBlockStateModel(state);
                    if (model == event.getBakingResult().missingModels().block()) throw new AssertionError("Missing model " + state);
                    var parts = new ArrayList<BlockStateModelPart>();
                    model.collectParts(RandomSource.create(42), parts);
                    var quads = new ArrayList<BakedQuad>();
                    for (var part : parts) {
                        quads.addAll(part.getQuads(null));
                        for (var face : Direction.values()) quads.addAll(part.getQuads(face));
                    }
                    if (quads.isEmpty()) throw new AssertionError("Empty attachment " + state);
                    for (var quad : quads) {
                        if (quad.materialInfo().sprite().contents().name().toString().contains("missing"))
                            throw new AssertionError("Missing texture " + state);
                        for (int i=0; i<4; i++) {
                            float y = quad.position(i).y() + 1;
                            if (y < 0 || y > 2) throw new AssertionError("Invalid cabinet-relative height " + state);
                        }
                    }
                    if (CabinetAttachmentLightingCheck.verify(state, model, parts)) shadedModels++;
                    count++;
                }
            }
            if (shadedModels == 0) throw new AssertionError("Lighting fixture must distinguish ambient shading from flat lighting");
            result = "PASS: " + count + " attachment models validated; placed/attached vertex colors and light match with smooth lighting on and off; " + shadedModels + " models exercise ambient shading.\n";
        } catch (Throwable error) { result = "FAIL: " + error + "\n"; error.printStackTrace(); }
        try {
            var path = Path.of(System.getProperty("cabinetAttachment.checkReport"));
            Files.createDirectories(path.getParent());
            Files.writeString(path, result);
        } catch (Exception error) { throw new RuntimeException(error); }
        System.out.println(result);
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().stop());
    }
}
