package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.FourPostBedBlock;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Keep custom bed sleepers above the taller bedding on both logical sides. */
@EventBusSubscriber(modid = ATouchofClass.MODID)
public final class FourPostBedSleepEvents {
    // Vanilla uses 11/16: two units above its 9/16 mattress. Our pillow and
    // mattress collision reach 13/16, so retain that same two-unit clearance.
    public static final double SLEEP_HEIGHT = 15.0 / 16;

    private FourPostBedSleepEvents() {}

    @SubscribeEvent
    public static void afterEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity sleeper) alignSleeper(sleeper);
    }

    public static void alignSleeper(LivingEntity sleeper) {
        if (!sleeper.isSleeping()) return;
        var sleepingPos = sleeper.getSleepingPos();
        if (sleepingPos.isEmpty()) return;
        var pos = sleepingPos.get();
        var state = sleeper.level().getBlockState(pos);
        if (!(state.getBlock() instanceof FourPostBedBlock)
                || state.getValue(FourPostBedBlock.UPPER)
                || state.getValue(FourPostBedBlock.SECTION) != FourPostBedBlock.Section.MIDDLE) return;
        double y = pos.getY() + SLEEP_HEIGHT;
        if (Math.abs(sleeper.getY() - y) < 0.000001) return;
        sleeper.setPos(sleeper.getX(), y, sleeper.getZ());
        // Avoid interpolating the body/camera back through the mattress after
        // vanilla positioning on initial sleep, client sync, or saved-data load.
        sleeper.yo = y;
        sleeper.yOld = y;
    }
}
