package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.KeyCutterBlock;
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
import net.minecraft.world.level.material.PushReaction;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ATouchofClass.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ATouchofClass.MODID);

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

    public static final Supplier<BlockEntityType<KeyCutterBlockEntity>> KEY_CUTTER_BE = BLOCK_ENTITY_TYPES.register("key_cutter_be",
            () -> new BlockEntityType<>(
                    KeyCutterBlockEntity::new,
                    KEY_CUTTER.get()
            )
    );

    public static final Supplier<BlockEntityType<TallDoorBlockEntity>> TALL_DOOR_BE = BLOCK_ENTITY_TYPES.register("tall_door_be",
            () -> new BlockEntityType<>(
                    TallDoorBlockEntity::new,
                    TALL_DOOR.get()
            )
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
}
