package com.digitaldownpour.atouchofclass;

import java.util.ArrayList;
import java.util.List;

/** Registry names shared by the block and item registrations. */
public final class FourPostBedVariants {
    public static final List<String> WOODS = List.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped");
    public static final List<String> COLORS = List.of(
            "white", "orange", "magenta", "light_blue", "yellow", "lime",
            "pink", "gray", "light_gray", "cyan", "purple", "blue",
            "brown", "green", "red", "black");
    public static final List<String> IDS = createIds();

    private FourPostBedVariants() {}

    public static String id(String wood, String color) {
        return wood + "_" + color + "_four_post_bed";
    }

    private static List<String> createIds() {
        List<String> ids = new ArrayList<>(WOODS.size() * COLORS.size());
        for (String wood : WOODS) {
            for (String color : COLORS) {
                ids.add(id(wood, color));
            }
        }
        return List.copyOf(ids);
    }
}
