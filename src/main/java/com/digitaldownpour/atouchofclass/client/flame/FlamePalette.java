package com.digitaldownpour.atouchofclass.client.flame;

/** Posterized heat-to-colour ramp, matching the banded look of vanilla fire textures. */
public final class FlamePalette {
    private static final int[] THRESHOLDS = {40, 80, 125, 170, 210, 240};
    private static final int[] COLORS = {
            0xC8A02008, // ember edge
            0xDCC8380A, // deep red
            0xF0E8681A, // red-orange
            0xFFF59A1E, // orange
            0xFFFFC832, // yellow
            0xFFFFF0A0  // white-hot core
    };

    private FlamePalette() {}

    /** Returns an ARGB colour; fully transparent below the lowest threshold. */
    public static int argb(int heat) {
        int color = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (heat >= THRESHOLDS[i]) {
                color = COLORS[i];
            }
        }
        return color;
    }
}
