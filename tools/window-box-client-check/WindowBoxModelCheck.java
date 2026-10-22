package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.ModBlocks;
import com.digitaldownpour.atouchofclass.WindowBoxVariants;
import com.digitaldownpour.atouchofclass.blocks.WindowBoxBlock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Opt-in client bake validation, compiled only by window-box-client-check.init.gradle. */
@EventBusSubscriber(modid=ATouchofClass.MODID, value=Dist.CLIENT)
public final class WindowBoxModelCheck {
    @SubscribeEvent
    public static void baked(ModelEvent.BakingCompleted event) {
        String result;
        try {
            int count = 0;
            for (var definition : WindowBoxVariants.VALUES) {
                String wood = definition.wood();
                String texture = "minecraft:block/" + (wood.equals("bamboo") ? "stripped_bamboo_block"
                        : wood.equals("crimson") || wood.equals("warped") ? "stripped_"+wood+"_stem" : "stripped_"+wood+"_log");
                for (var state : ModBlocks.WINDOW_BOXES.get(definition.id()).get().getStateDefinition().getPossibleStates()) {
                    var model = event.getBakingResult().getBlockStateModel(state);
                    if (model == event.getBakingResult().missingModels().block()) throw new AssertionError("Missing model " + state);
                    var parts = new ArrayList<BlockStateModelPart>();
                    model.collectParts(RandomSource.create(0),parts);
                    var quads = new ArrayList<BakedQuad>();
                    for (var part : parts) {
                        quads.addAll(part.getQuads(null));
                        for (Direction direction : Direction.values()) quads.addAll(part.getQuads(direction));
                    }
                    int expected = 14 - (state.getValue(WindowBoxBlock.JOINED_LEFT) ? 3 : 0)
                            - (state.getValue(WindowBoxBlock.JOINED_RIGHT) ? 3 : 0);
                    if (quads.size() != expected) throw new AssertionError("Wrong cap geometry: " + state + ": " + quads.size());
                    int soil = 0;
                    for (var quad : quads) {
                        String sprite = quad.materialInfo().sprite().contents().name().toString();
                        if (sprite.equals("minecraft:block/dirt")) {
                            soil++;
                            for (int i=0;i<4;i++) if (Math.abs(quad.position(i).y()-.875f)>.00001) throw new AssertionError("Wrong soil height");
                            if (quad.direction()!=Direction.UP) throw new AssertionError("Soil winding must face up");
                        } else if (!sprite.equals(texture)) throw new AssertionError("Wrong wood texture: " + sprite + " for " + state);
                        if (sprite.equals(texture) && quad.direction()==state.getValue(WindowBoxBlock.FACING)) {
                            // Horizontal grain: source U depends only on height,
                            // while source V changes across the panel width.
                            for (int a=0;a<4;a++) for (int b=a+1;b<4;b++) {
                                var pa=quad.position(a); var pb=quad.position(b);
                                if (Math.abs(pa.y()-pb.y())<.00001 && pa.distance(pb)>.1) {
                                    if (Math.abs(UVPair.unpackU(quad.packedUV(a))-UVPair.unpackU(quad.packedUV(b)))>.00001
                                            || Math.abs(UVPair.unpackV(quad.packedUV(a))-UVPair.unpackV(quad.packedUV(b)))<.00001)
                                        throw new AssertionError("Grain is not horizontal: " + state);
                                }
                            }
                        }
                        for (int i=0;i<4;i++) {
                            var v = quad.position(i);
                            if (v.x()<-.00001 || v.x()>1.00001 || v.z()<-.00001 || v.z()>1.00001 || v.y()<.49999 || v.y()>1.00001)
                                throw new AssertionError("Vertex outside upper block half: " + state + " " + v);
                        }
                    }
                    if (soil!=1) throw new AssertionError("Missing dirt " + state);
                    count++;
                }
                var item = event.getBakingResult().itemStackModels().get(Identifier.fromNamespaceAndPath(ATouchofClass.MODID,definition.id()));
                if (item==null || item==event.getBakingResult().missingModels().item()) throw new AssertionError("Missing item " + definition.id());
            }
            result = "PASS: "+count+" block states and 13 inventory models baked with correct textures, caps, bounds, and soil height.\n";
        } catch (Throwable error) {
            result = "FAIL: "+error+"\n";
            error.printStackTrace();
        }
        try {
            Path path = Path.of(System.getProperty("windowBox.checkReport"));
            Files.createDirectories(path.getParent());
            Files.writeString(path,result);
        } catch (Exception error) { throw new RuntimeException(error); }
        System.out.println(result);
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().stop());
    }
}
