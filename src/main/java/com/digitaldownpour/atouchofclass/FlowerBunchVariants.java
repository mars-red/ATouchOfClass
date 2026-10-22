package com.digitaldownpour.atouchofclass;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Crafting sources and compact model families; foliage blocks are deliberately excluded. */
public final class FlowerBunchVariants {
    public static final List<Definition> VALUES = List.of(
            new Definition("dandelion", Blocks.DANDELION, "small"),
            new Definition("golden_dandelion", Blocks.GOLDEN_DANDELION, "small"),
            new Definition("poppy", Blocks.POPPY, "small"),
            new Definition("blue_orchid", Blocks.BLUE_ORCHID, "small"),
            new Definition("allium", Blocks.ALLIUM, "small"),
            new Definition("azure_bluet", Blocks.AZURE_BLUET, "small"),
            new Definition("red_tulip", Blocks.RED_TULIP, "small"),
            new Definition("orange_tulip", Blocks.ORANGE_TULIP, "small"),
            new Definition("white_tulip", Blocks.WHITE_TULIP, "small"),
            new Definition("pink_tulip", Blocks.PINK_TULIP, "small"),
            new Definition("oxeye_daisy", Blocks.OXEYE_DAISY, "small"),
            new Definition("cornflower", Blocks.CORNFLOWER, "small"),
            new Definition("lily_of_the_valley", Blocks.LILY_OF_THE_VALLEY, "small"),
            new Definition("wither_rose", Blocks.WITHER_ROSE, "small"),
            new Definition("torchflower", Blocks.TORCHFLOWER, "small"),
            new Definition("open_eyeblossom", Blocks.OPEN_EYEBLOSSOM, "small"),
            new Definition("closed_eyeblossom", Blocks.CLOSED_EYEBLOSSOM, "small"),
            new Definition("cactus_flower", Blocks.CACTUS_FLOWER, "small"),
            new Definition("rose_bush", Blocks.ROSE_BUSH, "shrub"),
            new Definition("lilac", Blocks.LILAC, "shrub"),
            new Definition("peony", Blocks.PEONY, "shrub"),
            new Definition("pitcher_plant", Blocks.PITCHER_PLANT, "pitcher"),
            new Definition("sunflower", Blocks.SUNFLOWER, "sunflower"),
            new Definition("pink_petals", Blocks.PINK_PETALS, "mat"),
            new Definition("wildflowers", Blocks.WILDFLOWERS, "mat")
    );

    private FlowerBunchVariants() {}

    public record Definition(String flower, Block sourceBlock, String modelFamily) {
        public String id() { return flower + "_bunch"; }
        public int height() {
            return switch (modelFamily) {
                case "sunflower" -> 14;
                case "shrub", "pitcher" -> 12;
                case "mat" -> 5;
                default -> 9;
            };
        }
    }
}
