package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;

@EventBusSubscriber(modid=ATouchofClass.MODID, value=Dist.CLIENT)
public final class RailingModelLoader implements UnbakedModelLoader<BlockModel> {
    @SubscribeEvent
    public static void register(ModelEvent.RegisterLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID,"railing"), new RailingModelLoader());
    }

    @Override
    public BlockModel read(JsonObject json, JsonDeserializationContext context) {
        BlockModel base=context.deserialize(json,BlockModel.class);
        List<BlockElement> elements=new ArrayList<>();
        for(var e:json.getAsJsonArray("elements")) elements.add(context.deserialize(e,BlockElement.class));
        return new BlockModel(new RailingGeometry(elements, json.get("grain_slope").getAsInt(),
                json.get("grain_lower").getAsBoolean()), base.guiLight(), base.ambientOcclusion(),
                base.transforms(), base.textureSlots(), base.parent(), base.rootTransform(),
                base.renderTypeGroup(), base.partVisibility());
    }

    private record RailingGeometry(List<BlockElement> elements, int slope, boolean lower)
            implements ExtendedUnbakedGeometry {
        RailingGeometry {
            if (slope != 1 && slope != -1) throw new IllegalArgumentException("grain_slope must be 1 or -1");
        }

        @Override
        public QuadCollection bake(TextureSlots slots, ModelBaker baker, ModelState state,
                                   ModelDebugName debugName, ContextMap properties) {
            var builder = new QuadCollection.Builder();
            Matrix4f inverse=new Matrix4f(state.transformation().getMatrix()).invert();
            for(BlockElement element:elements) {
                for(var entry:element.faces().entrySet()) {
                    Direction face=entry.getKey();
                    BlockElementFace definition=entry.getValue();
                    TextureAtlasSprite sprite = baker.sprites().resolveSlot(slots, definition.texture(), debugName);
                    BakedQuad quad=SimpleUnbakedGeometry.bakeFace(element,definition,sprite,face,state);
                    // The generator reserves #oak for diagonal wood. Level wood,
                    // log end grain and copper retain their ordinary vanilla mapping.
                    if(!definition.texture().equals("#oak")) builder.addUnculledFace(quad);
                    else for(BakedQuad mapped:remap(quad,inverse,face,slope,lower)) builder.addUnculledFace(mapped);
                }
            }
            return builder.build();
        }
    }

    private static List<BakedQuad> remap(BakedQuad quad,Matrix4f inverse,Direction face,int slope,boolean lower) {
        int[] data=quad.vertices();
        int stride=data.length/4;
        List<RailingUvMapping.Vertex> polygon=new ArrayList<>();
        for(int i=0;i<4;i++) {
            float x=Float.intBitsToFloat(data[i*stride]),y=Float.intBitsToFloat(data[i*stride+1]),z=Float.intBitsToFloat(data[i*stride+2]);
            Vector3f local=inverse.transformPosition(new Vector3f(x-.5f,y-.5f,z-.5f)).add(.5f,.5f,.5f).mul(16);
            double[] uv=RailingUvMapping.coordinates(local.x,local.y,local.z,slope,lower,face.getAxis()==Direction.Axis.Z);
            polygon.add(new RailingUvMapping.Vertex(x,y,z,uv[0],uv[1]));
        }
        List<BakedQuad> result=new ArrayList<>();
        for(var piece:RailingUvMapping.tile(polygon)) {
            if(piece.size()==4) result.add(writeQuad(quad,piece));
            else for(int i=1;i<piece.size()-1;i++) {
                // Degenerate fourth vertex encodes triangles using Minecraft's quad format.
                result.add(writeQuad(quad,List.of(piece.getFirst(),piece.get(i),piece.get(i+1),piece.get(i+1))));
            }
        }
        return result;
    }

    private static BakedQuad writeQuad(BakedQuad original,List<RailingUvMapping.Vertex> points) {
        int[] data=original.vertices().clone();int stride=data.length/4;
        TextureAtlasSprite sprite=original.sprite();
        // Keep interpolation inside this sprite even at a tile seam.
        final double inset=0.0001;
        for(int i=0;i<4;i++) {
            var p=points.get(i);int n=i*stride;
            data[n]=Float.floatToRawIntBits((float)p.x());data[n+1]=Float.floatToRawIntBits((float)p.y());data[n+2]=Float.floatToRawIntBits((float)p.z());
            data[n+4]=Float.floatToRawIntBits(sprite.getU((float)Math.clamp(p.u()/16,inset,1-inset)));
            data[n+5]=Float.floatToRawIntBits(sprite.getV((float)Math.clamp(p.v()/16,inset,1-inset)));
        }
        return new BakedQuad(data,original.tintIndex(),original.direction(),sprite,original.shade(),original.lightEmission(),original.hasAmbientOcclusion());
    }
}
