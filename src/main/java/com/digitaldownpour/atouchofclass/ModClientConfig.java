package com.digitaldownpour.atouchofclass;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue FORCE_DISABLE_OUTDOOR_UPLIGHT_SPOTLIGHT = BUILDER
            .comment("Force Outdoor Uplights to use the wall wash even with an active Iris shader pack.")
            .define("forceDisableOutdoorUplightSpotlight", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModClientConfig() {}
}
