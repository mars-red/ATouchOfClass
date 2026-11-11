package com.digitaldownpour.atouchofclass.client;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.blocks.RakeBoardBlock;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.joml.Vector3f;

import java.util.List;

import static com.digitaldownpour.atouchofclass.blocks.RakeBoardBlock.BOARD_BACK;
import static com.digitaldownpour.atouchofclass.blocks.RakeBoardBlock.BOARD_FRONT;
import static com.digitaldownpour.atouchofclass.blocks.RakeBoardBlock.WIDTH;

@EventBusSubscriber(modid = ATouchofClass.MODID, value = Dist.CLIENT)
public final class RakeBoardModelLoader implements UnbakedModelLoader<CuboidModel> {
    private static final double ROOT_TWO = Math.sqrt(2);

    @SubscribeEvent
    public static void register(ModelEvent.RegisterLoaders event) {
        event.register(Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "rake_board"), new RakeBoardModelLoader());
    }

    @Override
    public CuboidModel read(JsonObject json, JsonDeserializationContext context) {
        CuboidModel base = context.deserialize(json, CuboidModel.class);
        boolean includeFiller = !json.has("include_filler") || json.get("include_filler").getAsBoolean();
        return new CuboidModel(new Geometry(!json.get("rises_left").getAsBoolean(), includeFiller),
                base.guiLight(), base.ambientOcclusion(), base.transforms(), base.textureSlots(), base.parent(),
                base.rootTransform(), base.partVisibility(), base.itemLayerFaceData());
    }

    private record Geometry(boolean mirrored, boolean includeFiller) implements ExtendedUnbakedGeometry {
        @Override
        public QuadCollection bake(TextureSlots slots, ModelBaker baker, ModelState state,
                                   ModelDebugName name, ContextMap properties) {
            QuadCollection.Builder quads = new QuadCollection.Builder();
            Material.Baked board = baker.materials().resolveSlot(slots, "#board", name);
            Mesh mesh = new Mesh(quads, state, mirrored);
            if (includeFiller) {
                Material.Baked filler = baker.materials().resolveSlot(slots, "#filler", name);
                List<Point> wedge = List.of(new Point(0, 0), new Point(16, 16), new Point(0, 16));
                mesh.cap(wedge, 0, filler, false, true);
                mesh.cap(wedge, 16, filler, false, false);
                mesh.rim(wedge, 0, 16, filler);
            }
            mesh.boardHalf(0, 2.2, 1.65, board);
            mesh.boardHalf(8, 3.2, 2.35, board);
            // The two halves share their center edge. Only the outside ends need caps.
            mesh.edge(boardPoint(0, 0), boardPoint(0, WIDTH), BOARD_FRONT, BOARD_BACK, board);
            mesh.edge(boardPoint(16, WIDTH), boardPoint(16, 0), BOARD_FRONT, BOARD_BACK, board);
            return quads.build();
        }
    }

    private record Point(double x, double y) {}

    private static Point boardPoint(double x, double across) {
        return new Point(x, x - across * ROOT_TWO);
    }

    private record Mesh(QuadCollection.Builder quads, ModelState state, boolean mirrored) {
        void boardHalf(double start, double along, double across, Material.Baked material) {
            double end = start + 8;
            double center = start + 4;
            double reach = along / ROOT_TWO;
            double spread = across / ROOT_TWO;
            Point left = boardPoint(center - reach, WIDTH / 2);
            Point bottom = boardPoint(center + spread, WIDTH / 2 + across);
            Point right = boardPoint(center + reach, WIDTH / 2);
            Point top = boardPoint(center - spread, WIDTH / 2 - across);
            Point outerLeft = boardPoint(start, WIDTH / 2);
            Point outerBottom = boardPoint(bottom.x, WIDTH);
            Point outerRight = boardPoint(end, WIDTH / 2);
            Point outerTop = boardPoint(top.x, 0);

            // Four convex panels surround the diamond. The interior rim faces into the opening.
            panel(List.of(left, outerLeft, boardPoint(start, WIDTH), outerBottom, bottom), material);
            panel(List.of(bottom, outerBottom, boardPoint(end, WIDTH), outerRight, right), material);
            panel(List.of(right, outerRight, boardPoint(end, 0), outerTop, top), material);
            panel(List.of(top, outerTop, boardPoint(start, 0), outerLeft, left), material);
            rim(List.of(top, right, bottom, left), BOARD_FRONT, BOARD_BACK, material);
            edge(boardPoint(start, WIDTH), boardPoint(end, WIDTH), BOARD_FRONT, BOARD_BACK, material);
            edge(boardPoint(end, 0), boardPoint(start, 0), BOARD_FRONT, BOARD_BACK, material);
        }

        void panel(List<Point> polygon, Material.Baked material) {
            cap(polygon, BOARD_FRONT, material, true, true);
            cap(polygon, BOARD_BACK, material, true, false);
        }

        void cap(List<Point> polygon, double z, Material.Baked material, boolean wood, boolean front) {
            var vertices = polygon.stream().map(p -> new RailingUvMapping.Vertex(p.x, p.y, z,
                    wood ? (p.x + p.y) / ROOT_TWO : p.x,
                    wood ? (p.x - p.y) / ROOT_TWO : 16 - p.y)).toList();
            emit(front ? vertices.reversed() : vertices, material);
        }

        void rim(List<Point> polygon, double front, double back, Material.Baked material) {
            for (int i = 0; i < polygon.size(); i++) {
                edge(polygon.get(i), polygon.get((i + 1) % polygon.size()), front, back, material);
            }
        }

        void edge(Point a, Point b, double front, double back, Material.Baked material) {
            double length = Math.hypot(b.x - a.x, b.y - a.y);
            emit(List.of(new RailingUvMapping.Vertex(a.x, a.y, front, 0, 0),
                    new RailingUvMapping.Vertex(b.x, b.y, front, length, 0),
                    new RailingUvMapping.Vertex(b.x, b.y, back, length, back - front),
                    new RailingUvMapping.Vertex(a.x, a.y, back, 0, back - front)), material);
        }

        void emit(List<RailingUvMapping.Vertex> vertices, Material.Baked material) {
            for (var polygon : RailingUvMapping.tile(mirrored ? vertices.reversed() : vertices)) {
                if (polygon.size() == 4) {
                    quad(polygon, material);
                } else {
                    for (int i = 1; i < polygon.size() - 1; i++) {
                        var triangle = List.of(polygon.getFirst(), polygon.get(i), polygon.get(i + 1), polygon.get(i + 1));
                        if (RailingUvMapping.area(triangle) > 1e-8) quad(triangle, material);
                    }
                }
            }
        }

        void quad(List<RailingUvMapping.Vertex> points, Material.Baked material) {
            Vector3f[] positions = new Vector3f[4];
            long[] uv = new long[4];
            var sprite = material.sprite();
            for (int i = 0; i < 4; i++) {
                var point = points.get(i);
                double x = mirrored ? 16 - point.x() : point.x();
                Vector3f position = new Vector3f((float) (x / 16), (float) (point.y() / 16), (float) (point.z() / 16));
                positions[i] = state.transformation().getMatrix().transformPosition(position.sub(.5f, .5f, .5f))
                        .add(.5f, .5f, .5f);
                uv[i] = UVPair.pack(sprite.getU((float) (point.u() / 16)), sprite.getV((float) (point.v() / 16)));
            }
            Vector3f normal = new Vector3f(positions[1]).sub(positions[0])
                    .cross(new Vector3f(positions[2]).sub(positions[0]));
            if (normal.lengthSquared() < 1e-12f) {
                normal = new Vector3f(positions[2]).sub(positions[0])
                        .cross(new Vector3f(positions[3]).sub(positions[0]));
            }
            Direction direction = Direction.getApproximateNearest(normal.x, normal.y, normal.z);
            var info = BakedQuad.MaterialInfo.of(material, sprite.contents().transparency(), -1, null, 0);
            quads.addUnculledFace(new BakedQuad(positions[0], positions[1], positions[2], positions[3],
                    uv[0], uv[1], uv[2], uv[3], direction, info));
        }
    }
}
