package com.digitaldownpour.atouchofclass.client.flame;

import java.util.SplittableRandom;

/**
 * Classic 2D fire automaton: the bottom rows are re-seeded with random heat every step and each
 * cell above is a cooled, blurred blend of the cells beneath it. Pure Java with no Minecraft
 * dependencies, so it is deterministic for a given seed and safe to unit test.
 */
public final class FlameSimulation {
    public static final int MAX_HEAT = 255;

    private final int width;
    private final int height;
    private final int[] heat;
    private final int[] profile;
    private final int[] scratch;
    private final SplittableRandom random;

    public FlameSimulation(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.heat = new int[width * height];
        this.scratch = new int[width * height];
        this.random = new SplittableRandom(seed);
        this.profile = new int[width];
        // Flat seed strength and wrapped columns make the flame tile horizontally across joined pits.
        for (int x = 0; x < width; x++) {
            double d = (x + 0.5) / width * 2.0 - 1.0;
            profile[x] = MAX_HEAT;
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** Heat in [0, 255]; row 0 is the top of the flame. */
    public int heat(int x, int y) {
        return heat[y * width + x];
    }

    public void step() {
        for (int y = 0; y < height - 2; y++) {
            for (int x = 0; x < width; x++) {
                // Random sideways drift gives the tongues their licking motion.
                int drift = random.nextInt(4) == 0 ? random.nextInt(3) - 1 : 0;
                int left = at(x - 1 + drift, y + 1);
                int mid = at(x + drift, y + 1);
                int right = at(x + 1 + drift, y + 1);
                int far = at(x, y + 2);
                int blurred = (left + 2 * mid + right + far * 2) / 6;
                scratch[y * width + x] = Math.max(0, blurred - 38 - random.nextInt(34));
            }
        }
        for (int y = height - 2; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int seed = random.nextInt(8) == 0 ? 90 : 190 + random.nextInt(66);
                scratch[y * width + x] = seed * profile[x] / MAX_HEAT;
            }
        }
        System.arraycopy(scratch, 0, heat, 0, heat.length);
    }

    private int at(int x, int y) {
        int cx = Math.floorMod(x, width);
        return heat[y * width + cx];
    }
}
