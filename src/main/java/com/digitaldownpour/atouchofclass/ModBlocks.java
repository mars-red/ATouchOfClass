package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.WallSconceBlock;
import com.digitaldownpour.atouchofclass.blocks.CopperChainBlock;
import com.digitaldownpour.atouchofclass.blocks.CopperChandelierBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCopperChainBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCopperChandelierBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringWallSconceBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import com.digitaldownpour.atouchofclass.blocks.KeyCutterBlock;
import com.digitaldownpour.atouchofclass.blocks.CasingBlock;
import com.digitaldownpour.atouchofclass.blocks.CrownMouldingBlock;
import com.digitaldownpour.atouchofclass.blocks.BaseboardBlock;
import com.digitaldownpour.atouchofclass.blocks.WindowApronBlock;
import com.digitaldownpour.atouchofclass.blocks.PlinthBlock;
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

    public static final DeferredBlock<CrownMouldingBlock> CROWN_MOULDING = BLOCKS.register("crown_moulding",
            registryName -> new CrownMouldingBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.SNOW)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<BaseboardBlock> BASEBOARD = BLOCKS.register("baseboard",
            registryName -> new BaseboardBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.SNOW)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<WindowApronBlock> WINDOW_APRON = BLOCKS.register("window_apron",
            registryName -> new WindowApronBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.SNOW)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<PlinthBlock> PLINTH_BLOCK = BLOCKS.register("plinth_block",
            registryName -> new PlinthBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.SNOW)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .noOcclusion()));

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

    public static final DeferredBlock<WallSconceBlock> COPPER_WALL_SCONCE = registerSconce("copper_wall_sconce", WeatherState.UNAFFECTED, false);
    public static final DeferredBlock<WallSconceBlock> EXPOSED_COPPER_WALL_SCONCE = registerSconce("exposed_copper_wall_sconce", WeatherState.EXPOSED, false);
    public static final DeferredBlock<WallSconceBlock> WEATHERED_COPPER_WALL_SCONCE = registerSconce("weathered_copper_wall_sconce", WeatherState.WEATHERED, false);
    public static final DeferredBlock<WallSconceBlock> OXIDIZED_COPPER_WALL_SCONCE = registerSconce("oxidized_copper_wall_sconce", WeatherState.OXIDIZED, false);
    public static final DeferredBlock<WallSconceBlock> WAXED_COPPER_WALL_SCONCE = registerSconce("waxed_copper_wall_sconce", WeatherState.UNAFFECTED, true);
    public static final DeferredBlock<WallSconceBlock> WAXED_EXPOSED_COPPER_WALL_SCONCE = registerSconce("waxed_exposed_copper_wall_sconce", WeatherState.EXPOSED, true);
    public static final DeferredBlock<WallSconceBlock> WAXED_WEATHERED_COPPER_WALL_SCONCE = registerSconce("waxed_weathered_copper_wall_sconce", WeatherState.WEATHERED, true);
    public static final DeferredBlock<WallSconceBlock> WAXED_OXIDIZED_COPPER_WALL_SCONCE = registerSconce("waxed_oxidized_copper_wall_sconce", WeatherState.OXIDIZED, true);

    private static DeferredBlock<WallSconceBlock> registerSconce(String name, WeatherState age, boolean waxed) {
        return BLOCKS.register(name, key -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, key))
                    .mapColor(age.ordinal() < 2 ? MapColor.COLOR_ORANGE : MapColor.COLOR_CYAN)
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(net.minecraft.world.level.block.SoundType.COPPER)
                    .noOcclusion().lightLevel(state -> 15);
            return waxed ? new WallSconceBlock(properties) : new WeatheringWallSconceBlock(age, properties);
        });
    }
    public static final DeferredBlock<CopperChainBlock> COPPER_CHAIN = registerCopperChain("copper_chain", WeatherState.UNAFFECTED, false);
    public static final DeferredBlock<CopperChainBlock> EXPOSED_COPPER_CHAIN = registerCopperChain("exposed_copper_chain", WeatherState.EXPOSED, false);
    public static final DeferredBlock<CopperChainBlock> WEATHERED_COPPER_CHAIN = registerCopperChain("weathered_copper_chain", WeatherState.WEATHERED, false);
    public static final DeferredBlock<CopperChainBlock> OXIDIZED_COPPER_CHAIN = registerCopperChain("oxidized_copper_chain", WeatherState.OXIDIZED, false);
    public static final DeferredBlock<CopperChainBlock> WAXED_COPPER_CHAIN = registerCopperChain("waxed_copper_chain", WeatherState.UNAFFECTED, true);
    public static final DeferredBlock<CopperChainBlock> WAXED_EXPOSED_COPPER_CHAIN = registerCopperChain("waxed_exposed_copper_chain", WeatherState.EXPOSED, true);
    public static final DeferredBlock<CopperChainBlock> WAXED_WEATHERED_COPPER_CHAIN = registerCopperChain("waxed_weathered_copper_chain", WeatherState.WEATHERED, true);
    public static final DeferredBlock<CopperChainBlock> WAXED_OXIDIZED_COPPER_CHAIN = registerCopperChain("waxed_oxidized_copper_chain", WeatherState.OXIDIZED, true);

    private static DeferredBlock<CopperChainBlock> registerCopperChain(String name, WeatherState age, boolean waxed) {
        return BLOCKS.register(name, key -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, key))
                    .mapColor(age.ordinal() < 2 ? MapColor.COLOR_ORANGE : MapColor.COLOR_CYAN)
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(net.minecraft.world.level.block.SoundType.COPPER).noOcclusion();
            return waxed ? new CopperChainBlock(properties) : new WeatheringCopperChainBlock(age, properties);
        });
    }
    public static final DeferredBlock<CopperChandelierBlock> COPPER_CHANDELIER = registerCopperChandelier("copper_chandelier", WeatherState.UNAFFECTED, false);
    public static final DeferredBlock<CopperChandelierBlock> EXPOSED_COPPER_CHANDELIER = registerCopperChandelier("exposed_copper_chandelier", WeatherState.EXPOSED, false);
    public static final DeferredBlock<CopperChandelierBlock> WEATHERED_COPPER_CHANDELIER = registerCopperChandelier("weathered_copper_chandelier", WeatherState.WEATHERED, false);
    public static final DeferredBlock<CopperChandelierBlock> OXIDIZED_COPPER_CHANDELIER = registerCopperChandelier("oxidized_copper_chandelier", WeatherState.OXIDIZED, false);
    public static final DeferredBlock<CopperChandelierBlock> WAXED_COPPER_CHANDELIER = registerCopperChandelier("waxed_copper_chandelier", WeatherState.UNAFFECTED, true);
    public static final DeferredBlock<CopperChandelierBlock> WAXED_EXPOSED_COPPER_CHANDELIER = registerCopperChandelier("waxed_exposed_copper_chandelier", WeatherState.EXPOSED, true);
    public static final DeferredBlock<CopperChandelierBlock> WAXED_WEATHERED_COPPER_CHANDELIER = registerCopperChandelier("waxed_weathered_copper_chandelier", WeatherState.WEATHERED, true);
    public static final DeferredBlock<CopperChandelierBlock> WAXED_OXIDIZED_COPPER_CHANDELIER = registerCopperChandelier("waxed_oxidized_copper_chandelier", WeatherState.OXIDIZED, true);

    private static DeferredBlock<CopperChandelierBlock> registerCopperChandelier(String name, WeatherState age, boolean waxed) {
        return BLOCKS.register(name, key -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, key))
                    .mapColor(age.ordinal() < 2 ? MapColor.COLOR_ORANGE : MapColor.COLOR_CYAN)
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(net.minecraft.world.level.block.SoundType.COPPER).noOcclusion().lightLevel(state -> 15);
            return waxed ? new CopperChandelierBlock(properties) : new WeatheringCopperChandelierBlock(age, properties);
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
}
