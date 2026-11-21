package com.digitaldownpour.atouchofclass;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Concrete colours shared by fire pit block and item registrations. */
public final class FirePitVariants {
    public static final List<Definition> VALUES = Arrays.stream(DyeColor.values())
            .map(dye -> new Definition(dye.getName(), Blocks.CONCRETE.pick(dye)))
            .toList();

    private FirePitVariants() {}

    public record Definition(String color, Block sourceBlock) {
        public String id() { return color + "_concrete_fire_pit"; }
    }
}
