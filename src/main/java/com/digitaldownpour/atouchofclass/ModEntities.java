package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.KeyCutterBlock;
import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.blocks.TallSidelightBlock;
import com.digitaldownpour.atouchofclass.entities.KeyCutterBlockEntity;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import com.digitaldownpour.atouchofclass.entities.DresserBlockEntity;
import com.digitaldownpour.atouchofclass.entities.RefrigeratorBlockEntity;
import com.digitaldownpour.atouchofclass.entities.OvenBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ATouchofClass.MODID);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ATouchofClass.MODID);

    public static final Supplier<BlockEntityType<KeyCutterBlockEntity>> KEY_CUTTER_BE = BLOCK_ENTITY_TYPES.register("key_cutter_be",
            () -> new BlockEntityType<>(
                    KeyCutterBlockEntity::new,
                    ModBlocks.KEY_CUTTER.get()
            )
    );

    public static final Supplier<BlockEntityType<CabinetBlockEntity>> CABINET_BE = BLOCK_ENTITY_TYPES.register("cabinet_be",
            () -> new BlockEntityType<>(
                    CabinetBlockEntity::new,
                    ModBlocks.cabinetBlocks().stream().map(DeferredHolder::get).toArray(Block[]::new)
            )
    );

    public static final Supplier<BlockEntityType<DresserBlockEntity>> DRESSER_BE = BLOCK_ENTITY_TYPES.register("dresser_be",
            () -> new BlockEntityType<>(
                    DresserBlockEntity::new,
                    ModBlocks.dresserBlocks().stream().map(DeferredHolder::get).toArray(Block[]::new)
            )
    );

    public static final Supplier<BlockEntityType<RefrigeratorBlockEntity>> REFRIGERATOR_BE =
            BLOCK_ENTITY_TYPES.register("refrigerator_be", () -> new BlockEntityType<>(
                    RefrigeratorBlockEntity::new,
                    ModBlocks.REFRIGERATOR.get()
            ));

    public static final Supplier<BlockEntityType<OvenBlockEntity>> OVEN_BE =
            BLOCK_ENTITY_TYPES.register("oven_be", () -> new BlockEntityType<>(
                    OvenBlockEntity::new,
                    ModBlocks.OVEN.get()
            ));

    public static final Supplier<BlockEntityType<TallDoorBlockEntity>> TALL_DOOR_BE = BLOCK_ENTITY_TYPES.register("tall_door_be",
            () -> new BlockEntityType<>(
                    TallDoorBlockEntity::new,
                    ModBlocks.TALL_OAK_DOOR.get(),
                    ModBlocks.TALL_SPRUCE_DOOR.get(),
                    ModBlocks.TALL_JUNGLE_DOOR.get(),
                    ModBlocks.TALL_ACACIA_DOOR.get(),
                    ModBlocks.TALL_DARK_OAK_DOOR.get(),
                    ModBlocks.TALL_CHERRY_DOOR.get(),
                    ModBlocks.TALL_BAMBOO_DOOR.get(),
                    ModBlocks.TALL_PALE_OAK_DOOR.get(),
                    ModBlocks.TALL_CRIMSON_DOOR.get(),
                    ModBlocks.TALL_WARPED_DOOR.get(),
                    // TALL_DOOR_VARIANT_BLOCK_ENTITY_ENTRIES
                    ModBlocks.TALL_MANGROVE_DOOR.get(),
                    ModBlocks.TALL_BIRCH_DOOR.get()
            )
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
}
