package com.digitaldownpour.atouchofclass;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Material definitions for nightstands. Oak remains the hand-tuned source variant. */
public final class NightstandVariants {
    public static final List<Definition> VALUES = List.of(
            // GENERATED_NIGHTSTAND_DEFINITIONS_START
            new Definition("oak", Blocks.STRIPPED_OAK_WOOD)
            // GENERATED_NIGHTSTAND_DEFINITIONS_END
    );

    private NightstandVariants() {}

    public record Definition(String wood, Block sourceBlock) {
        public String id() {
            return wood + "_nightstand";
        }
    }
}
