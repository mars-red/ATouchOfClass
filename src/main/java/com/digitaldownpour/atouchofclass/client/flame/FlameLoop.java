package com.digitaldownpour.atouchofclass.client.flame;

/**
 * A fixed set of pre-simulated flame frames that loops seamlessly. The first frames are
 * cross-faded with the frames that would follow the last one, so the wrap from the last frame
 * back to the first is a natural simulation step rather than a visible jump.
 */
public final class FlameLoop {
    private final int width;
    private final int height;
    private final int[][] frames;

    private FlameLoop(int width, int height, int[][] frames) {
        this.width = width;
        this.height = height;
        this.frames = frames;
    }

    public static FlameLoop bake(int width, int height, int frameCount, int blendFrames, long seed) {
        FlameSimulation sim = new FlameSimulation(width, height, seed);
        for (int i = 0; i < height * 4; i++) {
            sim.step();
        }
        int total = frameCount + blendFrames;
        int[][] raw = new int[total][width * height];
        for (int f = 0; f < total; f++) {
            sim.step();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    raw[f][y * width + x] = sim.heat(x, y);
                }
            }
        }
        int[][] frames = new int[frameCount][];
        for (int f = 0; f < frameCount; f++) {
            if (f >= blendFrames) {
                frames[f] = raw[f];
                continue;
            }
            // Weight shifts from the continuation of the tail (frame f + count) to the frame itself.
            int[] blended = new int[width * height];
            for (int i = 0; i < blended.length; i++) {
                blended[i] = (raw[f][i] * f + raw[f + frameCount][i] * (blendFrames - f)) / blendFrames;
            }
            frames[f] = blended;
        }
        return new FlameLoop(width, height, frames);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int frameCount() {
        return frames.length;
    }

    public int heat(int frame, int x, int y) {
        return frames[frame][y * width + x];
    }
}
