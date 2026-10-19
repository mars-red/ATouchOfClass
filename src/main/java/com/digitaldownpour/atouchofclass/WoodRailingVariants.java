package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.OakRailingBlock;
import com.digitaldownpour.atouchofclass.blocks.OakNewelPostBlock;
import com.digitaldownpour.atouchofclass.blocks.OakBalusterBlock;
import com.digitaldownpour.atouchofclass.blocks.OakBalustradeBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared behavior and cached geometry for every wood species. Oak retains its existing IDs. */
public final class WoodRailingVariants {
    private WoodRailingVariants() {}
    public record Family(DeferredBlock<OakRailingBlock> railing, DeferredBlock<OakBalusterBlock> baluster,
                         DeferredBlock<OakBalustradeBlock> balustrade, DeferredBlock<OakNewelPostBlock> newelPost) {}

    static Map<String,Family> register() {
        Map<String,Family> families=new LinkedHashMap<>();
        for(String wood:new String[]{"spruce","birch","jungle","acacia","dark_oak","mangrove","cherry","pale_oak","bamboo","crimson","warped"}) {
            var rail=ModBlocks.BLOCKS.register(wood+"_railing",key->new OakRailingBlock(properties(key)));
            var post=ModBlocks.BLOCKS.register(wood+"_baluster",key->new OakBalusterBlock(properties(key)));
            var balustrade=ModBlocks.BLOCKS.register(wood+"_balustrade",key->new OakBalustradeBlock(properties(key)));
            var newel=ModBlocks.BLOCKS.register(wood+"_newel_post",key->new OakNewelPostBlock(properties(key).pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)));
            families.put(wood,new Family(rail,post,balustrade,newel));
        }
        return java.util.Collections.unmodifiableMap(families);
    }
    private static BlockBehaviour.Properties properties(net.minecraft.resources.Identifier key) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,key))
                .mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD).noOcclusion();
    }
}
