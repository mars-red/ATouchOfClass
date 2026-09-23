package com.digitaldownpour.atouchofclass;

import java.util.List;

/** Registry names shared by dresser block and item registrations. */
public final class DresserVariants {
    public static final List<String> WOODS = List.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped");
    public static final List<String> IDS = WOODS.stream().map(DresserVariants::id).toList();

    private DresserVariants() {}

    public static String id(String wood) {
        return wood + "_dresser";
    }
}
