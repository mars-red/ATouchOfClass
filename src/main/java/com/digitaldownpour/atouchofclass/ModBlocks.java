package com.digitaldownpour.atouchofclass;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
//import net.neoforged.neoforge.registries.RegistryObject;

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
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredHolder<Block, KeyCutterBlock> KEY_CUTTER = BLOCKS.register("key_cutter",
            registryName -> new KeyCutterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .noOcclusion()
                    .strength(3.0F)
            ));

    public static final DeferredItem<BlockItem> TALL_DOOR_ITEM = ModItems.ITEMS.registerSimpleBlockItem("tall_door", TALL_DOOR);

    public static final DeferredItem<BlockItem> KEY_CUTTER_ITEM = ModItems.ITEMS.registerSimpleBlockItem("key_cutter", KEY_CUTTER);

    public static final Supplier<BlockEntityType<KeyCutterBlockEntity>> KEY_CUTTER_BE = BLOCK_ENTITY_TYPES.register("key_cutter_be",
            () -> new BlockEntityType<>(
                    KeyCutterBlockEntity::new,
                    KEY_CUTTER.get()
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);

        eventBus.addListener(ModBlocks::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(TALL_DOOR_ITEM);
            event.accept(KEY_CUTTER_ITEM);
        }
    }
}
