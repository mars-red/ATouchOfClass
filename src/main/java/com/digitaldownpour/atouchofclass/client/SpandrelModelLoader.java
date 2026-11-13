package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.blocks.SpandrelBlock.Section;
import com.digitaldownpour.atouchofclass.client.RakeBoardModelLoader.Mesh;
import com.digitaldownpour.atouchofclass.client.RakeBoardModelLoader.Point;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.digitaldownpour.atouchofclass.blocks.SpandrelBlock.BACK;
import static com.digitaldownpour.atouchofclass.blocks.SpandrelBlock.BAND_BOTTOM;
import static com.digitaldownpour.atouchofclass.blocks.SpandrelBlock.FRONT;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class SpandrelModelLoader implements UnbakedModelLoader<CuboidModel> {
    @SubscribeEvent
    public static void register(ModelEvent.RegisterLoaders event) {
        event.register(Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "spandrel"), new SpandrelModelLoader());
    }

    @Override
    public CuboidModel read(JsonObject json, JsonDeserializationContext context) {
        CuboidModel base = context.deserialize(json, CuboidModel.class);
        Section section = Section.valueOf(json.get("section").getAsString().toUpperCase(Locale.ROOT));
        return new CuboidModel(new Geometry(section), base.guiLight(), base.ambientOcclusion(),
                base.transforms(), base.textureSlots(), base.parent(), base.rootTransform(),
                base.partVisibility(), base.itemLayerFaceData());
    }

    private record Geometry(Section section) implements ExtendedUnbakedGeometry {
        @Override
        public QuadCollection bake(TextureSlots slots, ModelBaker baker, ModelState state,
                                   ModelDebugName name, ContextMap properties) {
            var quads = new QuadCollection.Builder();
            Material.Baked wood = baker.materials().resolveSlot(slots, "#wood", name);
            var mesh = new Mesh(quads, state, false);

            piercedFaces(mesh, band(0, 8), diamond(4, 12, 3.2, 2.35), wood);
            piercedFaces(mesh, band(8, 16), diamond(12, 12, 2.2, 1.65), wood);
            if (section == Section.STRAIGHT) {
                mesh.rim(band(0, 16), FRONT, BACK, wood);
            } else {
                // The band pattern stays in phase. Only the tapered extension is mirrored.
                var end = new Mesh(quads, state, section == Section.LEFT);
                var tip = new Point(0, 0);
                var inner = new Point(16, BAND_BOTTOM);
                piercedFaces(end, List.of(tip, inner, new Point(0, BAND_BOTTOM)),
                        diamond(3.5, 5.4, 1.8, 1.55), wood);
                end.rim(List.of(tip, inner, new Point(16, 16), new Point(0, 16)), FRONT, BACK, wood);
            }
            return quads.build();
        }
    }

    private static List<Point> band(double start, double end) {
        return List.of(new Point(start, BAND_BOTTOM), new Point(end, BAND_BOTTOM),
                new Point(end, 16), new Point(start, 16));
    }

    private static List<Point> diamond(double x, double y, double width, double height) {
        return List.of(new Point(x - width, y), new Point(x, y - height),
                new Point(x + width, y), new Point(x, y + height));
    }

    private static void piercedFaces(Mesh mesh, List<Point> outline, List<Point> hole, Material.Baked wood) {
        // Each diamond edge partitions off one solid convex panel. The remaining polygon is the hole.
        List<Point> remainder = outline;
        for (int i = 0; i < hole.size(); i++) {
            Point a = hole.get(i);
            Point b = hole.get((i + 1) % hole.size());
            List<Point> panel = clip(remainder, a, b, false);
            mesh.cap(panel, FRONT, wood, false, true);
            mesh.cap(panel, BACK, wood, false, false);
            remainder = clip(remainder, a, b, true);
        }
        mesh.rim(hole.reversed(), FRONT, BACK, wood);
    }

    private static List<Point> clip(List<Point> polygon, Point a, Point b, boolean inside) {
        List<Point> result = new ArrayList<>();
        for (int i = 0; i < polygon.size(); i++) {
            Point start = polygon.get(i);
            Point end = polygon.get((i + 1) % polygon.size());
            double from = side(a, b, start);
            double to = side(a, b, end);
            if (inside ? from >= 0 : from <= 0) result.add(start);
            if (from * to < 0) {
                double fraction = from / (from - to);
                result.add(new Point(start.x() + (end.x() - start.x()) * fraction,
                        start.y() + (end.y() - start.y()) * fraction));
            }
        }
        return result;
    }

    private static double side(Point a, Point b, Point p) {
        return (b.x() - a.x()) * (p.y() - a.y()) - (b.y() - a.y()) * (p.x() - a.x());
    }
}
