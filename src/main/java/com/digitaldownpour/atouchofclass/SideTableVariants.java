package com.digitaldownpour.atouchofclass;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Material definitions shared by side-table block and item registrations. */
public final class SideTableVariants {
    public static final List<Definition> VALUES = List.of(
            new Definition("oak", Blocks.STRIPPED_OAK_WOOD),
            new Definition("spruce", Blocks.STRIPPED_SPRUCE_WOOD),
            new Definition("birch", Blocks.STRIPPED_BIRCH_WOOD),
            new Definition("jungle", Blocks.STRIPPED_JUNGLE_WOOD),
            new Definition("acacia", Blocks.STRIPPED_ACACIA_WOOD),
            new Definition("dark_oak", Blocks.STRIPPED_DARK_OAK_WOOD),
            new Definition("mangrove", Blocks.STRIPPED_MANGROVE_WOOD),
            new Definition("cherry", Blocks.STRIPPED_CHERRY_WOOD),
            new Definition("pale_oak", Blocks.STRIPPED_PALE_OAK_WOOD),
            new Definition("bamboo", Blocks.STRIPPED_BAMBOO_BLOCK),
            new Definition("crimson", Blocks.STRIPPED_CRIMSON_HYPHAE),
            new Definition("warped", Blocks.STRIPPED_WARPED_HYPHAE)
    );

    private SideTableVariants() {}

    public record Definition(String wood, Block sourceBlock) {
        public String id() {
            return wood + "_side_table";
        }
    }
}
