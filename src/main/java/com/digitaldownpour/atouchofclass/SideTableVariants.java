package com.digitaldownpour.atouchofclass;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Material definitions shared by side-table block and item registrations. */
public final class SideTableVariants {
    public static final List<Definition> VALUES = List.of(
            new Definition("oak", Blocks.STRIPPED_OAK_WOOD)
    );

    private SideTableVariants() {}

    public record Definition(String wood, Block sourceBlock) {
        public String id() {
            return wood + "_side_table";
        }
    }
}
