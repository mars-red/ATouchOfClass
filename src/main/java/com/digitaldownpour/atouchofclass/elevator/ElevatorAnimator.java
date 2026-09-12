package com.digitaldownpour.atouchofclass.elevator;

import com.digitaldownpour.atouchofclass.ModSounds;
import com.digitaldownpour.atouchofclass.blocks.ElevatorDoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Advances elevator door slide animations in discrete steps, one server tick
 * loop driving every in-progress door regardless of which block/shaft it
 * belongs to (there is no per-block scheduled tick - this keeps door motion
 * centrally authoritative, matching how the rest of the elevator system
 * derives behavior rather than having blocks self-drive). Not persisted: an
 * animation interrupted by a server restart just leaves doors at whatever
 * discrete step they were on; ElevatorServerEvents reconciles stale open
 * doors back to closed on the next opportunity.
 */
public final class ElevatorAnimator {
    public static final int STEP_INTERVAL_TICKS = 4;

    public enum Direction { OPEN, CLOSE }

    private static final class Animation {
        final ServerLevel level;
        final List<BlockPos> doorBases;
        final Direction direction;
        final Runnable onComplete;
        long nextStepTick;
        boolean openingSoundPlayed;

        Animation(ServerLevel level, List<BlockPos> doorBases, Direction direction, Runnable onComplete, long nextStepTick) {
            this.level = level;
            this.doorBases = doorBases;
            this.direction = direction;
            this.onComplete = onComplete;
            this.nextStepTick = nextStepTick;
        }
    }

    private static final List<Animation> ACTIVE = new ArrayList<>();

    private ElevatorAnimator() {
    }

    public static void open(ServerLevel level, List<BlockPos> doorBases, Runnable onComplete) {
        start(level, doorBases, Direction.OPEN, onComplete);
    }

    public static void close(ServerLevel level, List<BlockPos> doorBases, Runnable onComplete) {
        start(level, doorBases, Direction.CLOSE, onComplete);
    }

    private static void start(ServerLevel level, List<BlockPos> doorBases, Direction direction, Runnable onComplete) {
        if (doorBases.isEmpty()) {
            onComplete.run();
            return;
        }
        long now = level.getGameTime();
        ACTIVE.add(new Animation(level, doorBases, direction, onComplete, now + STEP_INTERVAL_TICKS));
    }

    /** Called once per server tick; only touches in-progress animations, so idle shafts cost nothing. */
    public static void tick(ServerLevel level) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        List<Animation> completed = new ArrayList<>();
        for (Animation anim : ACTIVE) {
            if (anim.level != level || now < anim.nextStepTick) {
                continue;
            }
            boolean allDone = true;
            for (BlockPos base : anim.doorBases) {
                int step = ElevatorDoorBlock.getSlideStep(level, base);
                int target = anim.direction == Direction.OPEN ? ElevatorDoorBlock.MAX_SLIDE_STEP : 0;
                if (step == target) {
                    continue;
                }
                if (anim.direction == Direction.OPEN && !anim.openingSoundPlayed) {
                    level.playSound(null, base, ModSounds.ELEVATOR_DING.value(), SoundSource.BLOCKS, 0.6F, 1.0F);
                    anim.openingSoundPlayed = true;
                }
                step += anim.direction == Direction.OPEN ? 1 : -1;
                ElevatorDoorBlock.setSlideStep(level, base, step);
                if (step != target) {
                    allDone = false;
                }
            }
            if (allDone) {
                completed.add(anim);
            } else {
                anim.nextStepTick = now + STEP_INTERVAL_TICKS;
            }
        }
        if (!completed.isEmpty()) {
            ACTIVE.removeAll(completed);
            for (Animation anim : completed) {
                anim.onComplete.run();
            }
        }
    }

    /** Cleared on server stop/start alongside ElevatorLockManager - see ElevatorServerEvents. */
    public static void clear() {
        ACTIVE.clear();
    }
}
