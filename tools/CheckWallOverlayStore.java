import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.digitaldownpour.atouchofclass.components.ClientWallOverlayData;
import net.minecraft.core.SectionPos;
import java.util.Set;
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
        ClientWallOverlayData.clear();
        var a = new BlockPos(-1, -1, 15);
        var b = new BlockPos(-2, -2, 14);
        ClientWallOverlayData.setFace(a, Direction.NORTH, texture);
        ClientWallOverlayData.setFace(b, Direction.SOUTH, texture);
        require(ClientWallOverlayData.drainDirtySections().equals(Set.of(SectionPos.of(a))),
                "Edits in one section must coalesce, including negative coordinates");
        require(ClientWallOverlayData.drainDirtySections().isEmpty(), "Dirty sections were not drained");
        ClientWallOverlayData.setFace(a, Direction.NORTH, null);
        require(ClientWallOverlayData.drainDirtySections().contains(SectionPos.of(a)), "Removal did not request rebuild");
        var replacement = new BlockPos(-1, 16, 15);
        ClientWallOverlayData.replaceChunk(ChunkPos.containing(b), Map.of(replacement, loaded));
        require(ClientWallOverlayData.drainDirtySections().equals(Set.of(SectionPos.of(b), SectionPos.of(replacement))),
                "Replacement must rebuild both removed and added sections");
        ClientWallOverlayData.replaceChunk(ChunkPos.containing(b), Map.of());
        require(ClientWallOverlayData.drainDirtySections().equals(Set.of(SectionPos.of(replacement))),
                "Empty sync must erase old geometry");
        ClientWallOverlayData.setFace(a, Direction.NORTH, texture);
        ClientWallOverlayData.forgetChunk(ChunkPos.containing(a));
        require(ClientWallOverlayData.getChunks().isEmpty() && ClientWallOverlayData.drainDirtySections().isEmpty(),
                "Chunk unload retained overlays or rebuild requests");
        ClientWallOverlayData.setFace(a, Direction.NORTH, texture);
        ClientWallOverlayData.clear();
        require(ClientWallOverlayData.getChunks().isEmpty() && ClientWallOverlayData.drainDirtySections().isEmpty(),
                "Level unload retained overlays or rebuild requests");
        System.out.println("Client overlay checks passed: rebuild batching, removal, replacement sync, empty sync, and unload cleanup.");
        System.out.println("Overlay checks passed: negative and positive chunk boundaries, all six faces, NBT/network round trips, replacement filtering, and empty-chunk cleanup.");
    }
}
