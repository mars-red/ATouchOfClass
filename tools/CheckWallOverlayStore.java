import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.digitaldownpour.atouchofclass.components.WallOverlayStore;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import java.util.Map;

public final class CheckWallOverlayStore {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        var texture = Identifier.parse("atouchofclass:block/check_wallpaper");
        var store = new WallOverlayStore();
        for (int x : new int[]{-17, -16, -1, 0, 15, 16}) {
            var pos = new BlockPos(x, 64, x);
            store.setFace(pos, Direction.NORTH, texture);
            var chunk = new ChunkPos(Math.floorDiv(x, 16), Math.floorDiv(x, 16));
            require(store.getChunkIfPresent(chunk).containsKey(pos), "Wrong chunk for " + x);
            require(texture.equals(store.getFace(pos, Direction.NORTH)), "Missing face");
        }
        var data = new WallOverlayData();
        for (var face : Direction.values()) data.setFace(face, texture);
        var loaded = WallOverlayData.load(data.save());
        var buffer = Unpooled.buffer();
        try {
            WallOverlayData.STREAM_CODEC.encode(buffer, loaded);
            var decoded = WallOverlayData.STREAM_CODEC.decode(buffer);
            for (var face : Direction.values()) require(texture.equals(decoded.getFace(face)), "Lost serialized face");
        } finally { buffer.release(); }
        var pos = new BlockPos(32, 64, 32);
        var chunk = new ChunkPos(2, 2);
        store.replaceChunk(chunk, Map.of(pos, data, new BlockPos(48, 64, 48), loaded));
        require(store.getChunkIfPresent(chunk).size() == 1, "Accepted overlay from another chunk");
        for (var face : Direction.values()) store.setFace(pos, face, null);
        require(!store.getChunks().containsKey(chunk), "Retained empty chunk");
        System.out.println("Overlay checks passed: negative and positive chunk boundaries, all six faces, NBT/network round trips, replacement filtering, and empty-chunk cleanup.");
    }
}
