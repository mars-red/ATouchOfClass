package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.WallSconceBlock;
import com.digitaldownpour.atouchofclass.blocks.OakRailingBlock;
import com.digitaldownpour.atouchofclass.blocks.OakBalustradeBlock;
import com.digitaldownpour.atouchofclass.blocks.OakBalusterBlock;
import com.digitaldownpour.atouchofclass.blocks.CopperChandelierChainBlock;
import com.digitaldownpour.atouchofclass.blocks.CopperChandelierBlock;
import com.digitaldownpour.atouchofclass.blocks.CopperLampPostBlock;
import com.digitaldownpour.atouchofclass.blocks.CopperFloorLampBlock;
import com.digitaldownpour.atouchofclass.blocks.CopperCeilingLightBlock;
import com.digitaldownpour.atouchofclass.blocks.CopperTableLampBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCopperChandelierChainBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCopperChandelierBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCopperLampPostBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCopperFloorLampBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCopperCeilingLightBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCopperTableLampBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringWallSconceBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import com.digitaldownpour.atouchofclass.blocks.KeyCutterBlock;
import com.digitaldownpour.atouchofclass.blocks.CasingBlock;
import com.digitaldownpour.atouchofclass.blocks.CrownMouldingBlock;
import com.digitaldownpour.atouchofclass.blocks.BaseboardBlock;
import com.digitaldownpour.atouchofclass.blocks.WindowApronBlock;
import com.digitaldownpour.atouchofclass.blocks.WindowLedgeBlock;
import com.digitaldownpour.atouchofclass.blocks.PlinthBlock;
import com.digitaldownpour.atouchofclass.blocks.PlasterColumnBlock;
import com.digitaldownpour.atouchofclass.blocks.CornerBlock;
import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.blocks.TallSidelightBlock;
import com.digitaldownpour.atouchofclass.blocks.GlassShowerDoorBlock;
import com.digitaldownpour.atouchofclass.blocks.GlassShowerSurroundBlock;
import com.digitaldownpour.atouchofclass.blocks.RainShowerHeadBlock;
import com.digitaldownpour.atouchofclass.blocks.WallShowerHeadBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorShaftPanelBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorFloorBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorDoorBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorCallButtonBlock;
import com.digitaldownpour.atouchofclass.blocks.ElevatorDestinationControlBlock;
import com.digitaldownpour.atouchofclass.blocks.ToiletBlock;
import com.digitaldownpour.atouchofclass.blocks.ClawfootBathtubBlock;
import com.digitaldownpour.atouchofclass.blocks.FourPostBedBlock;
import com.digitaldownpour.atouchofclass.blocks.RefrigeratorBlock;
import com.digitaldownpour.atouchofclass.blocks.OvenBlock;
import com.digitaldownpour.atouchofclass.blocks.InductionCooktopBlock;
import com.digitaldownpour.atouchofclass.blocks.CabinetBlock;
import com.digitaldownpour.atouchofclass.blocks.DoorCabinetBlock;
import com.digitaldownpour.atouchofclass.blocks.DresserBlock;
import com.digitaldownpour.atouchofclass.blocks.LoveSeatBlock;
import com.digitaldownpour.atouchofclass.blocks.SofaBlock;
import com.digitaldownpour.atouchofclass.blocks.ChairBlock;
import com.digitaldownpour.atouchofclass.blocks.DiningChairBlock;
import com.digitaldownpour.atouchofclass.blocks.SideTableBlock;
import com.digitaldownpour.atouchofclass.blocks.DiningTableBlock;
import com.digitaldownpour.atouchofclass.blocks.NightstandBlock;
import com.digitaldownpour.atouchofclass.blocks.CountertopBlock;
import com.digitaldownpour.atouchofclass.blocks.SinkBlock;
import com.digitaldownpour.atouchofclass.blocks.KitchenSinkBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringCountertopBlock;
import com.digitaldownpour.atouchofclass.blocks.WeatheringSinkBlock;
import com.digitaldownpour.atouchofclass.entities.KeyCutterBlockEntity;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
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

    // Entries in this list are maintained by tools/generate-basic-block.ps1.
    private static final java.util.List<String> BASIC_BLOCK_NAMES = java.util.List.of(
            "acacia_parquet",
            "bamboo_parquet",
            "birch_parquet",
            "poplar_parquet",
            "calcite_tile",
            "cherry_parquet",
            "crimson_parquet",
            "dark_oak_parquet",
            "jungle_parquet",
            "mangrove_parquet",
            "marble_tile",
            "oak_parquet",
            "octagonal_inset_tile",
            "pale_oak_parquet",
            "spruce_parquet",
            "warped_parquet"
    );
    public static final java.util.Map<String, DeferredBlock<Block>> BASIC_BLOCKS = registerBasicBlocks();

    public static final DeferredBlock<WindowLedgeBlock> BRICK_WINDOW_LEDGE =
            BLOCKS.register("brick_window_ledge", registryName -> new WindowLedgeBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .noOcclusion()));

    public static final DeferredBlock<WindowLedgeBlock> BUFF_BRICK_WINDOW_LEDGE =
            BLOCKS.register("buff_brick_window_ledge", registryName -> new WindowLedgeBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .noOcclusion()));

    public static final DeferredBlock<SlabBlock> CALCITE_SLAB =
            BLOCKS.register("calcite_slab", registryName -> new SlabBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<Block> BUFF_BRICKS =
            BLOCKS.register("buff_bricks", registryName -> new Block(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<SlabBlock> BUFF_BRICK_SLAB =
            BLOCKS.register("buff_brick_slab", registryName -> new SlabBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BRICK_SLAB)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<StairBlock> BUFF_BRICK_STAIRS =
            BLOCKS.register("buff_brick_stairs", registryName -> new StairBlock(
                    BUFF_BRICKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BRICK_STAIRS)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))));
    public static final DeferredBlock<WallBlock> BUFF_BRICK_WALL =
            BLOCKS.register("buff_brick_wall", registryName -> new WallBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.BRICK_WALL)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))));

    public static final DeferredBlock<CountertopBlock> CALCITE_COUNTERTOP =
            BLOCKS.register("calcite_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.QUARTZ)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.CALCITE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<KitchenSinkBlock> KITCHEN_SINK =
            BLOCKS.register("kitchen_sink", registryName -> new KitchenSinkBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .strength(3.0F, 6.0F)
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> CALCITE_SINK =
            BLOCKS.register("calcite_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.QUARTZ)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.CALCITE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<CountertopBlock> SMOOTH_STONE_COUNTERTOP =
            BLOCKS.register("smooth_stone_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> SMOOTH_STONE_SINK =
            BLOCKS.register("smooth_stone_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> STONE_COUNTERTOP =
            BLOCKS.register("stone_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> STONE_SINK =
            BLOCKS.register("stone_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> STONE_BRICK_COUNTERTOP =
            BLOCKS.register("stone_brick_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> STONE_BRICK_SINK =
            BLOCKS.register("stone_brick_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> NETHER_BRICK_COUNTERTOP =
            BLOCKS.register("nether_brick_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> NETHER_BRICK_SINK =
            BLOCKS.register("nether_brick_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> QUARTZ_COUNTERTOP =
            BLOCKS.register("quartz_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> QUARTZ_SINK =
            BLOCKS.register("quartz_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> POLISHED_GRANITE_COUNTERTOP =
            BLOCKS.register("polished_granite_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> POLISHED_GRANITE_SINK =
            BLOCKS.register("polished_granite_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> POLISHED_DIORITE_COUNTERTOP =
            BLOCKS.register("polished_diorite_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> POLISHED_DIORITE_SINK =
            BLOCKS.register("polished_diorite_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> END_STONE_BRICK_COUNTERTOP =
            BLOCKS.register("end_stone_brick_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> END_STONE_BRICK_SINK =
            BLOCKS.register("end_stone_brick_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> SMOOTH_QUARTZ_COUNTERTOP =
            BLOCKS.register("smooth_quartz_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> SMOOTH_QUARTZ_SINK =
            BLOCKS.register("smooth_quartz_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> GRANITE_COUNTERTOP =
            BLOCKS.register("granite_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> GRANITE_SINK =
            BLOCKS.register("granite_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> ANDESITE_COUNTERTOP =
            BLOCKS.register("andesite_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> ANDESITE_SINK =
            BLOCKS.register("andesite_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> RED_NETHER_BRICK_COUNTERTOP =
            BLOCKS.register("red_nether_brick_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> RED_NETHER_BRICK_SINK =
            BLOCKS.register("red_nether_brick_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> POLISHED_ANDESITE_COUNTERTOP =
            BLOCKS.register("polished_andesite_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> POLISHED_ANDESITE_SINK =
            BLOCKS.register("polished_andesite_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> DIORITE_COUNTERTOP =
            BLOCKS.register("diorite_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> DIORITE_SINK =
            BLOCKS.register("diorite_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> POLISHED_BLACKSTONE_COUNTERTOP =
            BLOCKS.register("polished_blackstone_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> POLISHED_BLACKSTONE_SINK =
            BLOCKS.register("polished_blackstone_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> POLISHED_BLACKSTONE_BRICK_COUNTERTOP =
            BLOCKS.register("polished_blackstone_brick_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> POLISHED_BLACKSTONE_BRICK_SINK =
            BLOCKS.register("polished_blackstone_brick_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> POLISHED_DEEPSLATE_COUNTERTOP =
            BLOCKS.register("polished_deepslate_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> POLISHED_DEEPSLATE_SINK =
            BLOCKS.register("polished_deepslate_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> DEEPSLATE_BRICK_COUNTERTOP =
            BLOCKS.register("deepslate_brick_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> DEEPSLATE_BRICK_SINK =
            BLOCKS.register("deepslate_brick_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> DEEPSLATE_TILE_COUNTERTOP =
            BLOCKS.register("deepslate_tile_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> DEEPSLATE_TILE_SINK =
            BLOCKS.register("deepslate_tile_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> RESIN_BRICK_COUNTERTOP =
            BLOCKS.register("resin_brick_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> RESIN_BRICK_SINK =
            BLOCKS.register("resin_brick_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> PRISMARINE_BRICK_COUNTERTOP =
            BLOCKS.register("prismarine_brick_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> PRISMARINE_BRICK_SINK =
            BLOCKS.register("prismarine_brick_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> DARK_PRISMARINE_COUNTERTOP =
            BLOCKS.register("dark_prismarine_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> DARK_PRISMARINE_SINK =
            BLOCKS.register("dark_prismarine_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> PURPUR_COUNTERTOP =
            BLOCKS.register("purpur_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> PURPUR_SINK =
            BLOCKS.register("purpur_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> WHITE_CONCRETE_COUNTERTOP =
            BLOCKS.register("white_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> WHITE_CONCRETE_SINK =
            BLOCKS.register("white_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> LIGHT_GRAY_CONCRETE_COUNTERTOP =
            BLOCKS.register("light_gray_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> LIGHT_GRAY_CONCRETE_SINK =
            BLOCKS.register("light_gray_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> GRAY_CONCRETE_COUNTERTOP =
            BLOCKS.register("gray_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> GRAY_CONCRETE_SINK =
            BLOCKS.register("gray_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> BLACK_CONCRETE_COUNTERTOP =
            BLOCKS.register("black_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> BLACK_CONCRETE_SINK =
            BLOCKS.register("black_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> BROWN_CONCRETE_COUNTERTOP =
            BLOCKS.register("brown_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> BROWN_CONCRETE_SINK =
            BLOCKS.register("brown_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> RED_CONCRETE_COUNTERTOP =
            BLOCKS.register("red_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> RED_CONCRETE_SINK =
            BLOCKS.register("red_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> ORANGE_CONCRETE_COUNTERTOP =
            BLOCKS.register("orange_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> ORANGE_CONCRETE_SINK =
            BLOCKS.register("orange_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> YELLOW_CONCRETE_COUNTERTOP =
            BLOCKS.register("yellow_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> YELLOW_CONCRETE_SINK =
            BLOCKS.register("yellow_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> LIME_CONCRETE_COUNTERTOP =
            BLOCKS.register("lime_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> LIME_CONCRETE_SINK =
            BLOCKS.register("lime_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> GREEN_CONCRETE_COUNTERTOP =
            BLOCKS.register("green_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> GREEN_CONCRETE_SINK =
            BLOCKS.register("green_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> CYAN_CONCRETE_COUNTERTOP =
            BLOCKS.register("cyan_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> CYAN_CONCRETE_SINK =
            BLOCKS.register("cyan_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> LIGHT_BLUE_CONCRETE_COUNTERTOP =
            BLOCKS.register("light_blue_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> LIGHT_BLUE_CONCRETE_SINK =
            BLOCKS.register("light_blue_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> BLUE_CONCRETE_COUNTERTOP =
            BLOCKS.register("blue_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> BLUE_CONCRETE_SINK =
            BLOCKS.register("blue_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> PURPLE_CONCRETE_COUNTERTOP =
            BLOCKS.register("purple_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> PURPLE_CONCRETE_SINK =
            BLOCKS.register("purple_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> MAGENTA_CONCRETE_COUNTERTOP =
            BLOCKS.register("magenta_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> MAGENTA_CONCRETE_SINK =
            BLOCKS.register("magenta_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> PINK_CONCRETE_COUNTERTOP =
            BLOCKS.register("pink_concrete_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> PINK_CONCRETE_SINK =
            BLOCKS.register("pink_concrete_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    public static final DeferredBlock<CountertopBlock> CUT_COPPER_COUNTERTOP =
            registerCopperCountertop("cut_copper_countertop", WeatherState.UNAFFECTED, false, Blocks.CUT_COPPER.weathering().unaffected());

    public static final DeferredBlock<SinkBlock> CUT_COPPER_SINK =
            registerCopperSink("cut_copper_sink", WeatherState.UNAFFECTED, false, Blocks.CUT_COPPER.weathering().unaffected());

    public static final DeferredBlock<CountertopBlock> EXPOSED_CUT_COPPER_COUNTERTOP =
            registerCopperCountertop("exposed_cut_copper_countertop", WeatherState.EXPOSED, false,
                    Blocks.CUT_COPPER.weathering().exposed());

    public static final DeferredBlock<SinkBlock> EXPOSED_CUT_COPPER_SINK =
            registerCopperSink("exposed_cut_copper_sink", WeatherState.EXPOSED, false,
                    Blocks.CUT_COPPER.weathering().exposed());

    public static final DeferredBlock<CountertopBlock> WEATHERED_CUT_COPPER_COUNTERTOP =
            registerCopperCountertop("weathered_cut_copper_countertop", WeatherState.WEATHERED, false,
                    Blocks.CUT_COPPER.weathering().weathered());

    public static final DeferredBlock<SinkBlock> WEATHERED_CUT_COPPER_SINK =
            registerCopperSink("weathered_cut_copper_sink", WeatherState.WEATHERED, false,
                    Blocks.CUT_COPPER.weathering().weathered());

    public static final DeferredBlock<CountertopBlock> OXIDIZED_CUT_COPPER_COUNTERTOP =
            registerCopperCountertop("oxidized_cut_copper_countertop", WeatherState.OXIDIZED, false,
                    Blocks.CUT_COPPER.weathering().oxidized());

    public static final DeferredBlock<SinkBlock> OXIDIZED_CUT_COPPER_SINK =
            registerCopperSink("oxidized_cut_copper_sink", WeatherState.OXIDIZED, false,
                    Blocks.CUT_COPPER.weathering().oxidized());

    public static final DeferredBlock<CountertopBlock> WAXED_CUT_COPPER_COUNTERTOP =
            registerCopperCountertop("waxed_cut_copper_countertop", WeatherState.UNAFFECTED, true,
                    Blocks.CUT_COPPER.waxed().unaffected());

    public static final DeferredBlock<SinkBlock> WAXED_CUT_COPPER_SINK =
            registerCopperSink("waxed_cut_copper_sink", WeatherState.UNAFFECTED, true,
                    Blocks.CUT_COPPER.waxed().unaffected());

    public static final DeferredBlock<CountertopBlock> WAXED_EXPOSED_CUT_COPPER_COUNTERTOP =
            registerCopperCountertop("waxed_exposed_cut_copper_countertop", WeatherState.EXPOSED, true,
                    Blocks.CUT_COPPER.waxed().exposed());

    public static final DeferredBlock<SinkBlock> WAXED_EXPOSED_CUT_COPPER_SINK =
            registerCopperSink("waxed_exposed_cut_copper_sink", WeatherState.EXPOSED, true,
                    Blocks.CUT_COPPER.waxed().exposed());

    public static final DeferredBlock<CountertopBlock> WAXED_WEATHERED_CUT_COPPER_COUNTERTOP =
            registerCopperCountertop("waxed_weathered_cut_copper_countertop", WeatherState.WEATHERED, true,
                    Blocks.CUT_COPPER.waxed().weathered());

    public static final DeferredBlock<SinkBlock> WAXED_WEATHERED_CUT_COPPER_SINK =
            registerCopperSink("waxed_weathered_cut_copper_sink", WeatherState.WEATHERED, true,
                    Blocks.CUT_COPPER.waxed().weathered());

    public static final DeferredBlock<CountertopBlock> WAXED_OXIDIZED_CUT_COPPER_COUNTERTOP =
            registerCopperCountertop("waxed_oxidized_cut_copper_countertop", WeatherState.OXIDIZED, true,
                    Blocks.CUT_COPPER.waxed().oxidized());

    public static final DeferredBlock<SinkBlock> WAXED_OXIDIZED_CUT_COPPER_SINK =
            registerCopperSink("waxed_oxidized_cut_copper_sink", WeatherState.OXIDIZED, true,
                    Blocks.CUT_COPPER.waxed().oxidized());

    private static DeferredBlock<CountertopBlock> registerCopperCountertop(
            String name, WeatherState age, boolean waxed, Block vanillaBlock) {
        return BLOCKS.register(name, registryName -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(vanillaBlock)
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .noOcclusion();
            return waxed ? new CountertopBlock(properties) : new WeatheringCountertopBlock(age, properties);
        });
    }

    private static DeferredBlock<SinkBlock> registerCopperSink(
            String name, WeatherState age, boolean waxed, Block vanillaBlock) {
        return BLOCKS.register(name, registryName -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(vanillaBlock)
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .noOcclusion();
            return waxed ? new SinkBlock(properties) : new WeatheringSinkBlock(age, properties);
        });
    }

    public static final DeferredBlock<CountertopBlock> CINNABAR_COUNTERTOP =
            BLOCKS.register("cinnabar_countertop", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> CINNABAR_SINK =
            BLOCKS.register("cinnabar_sink", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));
    // COUNTERTOP_SINK_VARIANT_REGISTRATIONS

    private static final java.util.List<DeferredHolder<Block, ? extends CabinetBlock>> CABINET_REGISTRATIONS =
            new java.util.ArrayList<>();

    public static final DeferredBlock<DoorCabinetBlock> DARK_OAK_DOOR_CABINET =
            registerDoorCabinet("dark_oak_door_cabinet");

    public static final DeferredBlock<CabinetBlock> DARK_OAK_DRAWER_CABINET =
            registerCabinet("dark_oak_drawer_cabinet");

    public static final DeferredBlock<DoorCabinetBlock> PALE_OAK_DOOR_CABINET =
            registerDoorCabinet("pale_oak_door_cabinet");

    public static final DeferredBlock<CabinetBlock> PALE_OAK_DRAWER_CABINET =
            registerCabinet("pale_oak_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> OAK_DOOR_CABINET =
            registerDoorCabinet("oak_door_cabinet");

    public static final DeferredBlock<CabinetBlock> OAK_DRAWER_CABINET =
            registerCabinet("oak_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> SPRUCE_DOOR_CABINET =
            registerDoorCabinet("spruce_door_cabinet");

    public static final DeferredBlock<CabinetBlock> SPRUCE_DRAWER_CABINET =
            registerCabinet("spruce_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> BIRCH_DOOR_CABINET =
            registerDoorCabinet("birch_door_cabinet");

    public static final DeferredBlock<DoorCabinetBlock> POPLAR_DOOR_CABINET =
            registerDoorCabinet("poplar_door_cabinet");

    public static final DeferredBlock<CabinetBlock> POPLAR_DRAWER_CABINET =
            registerCabinet("poplar_drawer_cabinet");

    public static final DeferredBlock<CabinetBlock> BIRCH_DRAWER_CABINET =
            registerCabinet("birch_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> JUNGLE_DOOR_CABINET =
            registerDoorCabinet("jungle_door_cabinet");

    public static final DeferredBlock<CabinetBlock> JUNGLE_DRAWER_CABINET =
            registerCabinet("jungle_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> ACACIA_DOOR_CABINET =
            registerDoorCabinet("acacia_door_cabinet");

    public static final DeferredBlock<CabinetBlock> ACACIA_DRAWER_CABINET =
            registerCabinet("acacia_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> MANGROVE_DOOR_CABINET =
            registerDoorCabinet("mangrove_door_cabinet");

    public static final DeferredBlock<CabinetBlock> MANGROVE_DRAWER_CABINET =
            registerCabinet("mangrove_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> CHERRY_DOOR_CABINET =
            registerDoorCabinet("cherry_door_cabinet");

    public static final DeferredBlock<CabinetBlock> CHERRY_DRAWER_CABINET =
            registerCabinet("cherry_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> BAMBOO_DOOR_CABINET =
            registerDoorCabinet("bamboo_door_cabinet");

    public static final DeferredBlock<CabinetBlock> BAMBOO_DRAWER_CABINET =
            registerCabinet("bamboo_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> CRIMSON_DOOR_CABINET =
            registerDoorCabinet("crimson_door_cabinet");

    public static final DeferredBlock<CabinetBlock> CRIMSON_DRAWER_CABINET =
            registerCabinet("crimson_drawer_cabinet");
    public static final DeferredBlock<DoorCabinetBlock> WARPED_DOOR_CABINET =
            registerDoorCabinet("warped_door_cabinet");

    public static final DeferredBlock<CabinetBlock> WARPED_DRAWER_CABINET =
            registerCabinet("warped_drawer_cabinet");
    // CABINET_VARIANT_REGISTRATIONS

    private static final java.util.List<DeferredHolder<Block, ? extends DresserBlock>> DRESSER_REGISTRATIONS =
            new java.util.ArrayList<>();
    public static final java.util.Map<String, DeferredBlock<DresserBlock>> DRESSERS = registerDressers();

    public static final java.util.Map<String, DeferredBlock<SideTableBlock>> SIDE_TABLES = registerSideTables();
    public static final DeferredBlock<SideTableBlock> OAK_SIDE_TABLE = SIDE_TABLES.get("oak_side_table");

    public static final java.util.Map<String, DeferredBlock<DiningTableBlock>> DINING_TABLES = registerDiningTables();
    public static final DeferredBlock<DiningTableBlock> OAK_DINING_TABLE = DINING_TABLES.get("oak_dining_table");

    public static final java.util.Map<String, DeferredBlock<NightstandBlock>> NIGHTSTANDS = registerNightstands();
    public static final DeferredBlock<NightstandBlock> OAK_NIGHTSTAND = NIGHTSTANDS.get("oak_nightstand");

    public static final DeferredBlock<LoveSeatBlock> LEATHER_LOVE_SEAT =
            BLOCKS.register("leather_love_seat", registryName -> new LoveSeatBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(1.5F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOL)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)
                            .noOcclusion()));

    public static final DeferredBlock<SofaBlock> LEATHER_SOFA =
            BLOCKS.register("leather_sofa", registryName -> new SofaBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(1.5F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOL)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)
                            .noOcclusion()));

    public static final DeferredBlock<ChairBlock> LEATHER_CHAIR =
            BLOCKS.register("leather_chair", registryName -> new ChairBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.COLOR_ORANGE)
                            .strength(1.5F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOL)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)
                            .noOcclusion()));

    public static final java.util.Map<String, DeferredBlock<DiningChairBlock>> DINING_CHAIRS = registerDiningChairs();
    public static final DeferredBlock<DiningChairBlock> OAK_DINING_CHAIR = DINING_CHAIRS.get("oak_dining_chair");

    private static java.util.Map<String, DeferredBlock<Block>> registerBasicBlocks() {
        var blocks = new java.util.LinkedHashMap<String, DeferredBlock<Block>>();
        BASIC_BLOCK_NAMES.forEach(name -> blocks.put(name, BLOCKS.register(name,
                registryName -> createBasicBlock(name, BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, registryName))
                        .mapColor(MapColor.STONE)
                        .strength(1.5F, 6.0F)
                        .requiresCorrectToolForDrops()))));
        return java.util.Collections.unmodifiableMap(blocks);
    }

    private static Block createBasicBlock(String name, BlockBehaviour.Properties properties) {
        if (name.equals("marble_tile")) {
            return new com.digitaldownpour.atouchofclass.blocks.StochasticBlock(properties, 4);
        }
        else if (name.equals("calcite_tile")) {
            return new com.digitaldownpour.atouchofclass.blocks.StochasticBlock(properties, 1);
        }
        return new Block(properties);
    }

    private static DeferredBlock<CabinetBlock> registerCabinet(String name) {
        DeferredBlock<CabinetBlock> cabinet = BLOCKS.register(name, registryName -> new CabinetBlock(
                BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, registryName))
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(2.0F)
                        .sound(net.minecraft.world.level.block.SoundType.WOOD)
                        .noOcclusion()));
        CABINET_REGISTRATIONS.add(cabinet);
        return cabinet;
    }

    private static DeferredBlock<DoorCabinetBlock> registerDoorCabinet(String name) {
        DeferredBlock<DoorCabinetBlock> cabinet = BLOCKS.register(name, registryName -> new DoorCabinetBlock(
                BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, registryName))
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(2.0F)
                        .sound(net.minecraft.world.level.block.SoundType.WOOD)
                        .noOcclusion()));
        CABINET_REGISTRATIONS.add(cabinet);
        return cabinet;
    }

    /** All cabinet variants, used to attach the shared storage block entity automatically. */
    public static java.util.List<DeferredHolder<Block, ? extends CabinetBlock>> cabinetBlocks() {
        return java.util.Collections.unmodifiableList(CABINET_REGISTRATIONS);
    }

    private static java.util.Map<String, DeferredBlock<DresserBlock>> registerDressers() {
        var blocks = new java.util.LinkedHashMap<String, DeferredBlock<DresserBlock>>();
        DresserVariants.IDS.forEach(name -> blocks.put(name, registerDresser(name)));
        return java.util.Collections.unmodifiableMap(blocks);
    }

    private static DeferredBlock<DresserBlock> registerDresser(String name) {
        DeferredBlock<DresserBlock> dresser = BLOCKS.register(name, registryName -> new DresserBlock(
                BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, registryName))
                        .mapColor(MapColor.COLOR_BROWN)
                        .strength(2.0F)
                        .sound(net.minecraft.world.level.block.SoundType.WOOD)
                        .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)
                        .noOcclusion()));
        DRESSER_REGISTRATIONS.add(dresser);
        return dresser;
    }

    // Registration has no world or position: intentionally copy the material's default values.
    @SuppressWarnings("deprecation")
    private static BlockBehaviour.Properties furnitureProperties(Block source, net.minecraft.resources.Identifier registryName) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, registryName))
                .mapColor(source.defaultMapColor())
                .strength(source.defaultDestroyTime(), source.getExplosionResistance())
                .sound(source.defaultBlockState().getSoundType())
                .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)
                .noOcclusion();
        if (source.defaultBlockState().ignitedByLava()) properties.ignitedByLava();
        return properties;
    }

    private static java.util.Map<String, DeferredBlock<DiningChairBlock>> registerDiningChairs() {
        var blocks = new java.util.LinkedHashMap<String, DeferredBlock<DiningChairBlock>>();
        DiningChairVariants.VALUES.forEach(definition -> blocks.put(definition.id(),
                BLOCKS.register(definition.id(), registryName -> {
                    Block source = definition.sourceBlock();
                    BlockBehaviour.Properties properties = furnitureProperties(source, registryName);
                    return new DiningChairBlock(properties);
                })));
        return java.util.Collections.unmodifiableMap(blocks);
    }

    private static java.util.Map<String, DeferredBlock<DiningTableBlock>> registerDiningTables() {
        var blocks = new java.util.LinkedHashMap<String, DeferredBlock<DiningTableBlock>>();
        DiningTableVariants.VALUES.forEach(definition -> blocks.put(definition.id(),
                BLOCKS.register(definition.id(), registryName -> {
                    Block source = definition.sourceBlock();
                    BlockBehaviour.Properties properties = furnitureProperties(source, registryName);
                    return new DiningTableBlock(properties);
                })));
        return java.util.Collections.unmodifiableMap(blocks);
    }

    private static java.util.Map<String, DeferredBlock<SideTableBlock>> registerSideTables() {
        var blocks = new java.util.LinkedHashMap<String, DeferredBlock<SideTableBlock>>();
        SideTableVariants.VALUES.forEach(definition -> blocks.put(definition.id(),
                BLOCKS.register(definition.id(), registryName -> {
                    Block source = definition.sourceBlock();
                    BlockBehaviour.Properties properties = furnitureProperties(source, registryName);
                    return new SideTableBlock(properties);
                })));
        return java.util.Collections.unmodifiableMap(blocks);
    }

    private static java.util.Map<String, DeferredBlock<NightstandBlock>> registerNightstands() {
        var blocks = new java.util.LinkedHashMap<String, DeferredBlock<NightstandBlock>>();
        NightstandVariants.VALUES.forEach(definition -> blocks.put(definition.id(),
                BLOCKS.register(definition.id(), registryName -> {
                    Block source = definition.sourceBlock();
                    BlockBehaviour.Properties properties = furnitureProperties(source, registryName);
                    return new NightstandBlock(properties);
                })));
        return java.util.Collections.unmodifiableMap(blocks);
    }

    /** All nightstand variants, used to attach their shared storage block entity. */
    public static java.util.Collection<DeferredBlock<NightstandBlock>> nightstandBlocks() {
        return NIGHTSTANDS.values();
    }

    /** All dresser variants, used to attach their shared storage block entity. */
    public static java.util.List<DeferredHolder<Block, ? extends DresserBlock>> dresserBlocks() {
        return java.util.Collections.unmodifiableList(DRESSER_REGISTRATIONS);
    }

    public static final java.util.Map<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.FlowerBunchBlock>> FLOWER_BUNCHES = registerFlowerBunches();

    private static java.util.Map<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.FlowerBunchBlock>> registerFlowerBunches() {
        var bunches = new java.util.LinkedHashMap<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.FlowerBunchBlock>>();
        FlowerBunchVariants.VALUES.forEach(definition -> bunches.put(definition.id(),
                BLOCKS.register(definition.id(), key -> new com.digitaldownpour.atouchofclass.blocks.FlowerBunchBlock(definition.height(),
                        BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, key))
                                .mapColor(definition.sourceBlock().defaultMapColor()).instabreak().noCollision().noOcclusion()
                                .sound(net.minecraft.world.level.block.SoundType.GRASS)))));
        return java.util.Collections.unmodifiableMap(bunches);
    }

    public static final java.util.Map<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.WindowBoxBlock>> WINDOW_BOXES = registerWindowBoxes();
    public static final DeferredBlock<com.digitaldownpour.atouchofclass.blocks.WindowBoxBlock> OAK_WINDOW_BOX = WINDOW_BOXES.get("oak_window_box");

    private static java.util.Map<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.WindowBoxBlock>> registerWindowBoxes() {
        var boxes = new java.util.LinkedHashMap<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.WindowBoxBlock>>();
        WindowBoxVariants.VALUES.forEach(definition -> boxes.put(definition.id(),
                BLOCKS.register(definition.id(), key -> new com.digitaldownpour.atouchofclass.blocks.WindowBoxBlock(
                        BlockBehaviour.Properties.ofFullCopy(definition.sourceBlock())
                                .setId(ResourceKey.create(Registries.BLOCK, key)).noOcclusion()))));
        return java.util.Collections.unmodifiableMap(boxes);
    }

    public static final java.util.Map<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.ShutterBlock>> SHUTTERS = registerShutters();
    public static final DeferredBlock<com.digitaldownpour.atouchofclass.blocks.ShutterBlock> OAK_SHUTTER = SHUTTERS.get("oak_shutter");

    private static java.util.Map<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.ShutterBlock>> registerShutters() {
        var shutters = new java.util.LinkedHashMap<String, DeferredBlock<com.digitaldownpour.atouchofclass.blocks.ShutterBlock>>();
        ShutterVariants.VALUES.forEach(definition -> shutters.put(definition.id(),
                BLOCKS.register(definition.id(), key -> new com.digitaldownpour.atouchofclass.blocks.ShutterBlock(
                        BlockBehaviour.Properties.ofFullCopy(definition.sourceBlock())
                                .setId(ResourceKey.create(Registries.BLOCK, key)).noOcclusion()))));
        return java.util.Collections.unmodifiableMap(shutters);
    }

    public static final DeferredBlock<OakRailingBlock> OAK_RAILING =
            BLOCKS.register("oak_railing", key -> new OakRailingBlock(
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, key))
                            .mapColor(MapColor.WOOD).strength(2.0F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<OakBalustradeBlock> OAK_BALUSTRADE =
            BLOCKS.register("oak_balustrade", key -> new OakBalustradeBlock(
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, key))
                            .mapColor(MapColor.WOOD).strength(2.0F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<OakBalusterBlock> OAK_BALUSTER =
            BLOCKS.register("oak_baluster", key -> new OakBalusterBlock(
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, key))
                            .mapColor(MapColor.WOOD).strength(2.0F)
                            .sound(net.minecraft.world.level.block.SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<com.digitaldownpour.atouchofclass.blocks.OakNewelPostBlock> OAK_NEWEL_POST =
            BLOCKS.register("oak_newel_post",key->new com.digitaldownpour.atouchofclass.blocks.OakNewelPostBlock(
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,key))
                            .mapColor(MapColor.WOOD).strength(2.0F).sound(net.minecraft.world.level.block.SoundType.WOOD)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE).noOcclusion()));
    public static final java.util.Map<String, WoodRailingVariants.Family> WOOD_RAILINGS = WoodRailingVariants.register();

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

    public static final DeferredBlock<PlasterColumnBlock> SQUARE_PLASTER_COLUMN =
            registerPlasterColumn("square_plaster_column", PlasterColumnBlock.Variant.SQUARE_COLUMN);
    public static final DeferredBlock<PlasterColumnBlock> SQUARE_PLASTER_COLUMN_BASE =
            registerPlasterColumn("square_plaster_column_base", PlasterColumnBlock.Variant.SQUARE_BASE);
    public static final DeferredBlock<PlasterColumnBlock> SQUARE_PLASTER_COLUMN_CAPITAL =
            registerPlasterColumn("square_plaster_column_capital", PlasterColumnBlock.Variant.SQUARE_CAPITAL);
    public static final DeferredBlock<PlasterColumnBlock> ROUND_PLASTER_COLUMN =
            registerPlasterColumn("round_plaster_column", PlasterColumnBlock.Variant.ROUND_COLUMN);
    public static final DeferredBlock<PlasterColumnBlock> ROUND_PLASTER_COLUMN_BASE =
            registerPlasterColumn("round_plaster_column_base", PlasterColumnBlock.Variant.ROUND_BASE);
    public static final DeferredBlock<PlasterColumnBlock> ROUND_PLASTER_COLUMN_CAPITAL =
            registerPlasterColumn("round_plaster_column_capital", PlasterColumnBlock.Variant.ROUND_CAPITAL);

    private static DeferredBlock<PlasterColumnBlock> registerPlasterColumn(String name,
                                                                          PlasterColumnBlock.Variant variant) {
        return BLOCKS.register(name, registryName -> new PlasterColumnBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, registryName))
                .mapColor(MapColor.SNOW)
                .strength(2.0F)
                .sound(net.minecraft.world.level.block.SoundType.WOOD)
                .noOcclusion(), variant));
    }

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

    public static final DeferredHolder<Block, TallDoorBlock> TALL_MANGROVE_DOOR =
            registerTallDoor("tall_mangrove_door", MapColor.COLOR_RED);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_MANGROVE_SIDELIGHT =
            registerTallSidelight("tall_mangrove_sidelight", MapColor.COLOR_RED);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_BIRCH_DOOR =
            registerTallDoor("tall_birch_door", MapColor.SAND);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_BIRCH_SIDELIGHT =
            registerTallSidelight("tall_birch_sidelight", MapColor.SAND);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_OAK_DOOR =
            registerTallDoor("tall_oak_door", MapColor.WOOD);

    public static final java.util.Map<String, DeferredHolder<Block, TallDoorBlock>> TALL_PATIO_DOORS = registerPatioDoors();
    public static final DeferredHolder<Block, TallDoorBlock> TALL_OAK_PATIO_DOOR = TALL_PATIO_DOORS.get("tall_oak_patio_door");

    private static java.util.Map<String, DeferredHolder<Block, TallDoorBlock>> registerPatioDoors() {
        var doors = new java.util.LinkedHashMap<String, DeferredHolder<Block, TallDoorBlock>>();
        PatioDoorVariants.VALUES.forEach(definition -> doors.put(definition.id(),
                registerTallDoor(definition.id(), definition.sourceBlock().defaultMapColor())));
        return java.util.Collections.unmodifiableMap(doors);
    }

    public static final DeferredHolder<Block, TallDoorBlock> TALL_SPRUCE_DOOR =
            registerTallDoor("tall_spruce_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_JUNGLE_DOOR =
            registerTallDoor("tall_jungle_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_ACACIA_DOOR =
            registerTallDoor("tall_acacia_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_DARK_OAK_DOOR =
            registerTallDoor("tall_dark_oak_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_CHERRY_DOOR =
            registerTallDoor("tall_cherry_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_BAMBOO_DOOR =
            registerTallDoor("tall_bamboo_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_PALE_OAK_DOOR =
            registerTallDoor("tall_pale_oak_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_CRIMSON_DOOR =
            registerTallDoor("tall_crimson_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_WARPED_DOOR =
            registerTallDoor("tall_warped_door", MapColor.WOOD);

    public static final DeferredHolder<Block, TallDoorBlock> TALL_POPLAR_DOOR =
            registerTallDoor("tall_poplar_door", MapColor.WOOD);

    // TALL_DOOR_VARIANT_REGISTRATIONS
    public static final DeferredHolder<Block, TallSidelightBlock> TALL_OAK_SIDELIGHT =
            registerTallSidelight("tall_oak_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_SPRUCE_SIDELIGHT =
            registerTallSidelight("tall_spruce_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_JUNGLE_SIDELIGHT =
            registerTallSidelight("tall_jungle_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_ACACIA_SIDELIGHT =
            registerTallSidelight("tall_acacia_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_DARK_OAK_SIDELIGHT =
            registerTallSidelight("tall_dark_oak_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_CHERRY_SIDELIGHT =
            registerTallSidelight("tall_cherry_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_BAMBOO_SIDELIGHT =
            registerTallSidelight("tall_bamboo_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_PALE_OAK_SIDELIGHT =
            registerTallSidelight("tall_pale_oak_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_CRIMSON_SIDELIGHT =
            registerTallSidelight("tall_crimson_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_WARPED_SIDELIGHT =
            registerTallSidelight("tall_warped_sidelight", MapColor.WOOD);

    public static final DeferredHolder<Block, TallSidelightBlock> TALL_POPLAR_SIDELIGHT =
            registerTallSidelight("tall_poplar_sidelight", MapColor.WOOD);

    // TALL_SIDELIGHT_VARIANT_REGISTRATIONS

    public static final DeferredHolder<Block, GlassShowerSurroundBlock> GLASS_SHOWER_SURROUND =
            BLOCKS.register("glass_shower_surround", registryName -> new GlassShowerSurroundBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.NONE)
                            .strength(0.3F)
                            .sound(net.minecraft.world.level.block.SoundType.GLASS)
                            .noOcclusion()));

    public static final DeferredHolder<Block, GlassShowerDoorBlock> GLASS_SHOWER_DOOR =
            BLOCKS.register("glass_shower_door", registryName -> new GlassShowerDoorBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.NONE)
                            .strength(0.3F)
                            .sound(net.minecraft.world.level.block.SoundType.GLASS)
                            .noOcclusion()));

    public static final DeferredHolder<Block, WallShowerHeadBlock> WALL_SHOWER_HEAD =
            BLOCKS.register("wall_shower_head", registryName -> new WallShowerHeadBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.METAL)
                            .strength(1.5F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
                            .noOcclusion()));

    public static final DeferredHolder<Block, RainShowerHeadBlock> RAIN_SHOWER_HEAD =
            BLOCKS.register("rain_shower_head", registryName -> new RainShowerHeadBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.METAL)
                            .strength(1.5F)
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
                            .noOcclusion()));

    public static final DeferredHolder<Block, KeyCutterBlock> KEY_CUTTER = BLOCKS.register("key_cutter",
            registryName -> new KeyCutterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .noOcclusion()
                    .strength(3.0F)
            )
    );

    public static final DeferredHolder<Block, ToiletBlock> TOILET = BLOCKS.register("toilet",
            registryName -> new ToiletBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.SNOW)
                    .strength(1.5F, 6.0F)
                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ClawfootBathtubBlock> CLAWFOOT_BATHTUB = BLOCKS.register("clawfoot_bathtub",
            registryName -> new ClawfootBathtubBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.SNOW)
                    .strength(1.5F, 6.0F)
                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    public static final java.util.Map<String, DeferredHolder<Block, FourPostBedBlock>> FOUR_POST_BEDS = registerFourPostBeds();
    public static final DeferredHolder<Block, FourPostBedBlock> DARK_OAK_RED_FOUR_POST_BED =
            FOUR_POST_BEDS.get("dark_oak_red_four_post_bed");

    private static java.util.Map<String, DeferredHolder<Block, FourPostBedBlock>> registerFourPostBeds() {
        java.util.Map<String, DeferredHolder<Block, FourPostBedBlock>> beds = new java.util.LinkedHashMap<>();
        for (String id : FourPostBedVariants.IDS) {
            beds.put(id, BLOCKS.register(id, registryName -> new FourPostBedBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .noOcclusion())));
        }
        return java.util.Collections.unmodifiableMap(beds);
    }

    public static final DeferredHolder<Block, RefrigeratorBlock> REFRIGERATOR = BLOCKS.register("refrigerator",
            registryName -> new RefrigeratorBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 6.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.IMMOVEABLE)
                    .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, OvenBlock> OVEN = BLOCKS.register("oven",
            registryName -> new OvenBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(3.5F, 6.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(OvenBlock.LIT) ? 13 : 0)
            )
    );

    public static final DeferredHolder<Block, InductionCooktopBlock> INDUCTION_COOKTOP =
            BLOCKS.register("induction_cooktop",
                    registryName -> new InductionCooktopBlock(BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.GLASS)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                    ));

    public static final DeferredHolder<Block, ElevatorShaftPanelBlock> ELEVATOR_SHAFT_PANEL = BLOCKS.register("elevator_shaft_panel",
            registryName -> new ElevatorShaftPanelBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, ElevatorFloorBlock> ELEVATOR_FLOOR = BLOCKS.register("elevator_floor",
            registryName -> new ElevatorFloorBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .requiresCorrectToolForDrops()
            )
    );

    public static final DeferredHolder<Block, ElevatorDoorBlock> ELEVATOR_DOOR = BLOCKS.register("elevator_door",
            registryName -> new ElevatorDoorBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );

    public static final DeferredHolder<Block, ElevatorCallButtonBlock> ELEVATOR_CALL_BUTTON = BLOCKS.register("elevator_call_button",
            registryName -> new ElevatorCallButtonBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
            )
    );

    public static final DeferredHolder<Block, ElevatorDestinationControlBlock> ELEVATOR_DESTINATION_CONTROL = BLOCKS.register("elevator_destination_control",
            registryName -> new ElevatorDestinationControlBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(2.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
            )
    );

    public static final DeferredHolder<Block, Block> BRUSHED_STAINLESS_STEEL = BLOCKS.register("brushed_stainless_steel",
            registryName -> new Block(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(3.0F)
                    .sound(net.minecraft.world.level.block.SoundType.METAL)
                    .requiresCorrectToolForDrops()
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
                    .mapColor(copperMapColor(age))
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(net.minecraft.world.level.block.SoundType.COPPER)
                    .noOcclusion().lightLevel(state -> 15);
            return waxed ? new WallSconceBlock(properties) : new WeatheringWallSconceBlock(age, properties);
        });
    }
    // Keep legacy copper_chain IDs for saved worlds; the display name is Copper Chandelier Chain.
    public static final DeferredBlock<CopperChandelierChainBlock> COPPER_CHANDELIER_CHAIN = registerCopperChandelierChain("copper_chain", WeatherState.UNAFFECTED, false);
    public static final DeferredBlock<CopperChandelierChainBlock> EXPOSED_COPPER_CHANDELIER_CHAIN = registerCopperChandelierChain("exposed_copper_chain", WeatherState.EXPOSED, false);
    public static final DeferredBlock<CopperChandelierChainBlock> WEATHERED_COPPER_CHANDELIER_CHAIN = registerCopperChandelierChain("weathered_copper_chain", WeatherState.WEATHERED, false);
    public static final DeferredBlock<CopperChandelierChainBlock> OXIDIZED_COPPER_CHANDELIER_CHAIN = registerCopperChandelierChain("oxidized_copper_chain", WeatherState.OXIDIZED, false);
    public static final DeferredBlock<CopperChandelierChainBlock> WAXED_COPPER_CHANDELIER_CHAIN = registerCopperChandelierChain("waxed_copper_chain", WeatherState.UNAFFECTED, true);
    public static final DeferredBlock<CopperChandelierChainBlock> WAXED_EXPOSED_COPPER_CHANDELIER_CHAIN = registerCopperChandelierChain("waxed_exposed_copper_chain", WeatherState.EXPOSED, true);
    public static final DeferredBlock<CopperChandelierChainBlock> WAXED_WEATHERED_COPPER_CHANDELIER_CHAIN = registerCopperChandelierChain("waxed_weathered_copper_chain", WeatherState.WEATHERED, true);
    public static final DeferredBlock<CopperChandelierChainBlock> WAXED_OXIDIZED_COPPER_CHANDELIER_CHAIN = registerCopperChandelierChain("waxed_oxidized_copper_chain", WeatherState.OXIDIZED, true);

    private static DeferredBlock<CopperChandelierChainBlock> registerCopperChandelierChain(String name, WeatherState age, boolean waxed) {
        return BLOCKS.register(name, key -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, key))
                    .mapColor(copperMapColor(age))
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(net.minecraft.world.level.block.SoundType.COPPER).noOcclusion();
            return waxed ? new CopperChandelierChainBlock(properties) : new WeatheringCopperChandelierChainBlock(age, properties);
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
                    .mapColor(copperMapColor(age))
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(net.minecraft.world.level.block.SoundType.COPPER).noOcclusion().lightLevel(state -> 15);
            return waxed ? new CopperChandelierBlock(properties) : new WeatheringCopperChandelierBlock(age, properties);
        });
    }

    public static final DeferredBlock<CopperLampPostBlock> COPPER_LAMP_POST = registerCopperLampPost("copper_lamp_post", WeatherState.UNAFFECTED, false);
    public static final DeferredBlock<CopperLampPostBlock> EXPOSED_COPPER_LAMP_POST = registerCopperLampPost("exposed_copper_lamp_post", WeatherState.EXPOSED, false);
    public static final DeferredBlock<CopperLampPostBlock> WEATHERED_COPPER_LAMP_POST = registerCopperLampPost("weathered_copper_lamp_post", WeatherState.WEATHERED, false);
    public static final DeferredBlock<CopperLampPostBlock> OXIDIZED_COPPER_LAMP_POST = registerCopperLampPost("oxidized_copper_lamp_post", WeatherState.OXIDIZED, false);
    public static final DeferredBlock<CopperLampPostBlock> WAXED_COPPER_LAMP_POST = registerCopperLampPost("waxed_copper_lamp_post", WeatherState.UNAFFECTED, true);
    public static final DeferredBlock<CopperLampPostBlock> WAXED_EXPOSED_COPPER_LAMP_POST = registerCopperLampPost("waxed_exposed_copper_lamp_post", WeatherState.EXPOSED, true);
    public static final DeferredBlock<CopperLampPostBlock> WAXED_WEATHERED_COPPER_LAMP_POST = registerCopperLampPost("waxed_weathered_copper_lamp_post", WeatherState.WEATHERED, true);
    public static final DeferredBlock<CopperLampPostBlock> WAXED_OXIDIZED_COPPER_LAMP_POST = registerCopperLampPost("waxed_oxidized_copper_lamp_post", WeatherState.OXIDIZED, true);

    private static DeferredBlock<CopperLampPostBlock> registerCopperLampPost(String name, WeatherState age, boolean waxed) {
        return BLOCKS.register(name, key -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, key))
                    .mapColor(copperMapColor(age))
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(net.minecraft.world.level.block.SoundType.COPPER)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(CopperLampPostBlock.PART) == 2 ? 15 : 0);
            return waxed ? new CopperLampPostBlock(properties) : new WeatheringCopperLampPostBlock(age, properties);
        });
    }

    public static final DeferredBlock<CopperFloorLampBlock> COPPER_FLOOR_LAMP = registerCopperFloorLamp("copper_floor_lamp", WeatherState.UNAFFECTED, false);
    public static final DeferredBlock<CopperFloorLampBlock> EXPOSED_COPPER_FLOOR_LAMP = registerCopperFloorLamp("exposed_copper_floor_lamp", WeatherState.EXPOSED, false);
    public static final DeferredBlock<CopperFloorLampBlock> WEATHERED_COPPER_FLOOR_LAMP = registerCopperFloorLamp("weathered_copper_floor_lamp", WeatherState.WEATHERED, false);
    public static final DeferredBlock<CopperFloorLampBlock> OXIDIZED_COPPER_FLOOR_LAMP = registerCopperFloorLamp("oxidized_copper_floor_lamp", WeatherState.OXIDIZED, false);
    public static final DeferredBlock<CopperFloorLampBlock> WAXED_COPPER_FLOOR_LAMP = registerCopperFloorLamp("waxed_copper_floor_lamp", WeatherState.UNAFFECTED, true);
    public static final DeferredBlock<CopperFloorLampBlock> WAXED_EXPOSED_COPPER_FLOOR_LAMP = registerCopperFloorLamp("waxed_exposed_copper_floor_lamp", WeatherState.EXPOSED, true);
    public static final DeferredBlock<CopperFloorLampBlock> WAXED_WEATHERED_COPPER_FLOOR_LAMP = registerCopperFloorLamp("waxed_weathered_copper_floor_lamp", WeatherState.WEATHERED, true);
    public static final DeferredBlock<CopperFloorLampBlock> WAXED_OXIDIZED_COPPER_FLOOR_LAMP = registerCopperFloorLamp("waxed_oxidized_copper_floor_lamp", WeatherState.OXIDIZED, true);

    private static DeferredBlock<CopperFloorLampBlock> registerCopperFloorLamp(String name, WeatherState age, boolean waxed) {
        return BLOCKS.register(name, key -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, key))
                    .mapColor(copperMapColor(age))
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(net.minecraft.world.level.block.SoundType.COPPER)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(CopperFloorLampBlock.PART) == 2 ? 15 : 0);
            return waxed ? new CopperFloorLampBlock(properties) : new WeatheringCopperFloorLampBlock(age, properties);
        });
    }

    public static final DeferredBlock<CopperCeilingLightBlock> COPPER_BOWL_CEILING_LIGHT = registerCopperCeilingLight("copper_bowl_ceiling_light", WeatherState.UNAFFECTED, false, false);
    public static final DeferredBlock<CopperCeilingLightBlock> EXPOSED_COPPER_BOWL_CEILING_LIGHT = registerCopperCeilingLight("exposed_copper_bowl_ceiling_light", WeatherState.EXPOSED, false, false);
    public static final DeferredBlock<CopperCeilingLightBlock> WEATHERED_COPPER_BOWL_CEILING_LIGHT = registerCopperCeilingLight("weathered_copper_bowl_ceiling_light", WeatherState.WEATHERED, false, false);
    public static final DeferredBlock<CopperCeilingLightBlock> OXIDIZED_COPPER_BOWL_CEILING_LIGHT = registerCopperCeilingLight("oxidized_copper_bowl_ceiling_light", WeatherState.OXIDIZED, false, false);
    public static final DeferredBlock<CopperCeilingLightBlock> WAXED_COPPER_BOWL_CEILING_LIGHT = registerCopperCeilingLight("waxed_copper_bowl_ceiling_light", WeatherState.UNAFFECTED, true, false);
    public static final DeferredBlock<CopperCeilingLightBlock> WAXED_EXPOSED_COPPER_BOWL_CEILING_LIGHT = registerCopperCeilingLight("waxed_exposed_copper_bowl_ceiling_light", WeatherState.EXPOSED, true, false);
    public static final DeferredBlock<CopperCeilingLightBlock> WAXED_WEATHERED_COPPER_BOWL_CEILING_LIGHT = registerCopperCeilingLight("waxed_weathered_copper_bowl_ceiling_light", WeatherState.WEATHERED, true, false);
    public static final DeferredBlock<CopperCeilingLightBlock> WAXED_OXIDIZED_COPPER_BOWL_CEILING_LIGHT = registerCopperCeilingLight("waxed_oxidized_copper_bowl_ceiling_light", WeatherState.OXIDIZED, true, false);

    public static final DeferredBlock<CopperCeilingLightBlock> COPPER_COFFERED_CEILING_LIGHT = registerCopperCeilingLight("copper_coffered_ceiling_light", WeatherState.UNAFFECTED, false, true);
    public static final DeferredBlock<CopperCeilingLightBlock> EXPOSED_COPPER_COFFERED_CEILING_LIGHT = registerCopperCeilingLight("exposed_copper_coffered_ceiling_light", WeatherState.EXPOSED, false, true);
    public static final DeferredBlock<CopperCeilingLightBlock> WEATHERED_COPPER_COFFERED_CEILING_LIGHT = registerCopperCeilingLight("weathered_copper_coffered_ceiling_light", WeatherState.WEATHERED, false, true);
    public static final DeferredBlock<CopperCeilingLightBlock> OXIDIZED_COPPER_COFFERED_CEILING_LIGHT = registerCopperCeilingLight("oxidized_copper_coffered_ceiling_light", WeatherState.OXIDIZED, false, true);
    public static final DeferredBlock<CopperCeilingLightBlock> WAXED_COPPER_COFFERED_CEILING_LIGHT = registerCopperCeilingLight("waxed_copper_coffered_ceiling_light", WeatherState.UNAFFECTED, true, true);
    public static final DeferredBlock<CopperCeilingLightBlock> WAXED_EXPOSED_COPPER_COFFERED_CEILING_LIGHT = registerCopperCeilingLight("waxed_exposed_copper_coffered_ceiling_light", WeatherState.EXPOSED, true, true);
    public static final DeferredBlock<CopperCeilingLightBlock> WAXED_WEATHERED_COPPER_COFFERED_CEILING_LIGHT = registerCopperCeilingLight("waxed_weathered_copper_coffered_ceiling_light", WeatherState.WEATHERED, true, true);
    public static final DeferredBlock<CopperCeilingLightBlock> WAXED_OXIDIZED_COPPER_COFFERED_CEILING_LIGHT = registerCopperCeilingLight("waxed_oxidized_copper_coffered_ceiling_light", WeatherState.OXIDIZED, true, true);

    private static DeferredBlock<CopperCeilingLightBlock> registerCopperCeilingLight(
            String name, WeatherState age, boolean waxed, boolean coffered) {
        return BLOCKS.register(name, key -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, key))
                    .mapColor(copperMapColor(age))
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(net.minecraft.world.level.block.SoundType.COPPER)
                    .noOcclusion().lightLevel(state -> 15);
            return waxed ? new CopperCeilingLightBlock(coffered, properties)
                    : new WeatheringCopperCeilingLightBlock(age, coffered, properties);
        });
    }

    public static final DeferredBlock<CopperTableLampBlock> COPPER_TABLE_LAMP = registerCopperTableLamp("copper_table_lamp", WeatherState.UNAFFECTED, false);
    public static final DeferredBlock<CopperTableLampBlock> EXPOSED_COPPER_TABLE_LAMP = registerCopperTableLamp("exposed_copper_table_lamp", WeatherState.EXPOSED, false);
    public static final DeferredBlock<CopperTableLampBlock> WEATHERED_COPPER_TABLE_LAMP = registerCopperTableLamp("weathered_copper_table_lamp", WeatherState.WEATHERED, false);
    public static final DeferredBlock<CopperTableLampBlock> OXIDIZED_COPPER_TABLE_LAMP = registerCopperTableLamp("oxidized_copper_table_lamp", WeatherState.OXIDIZED, false);
    public static final DeferredBlock<CopperTableLampBlock> WAXED_COPPER_TABLE_LAMP = registerCopperTableLamp("waxed_copper_table_lamp", WeatherState.UNAFFECTED, true);
    public static final DeferredBlock<CopperTableLampBlock> WAXED_EXPOSED_COPPER_TABLE_LAMP = registerCopperTableLamp("waxed_exposed_copper_table_lamp", WeatherState.EXPOSED, true);
    public static final DeferredBlock<CopperTableLampBlock> WAXED_WEATHERED_COPPER_TABLE_LAMP = registerCopperTableLamp("waxed_weathered_copper_table_lamp", WeatherState.WEATHERED, true);
    public static final DeferredBlock<CopperTableLampBlock> WAXED_OXIDIZED_COPPER_TABLE_LAMP = registerCopperTableLamp("waxed_oxidized_copper_table_lamp", WeatherState.OXIDIZED, true);

    private static DeferredBlock<CopperTableLampBlock> registerCopperTableLamp(String name, WeatherState age, boolean waxed) {
        return BLOCKS.register(name, key -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, key))
                    .mapColor(copperMapColor(age))
                    .strength(3.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(net.minecraft.world.level.block.SoundType.COPPER)
                    .noOcclusion().lightLevel(state -> 15);
            return waxed ? new CopperTableLampBlock(properties)
                    : new WeatheringCopperTableLampBlock(age, properties);
        });
    }

    private static MapColor copperMapColor(WeatherState age) {
        return switch (age) {
            case UNAFFECTED -> Blocks.CUT_COPPER.weathering().unaffected().defaultMapColor();
            case EXPOSED -> Blocks.CUT_COPPER.weathering().exposed().defaultMapColor();
            case WEATHERED -> Blocks.CUT_COPPER.weathering().weathered().defaultMapColor();
            case OXIDIZED -> Blocks.CUT_COPPER.weathering().oxidized().defaultMapColor();
        };
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static DeferredHolder<Block, TallDoorBlock> registerTallDoor(String name, MapColor mapColor) {
        return BLOCKS.register(name, registryName -> new TallDoorBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, registryName))
                .mapColor(mapColor)
                .strength(3.0F)
                .noOcclusion()));
    }

    private static DeferredHolder<Block, TallSidelightBlock> registerTallSidelight(String name, MapColor mapColor) {
        return BLOCKS.register(name, registryName -> new TallSidelightBlock(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, registryName))
                .mapColor(mapColor)
                .strength(3.0F)
                .noOcclusion()));
    }

    @SuppressWarnings("EmptyMethod") // Reserved callback; creative contents come from ModCreativeTabs.
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
}
