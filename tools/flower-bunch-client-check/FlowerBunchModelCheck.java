package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.FlowerBunchVariants;
import com.digitaldownpour.atouchofclass.ModBlocks;
import com.digitaldownpour.atouchofclass.blocks.FlowerBunchBlock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Opt-in validation of actual client-baked cutout geometry; excluded from release builds. */
@EventBusSubscriber(modid=ATouchofClass.MODID,value=Dist.CLIENT)
public final class FlowerBunchModelCheck {
    @SubscribeEvent
    public static void baked(ModelEvent.BakingCompleted event) {
        String result;
        try {
            int count=0;
            for (var flower : FlowerBunchVariants.VALUES) {
                for (var state : ModBlocks.FLOWER_BUNCHES.get(flower.id()).get().getStateDefinition().getPossibleStates()) {
                    var model=event.getBakingResult().getBlockStateModel(state);
                    if (model==event.getBakingResult().missingModels().block()) throw new AssertionError("Missing model "+state);
                    var parts=new ArrayList<BlockStateModelPart>();
                    model.collectParts(RandomSource.create(0),parts);
                    var quads=new ArrayList<BakedQuad>();
                    for (var part : parts) {
                        quads.addAll(part.getQuads(null));
                        for (var d : Direction.values()) quads.addAll(part.getQuads(d));
                    }
                    if (quads.size()<40) throw new AssertionError("Bunch must be dense and double-sided: "+state);
                    float minY=Float.POSITIVE_INFINITY,maxY=Float.NEGATIVE_INFINITY;
                    int frontHeads=0,backHeads=0;
                    for (var quad : quads) {
                        String texture=quad.materialInfo().sprite().contents().name().toString();
                        if (!texture.startsWith("minecraft:block/") || texture.contains("missing")) throw new AssertionError("Missing vanilla texture "+state+" "+texture);
                        if (texture.equals("minecraft:block/sunflower_front")) {
                            frontHeads++;
                            if (quad.direction()!=state.getValue(FlowerBunchBlock.FACING)) throw new AssertionError("Sunflower heads must face outward: "+state);
                        }
                        if (texture.equals("minecraft:block/sunflower_back")) backHeads++;
                        for (int i=0;i<4;i++) {
                            float y=quad.position(i).y();minY=Math.min(minY,y);maxY=Math.max(maxY,y);
                            if (quad.position(i).x()<-.25 || quad.position(i).x()>1.25 || quad.position(i).z()<-.25 || quad.position(i).z()>1.25)
                                throw new AssertionError("Excessive foliage overhang "+state);
                        }
                    }
                    if (Math.abs(minY+.125)>.00001 || Math.abs(maxY-(flower.height()-2)/16.0)>.00001)
                        throw new AssertionError("Plant must meet soil and keep its compact height: "+state+" "+minY+" "+maxY);
                    if (flower.flower().equals("sunflower") && (frontHeads!=5 || backHeads!=5)) throw new AssertionError("Sunflower needs five front/back disks");
                    count++;
                }
                var item=event.getBakingResult().itemStackModels().get(Identifier.fromNamespaceAndPath(ATouchofClass.MODID,flower.id()));
                if (item==null || item==event.getBakingResult().missingModels().item()) throw new AssertionError("Missing inventory model "+flower.id());
            }
            result="PASS: "+count+" planted models and 25 inventory models, correct soil height, dense geometry, vanilla textures and outward sunflower heads.\n";
        } catch (Throwable error) { result="FAIL: "+error+"\n";error.printStackTrace(); }
        try {
            Path path=Path.of(System.getProperty("flowerBunch.checkReport"));
            Files.createDirectories(path.getParent());Files.writeString(path,result);
        } catch (Exception error) { throw new RuntimeException(error); }
        System.out.println(result);
        Minecraft.getInstance().execute(()->Minecraft.getInstance().stop());
    }
}
