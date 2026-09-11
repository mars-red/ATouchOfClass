package com.digitaldownpour.atouchofclass.elevator;

import com.digitaldownpour.atouchofclass.blocks.ElevatorCallButtonBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorDestinationControlBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorDoorBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorFloorBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorShaftPanelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Pure discovery logic for the elevator system. Nothing here is persisted:
 * shaft membership, floor footprints, and floor numbering are always
 * recomputed from the live block layout at the moment they're needed. This
 * is what lets floor blocks be placed/removed with no invalidation
 * bookkeeping, and what makes a crooked/discontiguous shaft fail safe
 * (the discovery simply finds nothing usable) rather than needing explicit
 * validation rules.
 */
public final class ElevatorUtil {
    /** Hard cap on a single floor's connected footprint, bounding flood-fill cost. */
    public static final int MAX_FOOTPRINT_BLOCKS = 256;
    /** Hard cap on shaft column scan distance in each direction. */
    public static final int MAX_SHAFT_SCAN = 256;
    /** Interior AABB height above a cab floor - fixed per design decision. */
    public static final double INTERIOR_HEIGHT = 5.0;

    private ElevatorUtil() {
    }

    /** Identifies a single vertical shaft column. */
    public record ShaftColumn(ResourceKey<Level> dimension, int x, int z, int minY, int maxY) {
    }

    /** A single connected patch of ElevatorFloorBlock at one Y level. */
    public record Footprint(int y, Set<BlockPos> floorBlocks, boolean hasControl, boolean hasCall) {
        public AABB toAabb() {
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
            for (BlockPos p : floorBlocks) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minZ = Math.min(minZ, p.getZ());
                maxZ = Math.max(maxZ, p.getZ());
            }
            return new AABB(minX, y + 1, minZ, maxX + 1, y + 1 + INTERIOR_HEIGHT, maxZ + 1);
        }
    }

    /**
     * All footprints sharing one Y along one shaft: this is what makes two
     * cabs on opposite shaft sides at the same height a single linked
     * floor-stop, sharing one call/board/release lifecycle. Only footprints
     * that contain a destination control count as boardable "cabs" - a
     * footprint with only a call button (e.g. a waiting foyer) contributes
     * to the floor-stop's existence but not to its occupancy/teleport AABB.
     */
    public record FloorStop(int index, int y, List<Footprint> footprints) {
        public List<Footprint> cabs() {
            return footprints.stream().filter(Footprint::hasControl).toList();
        }

        public Optional<AABB> combinedCabAabb() {
            AABB result = null;
            for (Footprint f : cabs()) {
                AABB next = f.toAabb();
                result = result == null ? next : union(result, next);
            }
            return Optional.ofNullable(result);
        }
    }

    private static AABB union(AABB a, AABB b) {
        return new AABB(
                Math.min(a.minX, b.minX), Math.min(a.minY, b.minY), Math.min(a.minZ, b.minZ),
                Math.max(a.maxX, b.maxX), Math.max(a.maxY, b.maxY), Math.max(a.maxZ, b.maxZ)
        );
    }

    /** Flood-fills the connected ElevatorFloorBlock patch containing start, bounded by MAX_FOOTPRINT_BLOCKS. */
    public static Optional<Footprint> floodFillFloor(Level level, BlockPos start) {
        if (!(level.getBlockState(start).getBlock() instanceof ElevatorFloorBlock)) {
            return Optional.empty();
        }
        int y = start.getY();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        boolean hasControl = false;
        boolean hasCall = false;
        while (!queue.isEmpty()) {
            if (visited.size() > MAX_FOOTPRINT_BLOCKS) {
                return Optional.empty();
            }
            BlockPos current = queue.poll();
            BlockState above = level.getBlockState(current.above());
            if (above.getBlock() instanceof ElevatorDestinationControlBlock) {
                hasControl = true;
            }
            if (above.getBlock() instanceof ElevatorCallButtonBlock) {
                hasCall = true;
            }
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(dir);
                if (visited.contains(next)) {
                    continue;
                }
                if (level.getBlockState(next).getBlock() instanceof ElevatorFloorBlock) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return Optional.of(new Footprint(y, visited, hasControl, hasCall));
    }

    /**
     * Finds the single shaft column touched by a footprint's blocks. Returns
     * empty if zero or more than one distinct column is touched - ambiguity
     * is treated as non-functional rather than guessed at.
     */
    public static Optional<ShaftColumn> shaftForFootprint(Level level, Footprint footprint) {
        Integer shaftX = null;
        Integer shaftZ = null;
        for (BlockPos p : footprint.floorBlocks()) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos next = p.relative(dir);
                if (level.getBlockState(next).getBlock() instanceof ElevatorShaftPanelBlock) {
                    if (shaftX == null) {
                        shaftX = next.getX();
                        shaftZ = next.getZ();
                    } else if (shaftX != next.getX() || shaftZ != next.getZ()) {
                        return Optional.empty();
                    }
                }
            }
        }
        if (shaftX == null) {
            return Optional.empty();
        }
        return scanShaftColumn(level, new BlockPos(shaftX, footprint.y(), shaftZ));
    }

    /** Scans a straight vertical panel column upward/downward from a known panel position. */
    public static Optional<ShaftColumn> scanShaftColumn(Level level, BlockPos anyPanelPos) {
        if (!(level.getBlockState(anyPanelPos).getBlock() instanceof ElevatorShaftPanelBlock)) {
            return Optional.empty();
        }
        int minY = anyPanelPos.getY();
        int maxY = anyPanelPos.getY();
        for (int i = 1; i <= MAX_SHAFT_SCAN; i++) {
            BlockPos p = new BlockPos(anyPanelPos.getX(), anyPanelPos.getY() - i, anyPanelPos.getZ());
            if (!(level.getBlockState(p).getBlock() instanceof ElevatorShaftPanelBlock)) {
                break;
            }
            minY = p.getY();
        }
        for (int i = 1; i <= MAX_SHAFT_SCAN; i++) {
            BlockPos p = new BlockPos(anyPanelPos.getX(), anyPanelPos.getY() + i, anyPanelPos.getZ());
            if (!(level.getBlockState(p).getBlock() instanceof ElevatorShaftPanelBlock)) {
                break;
            }
            maxY = p.getY();
        }
        return Optional.of(new ShaftColumn(level.dimension(), anyPanelPos.getX(), anyPanelPos.getZ(), minY, maxY));
    }

    /**
     * Enumerates every floor-stop along a shaft: for each Y in the column,
     * discover every footprint touching the shaft from any horizontal side
     * and group them together. Floor index is derived purely from ascending
     * Y order - there is no other source of floor numbering.
     */
    public static List<FloorStop> listFloorStops(Level level, ShaftColumn shaft) {
        List<FloorStop> stops = new ArrayList<>();
        int index = 0;
        for (int y = shaft.minY(); y <= shaft.maxY(); y++) {
            Set<BlockPos> seeds = new HashSet<>();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = new BlockPos(shaft.x(), y, shaft.z()).relative(dir);
                if (level.getBlockState(neighbor).getBlock() instanceof ElevatorFloorBlock) {
                    seeds.add(neighbor);
                }
            }
            if (seeds.isEmpty()) {
                continue;
            }
            List<Footprint> footprints = new ArrayList<>();
            Set<BlockPos> claimed = new HashSet<>();
            for (BlockPos seed : seeds) {
                if (claimed.contains(seed)) {
                    continue;
                }
                Optional<Footprint> fp = floodFillFloor(level, seed);
                if (fp.isPresent()) {
                    claimed.addAll(fp.get().floorBlocks());
                    footprints.add(fp.get());
                }
            }
            if (!footprints.isEmpty()) {
                stops.add(new FloorStop(index, y, footprints));
                index++;
            }
        }
        return stops;
    }

    public static OptionalInt floorIndexForY(List<FloorStop> stops, int y) {
        for (FloorStop stop : stops) {
            if (stop.y() == y) {
                return OptionalInt.of(stop.index());
            }
        }
        return OptionalInt.empty();
    }

    /** Finds all door base (part == 0) positions belonging to a floor-stop's cabs. */
    public static List<BlockPos> doorBasesFor(Level level, FloorStop stop) {
        List<BlockPos> bases = new ArrayList<>();
        for (Footprint cab : stop.cabs()) {
            for (BlockPos floorPos : cab.floorBlocks()) {
                BlockPos above = floorPos.above();
                BlockState state = level.getBlockState(above);
                if (state.getBlock() instanceof ElevatorDoorBlock && state.getValue(ElevatorDoorBlock.PART) == 0) {
                    bases.add(above);
                }
            }
        }
        return bases;
    }

    /** Finds every call button position anywhere along the shaft (every floor-stop, every footprint). */
    public static List<BlockPos> allCallButtons(Level level, List<FloorStop> stops) {
        List<BlockPos> buttons = new ArrayList<>();
        for (FloorStop stop : stops) {
            for (Footprint fp : stop.footprints()) {
                for (BlockPos floorPos : fp.floorBlocks()) {
                    BlockPos above = floorPos.above();
                    if (level.getBlockState(above).getBlock() instanceof ElevatorCallButtonBlock) {
                        buttons.add(above);
                    }
                }
            }
        }
        return buttons;
    }
}
