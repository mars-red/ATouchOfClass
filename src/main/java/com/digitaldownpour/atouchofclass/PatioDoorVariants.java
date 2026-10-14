package com.digitaldownpour.atouchofclass;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Shared vanilla wood catalog for patio registration and asset generation. */
public final class PatioDoorVariants {
    public static final List<Definition> VALUES = List.of(
            new Definition("oak", Blocks.OAK_PLANKS),
            new Definition("spruce", Blocks.SPRUCE_PLANKS),
            new Definition("birch", Blocks.BIRCH_PLANKS),
            new Definition("jungle", Blocks.JUNGLE_PLANKS),
            new Definition("acacia", Blocks.ACACIA_PLANKS),
            new Definition("dark_oak", Blocks.DARK_OAK_PLANKS),
            new Definition("mangrove", Blocks.MANGROVE_PLANKS),
            new Definition("cherry", Blocks.CHERRY_PLANKS),
            new Definition("pale_oak", Blocks.PALE_OAK_PLANKS),
            new Definition("bamboo", Blocks.BAMBOO_PLANKS),
            new Definition("crimson", Blocks.CRIMSON_PLANKS),
            new Definition("warped", Blocks.WARPED_PLANKS)
    );

    private PatioDoorVariants() {}

    public record Definition(String wood, Block sourceBlock) {
        public String id() { return "tall_" + wood + "_patio_door"; }
    }
}
