package com.digitaldownpour.atouchofclass.elevator;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side, in-memory-only registry of active elevator locks. A lock is
 * interaction state, not world state: it is intentionally never persisted
 * (no SavedData), so a server restart always resumes with every shaft idle -
 * see ElevatorServerEvents for the corresponding safe-default reconciliation
 * that runs on server start/stop.
 */
public final class ElevatorLockManager {
    /** Identifies a shaft by its dimension and horizontal column. */
    public record ShaftKey(ResourceKey<Level> dimension, int x, int z) {
        public static ShaftKey of(ElevatorUtil.ShaftColumn shaft) {
            return new ShaftKey(shaft.dimension(), shaft.x(), shaft.z());
        }
    }

    /**
     * A single shaft's active lock. While transiting is true, the occupancy
     * poll skips this lock entirely (doors are mid slide-animation, not
     * stably open, so "is it empty" is not a meaningful question yet).
     */
    public static final class Lock {
        public final ShaftKey shaft;
        /** Snapshot taken at acquisition time; used to reach the shaft column again without a known Y. */
        public final ElevatorUtil.ShaftColumn shaftColumn;
        public int floorIndex;
        public boolean transiting;
        public boolean everOccupied;
        public long emptySinceTick = -1;
        public final long acquiredAtTick;

        Lock(ShaftKey shaft, ElevatorUtil.ShaftColumn shaftColumn, int floorIndex, long now) {
            this.shaft = shaft;
            this.shaftColumn = shaftColumn;
            this.floorIndex = floorIndex;
            this.acquiredAtTick = now;
        }
    }

    private static final Map<ShaftKey, Lock> ACTIVE_LOCKS = new ConcurrentHashMap<>();

    private ElevatorLockManager() {
    }

    public static synchronized Optional<Lock> tryAcquire(ElevatorUtil.ShaftColumn shaftColumn, int floorIndex, long now) {
        ShaftKey shaft = ShaftKey.of(shaftColumn);
        if (ACTIVE_LOCKS.containsKey(shaft)) {
            return Optional.empty();
        }
        Lock lock = new Lock(shaft, shaftColumn, floorIndex, now);
        ACTIVE_LOCKS.put(shaft, lock);
        return Optional.of(lock);
    }

    public static Optional<Lock> get(ShaftKey shaft) {
        return Optional.ofNullable(ACTIVE_LOCKS.get(shaft));
    }

    public static void release(ShaftKey shaft) {
        ACTIVE_LOCKS.remove(shaft);
    }

    /** Cleared on server stop/start - see ElevatorServerEvents. */
    public static void releaseAll() {
        ACTIVE_LOCKS.clear();
    }

    public static Map<ShaftKey, Lock> activeLocksView() {
        return Map.copyOf(ACTIVE_LOCKS);
    }
}
