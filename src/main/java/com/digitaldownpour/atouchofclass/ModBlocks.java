package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.KeyCutterBlock;
import com.digitaldownpour.atouchofclass.blocks.CasingBlock;
import com.digitaldownpour.atouchofclass.blocks.CornerBlock;
import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.blocks.TallSidelightBlock;
import com.digitaldownpour.atouchofclass.entities.KeyCutterBlockEntity;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ATouchofClass.MODID);

    public static final DeferredBlock<CornerBlock> CORNER_BLOCK = BLOCKS.register("corner_block",
            registryName -> new CornerBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.SNOW)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<CasingBlock> CASING = BLOCKS.register("casing",
            registryName -> new CasingBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.SNOW)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredHolder<Block, TallDoorBlock> TALL_DOOR = BLOCKS.register("tall_door",
            registryName -> new TallDoorBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(3.0F)
                    .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_SIDELIGHT = BLOCKS.register("tall_sidelight",
            registryName -> new TallSidelightBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.NONE)
                    .strength(3.0F)
                    .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, KeyCutterBlock> KEY_CUTTER = BLOCKS.register("key_cutter",
            registryName -> new KeyCutterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .noOcclusion()
                    .strength(3.0F)
            )
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
}
