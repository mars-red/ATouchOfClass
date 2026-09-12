package com.digitaldownpour.atouchofclass.elevator;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import com.digitaldownpour.atouchofclass.blocks.ElevatorCallButtonBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

/**
 * Central, per-server-tick driver for everything time-based in the elevator
 * system: door slide animation stepping, occupancy polling with debounced
 * auto-release, idle-timeout release of called-but-unboarded cabs, and the
 * short delay between arrival-close-complete and the target doors opening.
 * Nothing here is persisted; ServerStoppingEvent resets all state so a
 * restarted server always starts with every shaft idle and every door
 * assumed closed (doors are force-reset to closed the next time a call
 * button/status recompute touches them - see reconciliation note below).
 */
@EventBusSubscriber(modid = ATouchofClass.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ElevatorServerEvents {
    /** How often (in ticks) the occupancy/idle-timeout poll runs; doesn't need to be every tick. */
    private static final int POLL_INTERVAL_TICKS = 10;
    /** Debounce before releasing a lock that was boarded and is now empty. */
    private static final int EMPTY_DEBOUNCE_TICKS = 20;
    /** Timeout before releasing a lock that was called but never boarded. */
    private static final int IDLE_TIMEOUT_TICKS = 600;

    private record DelayedOpen(ServerLevel level, ElevatorLockManager.ShaftKey shaft, int floorIndex, long dueTick) {
    }

    private static final Queue<DelayedOpen> DELAYED_OPENS = new ArrayDeque<>();

    private ElevatorServerEvents() {
    }

    /** Called by ElevatorFlow after teleporting occupants, to open the target doors a short delay later. */
    public static void scheduleDelayedOpen(ServerLevel level, ElevatorLockManager.ShaftKey shaft, int floorIndex) {
        DELAYED_OPENS.add(new DelayedOpen(level, shaft, floorIndex, level.getGameTime() + ElevatorFlow.ARRIVAL_OPEN_DELAY_TICKS));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ElevatorAnimator.tick(level);
        }
        processDelayedOpens();
        long now = event.getServer().getTickCount();
        if (now % POLL_INTERVAL_TICKS != 0) {
            return;
        }
        pollLocks(event.getServer());
    }

    private static void processDelayedOpens() {
        if (DELAYED_OPENS.isEmpty()) {
            return;
        }
        List<DelayedOpen> ready = new ArrayList<>();
        for (DelayedOpen d : DELAYED_OPENS) {
            if (d.level.getGameTime() >= d.dueTick) {
                ready.add(d);
            }
        }
        if (ready.isEmpty()) {
            return;
        }
        DELAYED_OPENS.removeAll(ready);
        for (DelayedOpen d : ready) {
            Optional<ElevatorLockManager.Lock> lockOpt = ElevatorLockManager.get(d.shaft);
            if (lockOpt.isEmpty() || lockOpt.get().floorIndex != d.floorIndex) {
                continue;
            }
            findStopAndOpen(d.level, d.shaft, d.floorIndex);
        }
    }

    private static void findStopAndOpen(ServerLevel level, ElevatorLockManager.ShaftKey key, int floorIndex) {
        Optional<ElevatorUtil.ShaftColumn> shaftOpt = findShaftNear(level, key);
        if (shaftOpt.isEmpty()) {
            return;
        }
        List<ElevatorUtil.FloorStop> stops = ElevatorUtil.listFloorStops(level, shaftOpt.get());
        ElevatorFlow.findStop(stops, floorIndex).ifPresent(stop ->
                ElevatorAnimator.open(level, ElevatorUtil.doorBasesFor(level, stop), () -> {
                }));
        ElevatorFlow.refreshAllButtons(level, stops);
    }

    /** Re-derives the shaft column purely from its known x/z by scanning upward from world bottom for the first panel. */
    private static Optional<ElevatorUtil.ShaftColumn> findShaftNear(ServerLevel level, ElevatorLockManager.ShaftKey key) {
        int minBuildHeight = level.getMinY();
        int maxBuildHeight = level.getMaxY();
        for (int y = minBuildHeight; y <= maxBuildHeight; y++) {
            BlockPos p = new BlockPos(key.x(), y, key.z());
            Optional<ElevatorUtil.ShaftColumn> col = ElevatorUtil.scanShaftColumn(level, p);
            if (col.isPresent()) {
                return col;
            }
        }
        return Optional.empty();
    }

    private static void pollLocks(net.minecraft.server.MinecraftServer server) {
        for (Map.Entry<ElevatorLockManager.ShaftKey, ElevatorLockManager.Lock> entry : ElevatorLockManager.activeLocksView().entrySet()) {
            ElevatorLockManager.ShaftKey key = entry.getKey();
            ElevatorLockManager.Lock lock = entry.getValue();
            if (lock.transiting) {
                continue;
            }
            ServerLevel level = server.getLevel(key.dimension());
            if (level == null) {
                ElevatorLockManager.release(key);
                continue;
            }
            Optional<ElevatorUtil.ShaftColumn> shaftOpt = findShaftNear(level, key);
            if (shaftOpt.isEmpty()) {
                ElevatorLockManager.release(key);
                continue;
            }
            List<ElevatorUtil.FloorStop> stops = ElevatorUtil.listFloorStops(level, shaftOpt.get());
            Optional<ElevatorUtil.FloorStop> stopOpt = ElevatorFlow.findStop(stops, lock.floorIndex);
            if (stopOpt.isEmpty()) {
                ElevatorLockManager.release(key);
                continue;
            }
            Optional<net.minecraft.world.phys.AABB> aabbOpt = stopOpt.get().combinedCabAabb();
            boolean occupied = aabbOpt.isPresent() && !ElevatorFlow.snapshotRootEntities(level, aabbOpt.get()).isEmpty();

            long now = level.getGameTime();
            if (occupied) {
                lock.everOccupied = true;
                lock.emptySinceTick = -1;
                continue;
            }
            if (lock.emptySinceTick < 0) {
                lock.emptySinceTick = now;
                continue;
            }
            long threshold = lock.everOccupied ? EMPTY_DEBOUNCE_TICKS : IDLE_TIMEOUT_TICKS;
            if (now - lock.emptySinceTick >= threshold) {
                List<BlockPos> doorBases = ElevatorUtil.doorBasesFor(level, stopOpt.get());
                ElevatorAnimator.close(level, doorBases, () -> {
                    ElevatorLockManager.release(key);
                    ElevatorFlow.refreshAllButtons(level, ElevatorUtil.listFloorStops(level, shaftOpt.get()));
                });
                // Prevent re-triggering the close animation every poll while it plays out.
                lock.transiting = true;
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ElevatorLockManager.releaseAll();
        ElevatorAnimator.clear();
    }
}
