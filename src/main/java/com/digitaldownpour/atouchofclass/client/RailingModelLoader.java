package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
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
public final class RailingModelLoader implements UnbakedModelLoader<CuboidModel> {
    @SubscribeEvent
    public static void register(ModelEvent.RegisterLoaders event) {
        event.register(Identifier.fromNamespaceAndPath(ATouchofClass.MODID,"railing"), new RailingModelLoader());
    }

    @Override
    public CuboidModel read(JsonObject json, JsonDeserializationContext context) {
        CuboidModel base=context.deserialize(json,CuboidModel.class);
        List<CuboidModelElement> elements=new ArrayList<>();
        for(var e:json.getAsJsonArray("elements")) elements.add(context.deserialize(e,CuboidModelElement.class));
        return new CuboidModel(new RailingGeometry(elements, json.get("grain_slope").getAsInt(),
                json.get("grain_lower").getAsBoolean()), base.guiLight(), base.ambientOcclusion(),
                base.transforms(), base.textureSlots(), base.parent(), base.rootTransform(),
                base.partVisibility(), base.itemLayerFaceData());
    }

    private record RailingGeometry(List<CuboidModelElement> elements, int slope, boolean lower)
            implements ExtendedUnbakedGeometry {
        RailingGeometry {
            if (slope != 1 && slope != -1) throw new IllegalArgumentException("grain_slope must be 1 or -1");
        }

        @Override
        public QuadCollection bake(TextureSlots slots, ModelBaker baker, ModelState state,
                                   ModelDebugName debugName, ContextMap properties) {
            var builder = new QuadCollection.Builder();
            Matrix4f inverse=new Matrix4f(state.transformation().getMatrix()).invert();
            for(CuboidModelElement element:elements) {
                for(var entry:element.faces().entrySet()) {
                    Direction face=entry.getKey();
                    CuboidFace definition=entry.getValue();
                    var material = baker.materials().resolveSlot(slots, definition.texture(), debugName);
                    BakedQuad quad=FaceBakery.bakeQuad(baker, element.from(), element.to(), definition,
                            material, face, state, element.rotation(), element.shadeDirectionOverride(),
                            element.lightEmission());
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
        List<RailingUvMapping.Vertex> polygon=new ArrayList<>();
        for(int i=0;i<4;i++) {
            var position=quad.position(i);
            float x=position.x(), y=position.y(), z=position.z();
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
        Vector3f[] positions=new Vector3f[4];
        long[] uvs=new long[4];
        TextureAtlasSprite sprite=original.materialInfo().sprite();
        // Keep interpolation inside this sprite even at a tile seam.
        final double inset=0.0001;
        for(int i=0;i<4;i++) {
            var p=points.get(i);
            positions[i]=new Vector3f((float)p.x(),(float)p.y(),(float)p.z());
            uvs[i]=UVPair.pack(sprite.getU((float)Math.clamp(p.u()/16,inset,1-inset)),
                    sprite.getV((float)Math.clamp(p.v()/16,inset,1-inset)));
        }
        return new BakedQuad(positions[0],positions[1],positions[2],positions[3],
                uvs[0],uvs[1],uvs[2],uvs[3],original.direction(),original.materialInfo(),
                original.bakedNormals(),original.bakedColors());
    }
}
