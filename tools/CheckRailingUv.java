import com.digitaldownpour.atouchofclass.client.RailingUvMapping;
import com.digitaldownpour.atouchofclass.client.RailingUvMapping.Vertex;
import java.util.List;
import java.util.Random;

public final class CheckRailingUv {
    static void require(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    static boolean close(double a,double b) { return Math.abs(a-b)<1e-8; }
    static boolean sameTile(double a,double b) { return Math.abs((a-b)/16-Math.rint((a-b)/16))<1e-8; }
    static void seam(double x1,double y1,boolean lower1,double x2,double y2,boolean lower2,int slope) {
        for(boolean wall:new boolean[]{true,false}) {
            double[] a=RailingUvMapping.coordinates(x1,y1,13,slope,lower1,wall);
            double[] b=RailingUvMapping.coordinates(x2,y2,13,slope,lower2,wall);
            require(sameTile(a[0],b[0])&&sameTile(a[1],b[1]),"Corner texture seam");
        }
    }
    public static void main(String[] args) {
        Random random=new Random(846);
        for(int i=0;i<2000;i++) {
            double x=random.nextDouble()*32-16,y=random.nextDouble()*32-16;
            double w=.1+random.nextDouble()*16,h=.1+random.nextDouble()*16;
            int slope=i%2==0?1:-1;
            List<Vertex> polygon=new java.util.ArrayList<>();
            for(double[] p:new double[][]{{x,y},{x+w,y},{x+w,y+h},{x,y+h}}) {
                double[] uv=RailingUvMapping.coordinates(p[0],p[1],13,slope,i%3==0,true);
                polygon.add(new Vertex(p[0],p[1],13,uv[0],uv[1]));
            }
            double area=0;
            for(var tile:RailingUvMapping.tile(polygon)) {
                area+=RailingUvMapping.area(tile);
                for(var p:tile) {
                    require(p.u()>=0&&p.u()<=16&&p.v()>=0&&p.v()<=16,"Atlas overflow");
                    require(p.x()>=x-1e-8&&p.x()<=x+w+1e-8&&p.y()>=y-1e-8&&p.y()<=y+h+1e-8,"Changed geometry");
                }
            }
            require(close(area,w*h),"Lost or overlapping surface at a tile boundary");
        }
        seam(8,16,false,8,0,true,1);
        seam(8,16,false,8,0,true,-1);
        for(int slope:new int[]{-1,1}) {
            double[] a=RailingUvMapping.coordinates(3,6,13,slope,false,true);
            double[] b=RailingUvMapping.coordinates(4,6+slope,13,slope,false,true);
            require(close(a[0],b[0])&&!close(a[1],b[1]),"Grain does not follow the 45-degree rail");
            require(close(Math.abs(b[1]-a[1]),Math.sqrt(2)),"Diagonal grain scale differs from vanilla");
            for(double[] delta:new double[][]{{1,0},{0,1},{1,-slope}}) {
                double[] c=RailingUvMapping.coordinates(3+delta[0],6+delta[1],13,slope,false,true);
                require(close(Math.hypot(c[0]-a[0],c[1]-a[1]),Math.hypot(delta[0],delta[1])),
                        "Texture rotation must preserve distances in every direction");
            }
        }
        System.out.println("UV checks passed: 2,000 clipped faces, atlas bounds, area preservation, vertical seams, and scale-preserving 45-degree grain.");
    }
}
