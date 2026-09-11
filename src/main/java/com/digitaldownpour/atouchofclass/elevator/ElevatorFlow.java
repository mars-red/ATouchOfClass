package com.digitaldownpour.atouchofclass.elevator;

import com.digitaldownpour.atouchofclass.ModNetworking;
import com.digitaldownpour.atouchofclass.network.OpenElevatorFloorsPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Server-side orchestration for the whole call -> board -> select -> arrive
 * lifecycle. Every entry point re-derives shaft/floor/lock state fresh from
 * ElevatorUtil/ElevatorLockManager rather than trusting any previously
 * cached value, including values sent back from the client.
 */
public final class ElevatorFlow {
    /** Ticks between doors finishing the close animation at arrival and starting the open animation. */
    public static final int ARRIVAL_OPEN_DELAY_TICKS = 10;

    private ElevatorFlow() {
    }

    public static void handleCallButtonPress(Level level, BlockPos buttonPos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Optional<ElevatorUtil.Footprint> footprintOpt = ElevatorUtil.floodFillFloor(serverLevel, buttonPos.below());
        if (footprintOpt.isEmpty()) {
            return;
        }
        Optional<ElevatorUtil.ShaftColumn> shaftOpt = ElevatorUtil.shaftForFootprint(serverLevel, footprintOpt.get());
        if (shaftOpt.isEmpty()) {
            return;
        }
        ElevatorUtil.ShaftColumn shaft = shaftOpt.get();
        List<ElevatorUtil.FloorStop> stops = ElevatorUtil.listFloorStops(serverLevel, shaft);
        OptionalInt floorIdxOpt = ElevatorUtil.floorIndexForY(stops, footprintOpt.get().y());
        if (floorIdxOpt.isEmpty()) {
            return;
        }
        int floorIndex = floorIdxOpt.getAsInt();

        Optional<ElevatorLockManager.Lock> lockOpt =
                ElevatorLockManager.tryAcquire(shaft, floorIndex, serverLevel.getGameTime());
        if (lockOpt.isPresent()) {
            findStop(stops, floorIndex).ifPresent(stop ->
                    ElevatorAnimator.open(serverLevel, ElevatorUtil.doorBasesFor(serverLevel, stop), () -> {
                    }));
        }
        refreshAllButtons(serverLevel, stops);
    }

    public static void handleDestinationControlUse(Level level, BlockPos controlPos, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Optional<ElevatorUtil.Footprint> footprintOpt = ElevatorUtil.floodFillFloor(serverLevel, controlPos.below());
        if (footprintOpt.isEmpty()) {
            return;
        }
        Optional<ElevatorUtil.ShaftColumn> shaftOpt = ElevatorUtil.shaftForFootprint(serverLevel, footprintOpt.get());
        if (shaftOpt.isEmpty()) {
            return;
        }
        ElevatorUtil.ShaftColumn shaft = shaftOpt.get();
        List<ElevatorUtil.FloorStop> stops = ElevatorUtil.listFloorStops(serverLevel, shaft);
        OptionalInt floorIdxOpt = ElevatorUtil.floorIndexForY(stops, footprintOpt.get().y());
        if (floorIdxOpt.isEmpty()) {
            return;
        }
        int floorIndex = floorIdxOpt.getAsInt();

        ElevatorLockManager.ShaftKey key = ElevatorLockManager.ShaftKey.of(shaft);
        Optional<ElevatorLockManager.Lock> lockOpt = ElevatorLockManager.get(key);
        if (lockOpt.isEmpty() || lockOpt.get().floorIndex != floorIndex || lockOpt.get().transiting) {
            // Doors aren't stably open at this floor right now - nothing to select from.
            return;
        }

        List<Integer> destinations = stops.stream()
                .map(ElevatorUtil.FloorStop::index)
                .filter(i -> i != floorIndex)
                .toList();
        if (destinations.isEmpty()) {
            return;
        }

        ModNetworking.sendToPlayer(serverPlayer, new OpenElevatorFloorsPayload(controlPos, destinations, floorIndex));
    }

    public static void handleFloorSelected(ServerLevel level, BlockPos controlPos, int targetFloorIndex) {
        Optional<ElevatorUtil.Footprint> originFootprintOpt = ElevatorUtil.floodFillFloor(level, controlPos.below());
        if (originFootprintOpt.isEmpty()) {
            return;
        }
        Optional<ElevatorUtil.ShaftColumn> shaftOpt = ElevatorUtil.shaftForFootprint(level, originFootprintOpt.get());
        if (shaftOpt.isEmpty()) {
            return;
        }
        ElevatorUtil.ShaftColumn shaft = shaftOpt.get();
        List<ElevatorUtil.FloorStop> stops = ElevatorUtil.listFloorStops(level, shaft);
        OptionalInt originIdxOpt = ElevatorUtil.floorIndexForY(stops, originFootprintOpt.get().y());
        if (originIdxOpt.isEmpty()) {
            return;
        }
        int originIndex = originIdxOpt.getAsInt();
        if (originIndex == targetFloorIndex) {
            return;
        }

        ElevatorLockManager.ShaftKey key = ElevatorLockManager.ShaftKey.of(shaft);
        Optional<ElevatorLockManager.Lock> lockOpt = ElevatorLockManager.get(key);
        if (lockOpt.isEmpty() || lockOpt.get().floorIndex != originIndex || lockOpt.get().transiting) {
            return;
        }
        ElevatorLockManager.Lock lock = lockOpt.get();

        Optional<ElevatorUtil.FloorStop> originStopOpt = findStop(stops, originIndex);
        Optional<ElevatorUtil.FloorStop> targetStopOpt = findStop(stops, targetFloorIndex);
        if (originStopOpt.isEmpty() || targetStopOpt.isEmpty()) {
            return;
        }
        ElevatorUtil.FloorStop originStop = originStopOpt.get();
        ElevatorUtil.FloorStop targetStop = targetStopOpt.get();

        Optional<AABB> originAabbOpt = originStop.combinedCabAabb();
        Optional<AABB> targetAabbOpt = targetStop.combinedCabAabb();
        if (originAabbOpt.isEmpty() || targetAabbOpt.isEmpty()) {
            return;
        }
        AABB originAabb = originAabbOpt.get();
        AABB targetAabb = targetAabbOpt.get();

        List<BlockPos> originDoors = ElevatorUtil.doorBasesFor(level, originStop);
        List<Entity> passengerRoots = snapshotRootEntities(level, originAabb);
        double dx = targetAabb.minX - originAabb.minX;
        double dy = targetAabb.minY - originAabb.minY;
        double dz = targetAabb.minZ - originAabb.minZ;

        lock.transiting = true;

        ElevatorAnimator.close(level, originDoors, () -> {
            for (Entity entity : passengerRoots) {
                if (entity.isRemoved()) {
                    continue;
                }
                entity.teleportTo(entity.getX() + dx, entity.getY() + dy, entity.getZ() + dz);
            }
            lock.floorIndex = targetFloorIndex;
            lock.transiting = false;
            lock.everOccupied = lock.everOccupied || !passengerRoots.isEmpty();
            lock.emptySinceTick = -1;
            ElevatorServerEvents.scheduleDelayedOpen(level, key, targetFloorIndex);
        });
    }

    /** Root entities (players, mobs, ridden mounts/vehicles) fully contained in the cab volume, excluding decorative/attached entities. */
    static List<Entity> snapshotRootEntities(ServerLevel level, AABB cabAabb) {
        return level.getEntitiesOfClass(Entity.class, cabAabb,
                        e -> e.getRootVehicle() == e
                                && !(e instanceof HangingEntity)
                                && !(e instanceof LeashFenceKnotEntity))
                .stream()
                .toList();
    }

    static Optional<ElevatorUtil.FloorStop> findStop(List<ElevatorUtil.FloorStop> stops, int index) {
        return stops.stream().filter(s -> s.index() == index).findFirst();
    }

    static void refreshAllButtons(ServerLevel level, List<ElevatorUtil.FloorStop> stops) {
        for (BlockPos pos : ElevatorUtil.allCallButtons(level, stops)) {
            com.digitaldownpour.atouchofclass.blocks.ElevatorCallButtonBlock.refreshStatus(level, pos);
        }
    }
}
