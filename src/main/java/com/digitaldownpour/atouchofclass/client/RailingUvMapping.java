package com.digitaldownpour.atouchofclass.client;

import java.util.ArrayList;
import java.util.List;

/** Pure bake-time mapping and polygon clipping, independent of Minecraft rendering. */
public final class RailingUvMapping {
    private RailingUvMapping() {}

    public record Vertex(double x, double y, double z, double u, double v) {
        Vertex interpolate(Vertex b, double t) {
            return new Vertex(x + (b.x-x)*t, y + (b.y-y)*t, z + (b.z-z)*t,
                    u + (b.u-u)*t, v + (b.v-v)*t);
        }
    }

    public static double[] coordinates(double x, double y, double z, int slope, boolean lower, boolean wallFace) {
        // A true 45-degree rotation preserves vanilla texel density. Keep the
        // vertical corner pair aligned; horizontal joins may have a grain seam.
        // Everything remains baked, with no per-position or per-frame UV work.
        double divisor = Math.sqrt(2);
        double phase = lower ? 16 / divisor : 0;
        double u = wallFace ? (y - slope*x)/divisor + phase : z - 11.25;
        double v = (x + slope*y)/divisor + slope*phase;
        return new double[] {u, v};
    }

    public static List<List<Vertex>> tile(List<Vertex> polygon) {
        double minU = polygon.stream().mapToDouble(Vertex::u).min().orElseThrow();
        double maxU = polygon.stream().mapToDouble(Vertex::u).max().orElseThrow();
        double minV = polygon.stream().mapToDouble(Vertex::v).min().orElseThrow();
        double maxV = polygon.stream().mapToDouble(Vertex::v).max().orElseThrow();
        List<List<Vertex>> result = new ArrayList<>();
        for (int u = (int)Math.floor(minU/16); u <= (int)Math.floor(maxU/16); u++) {
            for (int v = (int)Math.floor(minV/16); v <= (int)Math.floor(maxV/16); v++) {
                List<Vertex> piece = clip(clip(polygon, true, u*16, true), true, (u+1)*16, false);
                piece = clip(clip(piece, false, v*16, true), false, (v+1)*16, false);
                if (piece.size() < 3 || area(piece) < 1e-12) continue;
                List<Vertex> wrapped = new ArrayList<>();
                for (Vertex p : piece) wrapped.add(new Vertex(p.x,p.y,p.z,
                        Math.clamp(p.u-u*16,0,16), Math.clamp(p.v-v*16,0,16)));
                result.add(List.copyOf(wrapped));
            }
        }
        return result;
    }

    private static List<Vertex> clip(List<Vertex> polygon, boolean uAxis, double edge, boolean greater) {
        List<Vertex> out = new ArrayList<>();
        for (int i=0; i<polygon.size(); i++) {
            Vertex a=polygon.get(i), b=polygon.get((i+1)%polygon.size());
            double av=uAxis?a.u:a.v, bv=uAxis?b.u:b.v;
            boolean ai=greater?av>=edge:av<=edge, bi=greater?bv>=edge:bv<=edge;
            if (ai) out.add(a);
            if (ai != bi) out.add(a.interpolate(b,(edge-av)/(bv-av)));
        }
        return out;
    }

    public static double area(List<Vertex> polygon) {
        double nx=0,ny=0,nz=0;
        for(int i=0;i<polygon.size();i++) {
            Vertex a=polygon.get(i),b=polygon.get((i+1)%polygon.size());
            nx+=(a.y-b.y)*(a.z+b.z); ny+=(a.z-b.z)*(a.x+b.x); nz+=(a.x-b.x)*(a.y+b.y);
        }
        return Math.sqrt(nx*nx+ny*ny+nz*nz)/2;
    }
}
