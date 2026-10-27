package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.items.DoorKeyItem;
import com.digitaldownpour.atouchofclass.items.KeyRingItem;
import com.digitaldownpour.atouchofclass.items.BucketOfPlasterItem;
import com.digitaldownpour.atouchofclass.items.TallDoorItem;
import com.digitaldownpour.atouchofclass.items.WallFinishItem;
import com.digitaldownpour.atouchofclass.items.WallpaperItem;
import com.digitaldownpour.atouchofclass.items.WainscotingItem;
import com.digitaldownpour.atouchofclass.items.StoneQuoinItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ATouchofClass.MODID);

    public static final java.util.List<DeferredItem<BlockItem>> BASIC_BLOCK_ITEMS = registerBasicBlockItems();

    public static final java.util.Map<String, DeferredItem<com.digitaldownpour.atouchofclass.items.FlowerBunchItem>> FLOWER_BUNCH_ITEMS = registerFlowerBunchItems();

    private static java.util.Map<String, DeferredItem<com.digitaldownpour.atouchofclass.items.FlowerBunchItem>> registerFlowerBunchItems() {
        var items = new java.util.LinkedHashMap<String, DeferredItem<com.digitaldownpour.atouchofclass.items.FlowerBunchItem>>();
        FlowerBunchVariants.VALUES.forEach(definition -> items.put(definition.id(),
                ITEMS.register(definition.id(), key -> new com.digitaldownpour.atouchofclass.items.FlowerBunchItem(
                        ModBlocks.FLOWER_BUNCHES.get(definition.id()).get(), new Item.Properties()
                                .setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix()))));
        return java.util.Collections.unmodifiableMap(items);
    }

    public static final java.util.Map<String, DeferredItem<BlockItem>> WINDOW_BOX_ITEMS = registerWindowBoxItems();
    public static final DeferredItem<BlockItem> OAK_WINDOW_BOX = WINDOW_BOX_ITEMS.get("oak_window_box");

    private static java.util.Map<String, DeferredItem<BlockItem>> registerWindowBoxItems() {
        var items = new java.util.LinkedHashMap<String, DeferredItem<BlockItem>>();
        WindowBoxVariants.VALUES.forEach(definition -> items.put(definition.id(),
                ITEMS.registerSimpleBlockItem(definition.id(), ModBlocks.WINDOW_BOXES.get(definition.id()))));
        return java.util.Collections.unmodifiableMap(items);
    }

    public static final java.util.Map<String, DeferredItem<BlockItem>> SHUTTER_ITEMS = registerShutterItems();
    public static final DeferredItem<BlockItem> OAK_SHUTTER = SHUTTER_ITEMS.get("oak_shutter");

    private static java.util.Map<String, DeferredItem<BlockItem>> registerShutterItems() {
        var items = new java.util.LinkedHashMap<String, DeferredItem<BlockItem>>();
        ShutterVariants.VALUES.forEach(definition -> items.put(definition.id(),
                ITEMS.registerSimpleBlockItem(definition.id(), ModBlocks.SHUTTERS.get(definition.id()))));
        return java.util.Collections.unmodifiableMap(items);
    }

    public static final DeferredItem<BlockItem> CALCITE_SLAB =
            ITEMS.registerSimpleBlockItem("calcite_slab", ModBlocks.CALCITE_SLAB);
    public static final DeferredItem<BlockItem> BRICK_WINDOW_LEDGE =
            ITEMS.registerSimpleBlockItem("brick_window_ledge", ModBlocks.BRICK_WINDOW_LEDGE);
    public static final DeferredItem<BlockItem> BUFF_BRICK_WINDOW_LEDGE =
            ITEMS.registerSimpleBlockItem("buff_brick_window_ledge", ModBlocks.BUFF_BRICK_WINDOW_LEDGE);

    public static final DeferredItem<BlockItem> BUFF_BRICKS =
            ITEMS.registerSimpleBlockItem("buff_bricks", ModBlocks.BUFF_BRICKS);
    public static final DeferredItem<BlockItem> BUFF_BRICK_SLAB =
            ITEMS.registerSimpleBlockItem("buff_brick_slab", ModBlocks.BUFF_BRICK_SLAB);
    public static final DeferredItem<BlockItem> BUFF_BRICK_STAIRS =
            ITEMS.registerSimpleBlockItem("buff_brick_stairs", ModBlocks.BUFF_BRICK_STAIRS);
    public static final DeferredItem<BlockItem> BUFF_BRICK_WALL =
            ITEMS.registerSimpleBlockItem("buff_brick_wall", ModBlocks.BUFF_BRICK_WALL);
    public static final DeferredItem<BlockItem> CALCITE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("calcite_countertop", ModBlocks.CALCITE_COUNTERTOP);
    public static final DeferredItem<BlockItem> CALCITE_SINK =
            ITEMS.registerSimpleBlockItem("calcite_sink", ModBlocks.CALCITE_SINK);
    public static final DeferredItem<BlockItem> KITCHEN_SINK =
            ITEMS.registerSimpleBlockItem("kitchen_sink", ModBlocks.KITCHEN_SINK);
    public static final DeferredItem<BlockItem> DARK_OAK_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("dark_oak_door_cabinet", ModBlocks.DARK_OAK_DOOR_CABINET);
    public static final DeferredItem<BlockItem> DARK_OAK_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("dark_oak_drawer_cabinet", ModBlocks.DARK_OAK_DRAWER_CABINET);

    public static final DeferredItem<BlockItem> SMOOTH_STONE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("smooth_stone_countertop", ModBlocks.SMOOTH_STONE_COUNTERTOP);
    public static final DeferredItem<BlockItem> SMOOTH_STONE_SINK =
            ITEMS.registerSimpleBlockItem("smooth_stone_sink", ModBlocks.SMOOTH_STONE_SINK);
    public static final DeferredItem<BlockItem> PALE_OAK_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("pale_oak_door_cabinet", ModBlocks.PALE_OAK_DOOR_CABINET);
    public static final DeferredItem<BlockItem> PALE_OAK_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("pale_oak_drawer_cabinet", ModBlocks.PALE_OAK_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> STONE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("stone_countertop", ModBlocks.STONE_COUNTERTOP);
    public static final DeferredItem<BlockItem> STONE_SINK =
            ITEMS.registerSimpleBlockItem("stone_sink", ModBlocks.STONE_SINK);
    public static final DeferredItem<BlockItem> STONE_BRICK_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("stone_brick_countertop", ModBlocks.STONE_BRICK_COUNTERTOP);
    public static final DeferredItem<BlockItem> STONE_BRICK_SINK =
            ITEMS.registerSimpleBlockItem("stone_brick_sink", ModBlocks.STONE_BRICK_SINK);
    public static final DeferredItem<BlockItem> NETHER_BRICK_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("nether_brick_countertop", ModBlocks.NETHER_BRICK_COUNTERTOP);
    public static final DeferredItem<BlockItem> NETHER_BRICK_SINK =
            ITEMS.registerSimpleBlockItem("nether_brick_sink", ModBlocks.NETHER_BRICK_SINK);
    public static final DeferredItem<BlockItem> QUARTZ_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("quartz_countertop", ModBlocks.QUARTZ_COUNTERTOP);
    public static final DeferredItem<BlockItem> QUARTZ_SINK =
            ITEMS.registerSimpleBlockItem("quartz_sink", ModBlocks.QUARTZ_SINK);
    public static final DeferredItem<BlockItem> POLISHED_GRANITE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("polished_granite_countertop", ModBlocks.POLISHED_GRANITE_COUNTERTOP);
    public static final DeferredItem<BlockItem> POLISHED_GRANITE_SINK =
            ITEMS.registerSimpleBlockItem("polished_granite_sink", ModBlocks.POLISHED_GRANITE_SINK);
    public static final DeferredItem<BlockItem> POLISHED_DIORITE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("polished_diorite_countertop", ModBlocks.POLISHED_DIORITE_COUNTERTOP);
    public static final DeferredItem<BlockItem> POLISHED_DIORITE_SINK =
            ITEMS.registerSimpleBlockItem("polished_diorite_sink", ModBlocks.POLISHED_DIORITE_SINK);
    public static final DeferredItem<BlockItem> END_STONE_BRICK_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("end_stone_brick_countertop", ModBlocks.END_STONE_BRICK_COUNTERTOP);
    public static final DeferredItem<BlockItem> END_STONE_BRICK_SINK =
            ITEMS.registerSimpleBlockItem("end_stone_brick_sink", ModBlocks.END_STONE_BRICK_SINK);
    public static final DeferredItem<BlockItem> SMOOTH_QUARTZ_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("smooth_quartz_countertop", ModBlocks.SMOOTH_QUARTZ_COUNTERTOP);
    public static final DeferredItem<BlockItem> SMOOTH_QUARTZ_SINK =
            ITEMS.registerSimpleBlockItem("smooth_quartz_sink", ModBlocks.SMOOTH_QUARTZ_SINK);
    public static final DeferredItem<BlockItem> GRANITE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("granite_countertop", ModBlocks.GRANITE_COUNTERTOP);
    public static final DeferredItem<BlockItem> GRANITE_SINK =
            ITEMS.registerSimpleBlockItem("granite_sink", ModBlocks.GRANITE_SINK);
    public static final DeferredItem<BlockItem> ANDESITE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("andesite_countertop", ModBlocks.ANDESITE_COUNTERTOP);
    public static final DeferredItem<BlockItem> ANDESITE_SINK =
            ITEMS.registerSimpleBlockItem("andesite_sink", ModBlocks.ANDESITE_SINK);
    public static final DeferredItem<BlockItem> RED_NETHER_BRICK_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("red_nether_brick_countertop", ModBlocks.RED_NETHER_BRICK_COUNTERTOP);
    public static final DeferredItem<BlockItem> RED_NETHER_BRICK_SINK =
            ITEMS.registerSimpleBlockItem("red_nether_brick_sink", ModBlocks.RED_NETHER_BRICK_SINK);
    public static final DeferredItem<BlockItem> POLISHED_ANDESITE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("polished_andesite_countertop", ModBlocks.POLISHED_ANDESITE_COUNTERTOP);
    public static final DeferredItem<BlockItem> POLISHED_ANDESITE_SINK =
            ITEMS.registerSimpleBlockItem("polished_andesite_sink", ModBlocks.POLISHED_ANDESITE_SINK);
    public static final DeferredItem<BlockItem> DIORITE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("diorite_countertop", ModBlocks.DIORITE_COUNTERTOP);
    public static final DeferredItem<BlockItem> DIORITE_SINK =
            ITEMS.registerSimpleBlockItem("diorite_sink", ModBlocks.DIORITE_SINK);
    public static final DeferredItem<BlockItem> POLISHED_BLACKSTONE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("polished_blackstone_countertop", ModBlocks.POLISHED_BLACKSTONE_COUNTERTOP);
    public static final DeferredItem<BlockItem> POLISHED_BLACKSTONE_SINK =
            ITEMS.registerSimpleBlockItem("polished_blackstone_sink", ModBlocks.POLISHED_BLACKSTONE_SINK);
    public static final DeferredItem<BlockItem> POLISHED_BLACKSTONE_BRICK_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("polished_blackstone_brick_countertop", ModBlocks.POLISHED_BLACKSTONE_BRICK_COUNTERTOP);
    public static final DeferredItem<BlockItem> POLISHED_BLACKSTONE_BRICK_SINK =
            ITEMS.registerSimpleBlockItem("polished_blackstone_brick_sink", ModBlocks.POLISHED_BLACKSTONE_BRICK_SINK);
    public static final DeferredItem<BlockItem> POLISHED_DEEPSLATE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("polished_deepslate_countertop", ModBlocks.POLISHED_DEEPSLATE_COUNTERTOP);
    public static final DeferredItem<BlockItem> POLISHED_DEEPSLATE_SINK =
            ITEMS.registerSimpleBlockItem("polished_deepslate_sink", ModBlocks.POLISHED_DEEPSLATE_SINK);
    public static final DeferredItem<BlockItem> DEEPSLATE_BRICK_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("deepslate_brick_countertop", ModBlocks.DEEPSLATE_BRICK_COUNTERTOP);
    public static final DeferredItem<BlockItem> DEEPSLATE_BRICK_SINK =
            ITEMS.registerSimpleBlockItem("deepslate_brick_sink", ModBlocks.DEEPSLATE_BRICK_SINK);
    public static final DeferredItem<BlockItem> DEEPSLATE_TILE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("deepslate_tile_countertop", ModBlocks.DEEPSLATE_TILE_COUNTERTOP);
    public static final DeferredItem<BlockItem> DEEPSLATE_TILE_SINK =
            ITEMS.registerSimpleBlockItem("deepslate_tile_sink", ModBlocks.DEEPSLATE_TILE_SINK);
    public static final DeferredItem<BlockItem> RESIN_BRICK_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("resin_brick_countertop", ModBlocks.RESIN_BRICK_COUNTERTOP);
    public static final DeferredItem<BlockItem> RESIN_BRICK_SINK =
            ITEMS.registerSimpleBlockItem("resin_brick_sink", ModBlocks.RESIN_BRICK_SINK);
    public static final DeferredItem<BlockItem> PRISMARINE_BRICK_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("prismarine_brick_countertop", ModBlocks.PRISMARINE_BRICK_COUNTERTOP);
    public static final DeferredItem<BlockItem> PRISMARINE_BRICK_SINK =
            ITEMS.registerSimpleBlockItem("prismarine_brick_sink", ModBlocks.PRISMARINE_BRICK_SINK);
    public static final DeferredItem<BlockItem> DARK_PRISMARINE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("dark_prismarine_countertop", ModBlocks.DARK_PRISMARINE_COUNTERTOP);
    public static final DeferredItem<BlockItem> DARK_PRISMARINE_SINK =
            ITEMS.registerSimpleBlockItem("dark_prismarine_sink", ModBlocks.DARK_PRISMARINE_SINK);
    public static final DeferredItem<BlockItem> PURPUR_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("purpur_countertop", ModBlocks.PURPUR_COUNTERTOP);
    public static final DeferredItem<BlockItem> PURPUR_SINK =
            ITEMS.registerSimpleBlockItem("purpur_sink", ModBlocks.PURPUR_SINK);
    public static final DeferredItem<BlockItem> WHITE_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("white_concrete_countertop", ModBlocks.WHITE_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> WHITE_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("white_concrete_sink", ModBlocks.WHITE_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> LIGHT_GRAY_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("light_gray_concrete_countertop", ModBlocks.LIGHT_GRAY_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> LIGHT_GRAY_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("light_gray_concrete_sink", ModBlocks.LIGHT_GRAY_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> GRAY_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("gray_concrete_countertop", ModBlocks.GRAY_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> GRAY_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("gray_concrete_sink", ModBlocks.GRAY_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> BLACK_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("black_concrete_countertop", ModBlocks.BLACK_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> BLACK_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("black_concrete_sink", ModBlocks.BLACK_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> BROWN_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("brown_concrete_countertop", ModBlocks.BROWN_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> BROWN_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("brown_concrete_sink", ModBlocks.BROWN_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> RED_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("red_concrete_countertop", ModBlocks.RED_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> RED_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("red_concrete_sink", ModBlocks.RED_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> ORANGE_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("orange_concrete_countertop", ModBlocks.ORANGE_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> ORANGE_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("orange_concrete_sink", ModBlocks.ORANGE_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> YELLOW_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("yellow_concrete_countertop", ModBlocks.YELLOW_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> YELLOW_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("yellow_concrete_sink", ModBlocks.YELLOW_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> LIME_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("lime_concrete_countertop", ModBlocks.LIME_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> LIME_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("lime_concrete_sink", ModBlocks.LIME_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> GREEN_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("green_concrete_countertop", ModBlocks.GREEN_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> GREEN_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("green_concrete_sink", ModBlocks.GREEN_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> CYAN_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("cyan_concrete_countertop", ModBlocks.CYAN_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> CYAN_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("cyan_concrete_sink", ModBlocks.CYAN_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> LIGHT_BLUE_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("light_blue_concrete_countertop", ModBlocks.LIGHT_BLUE_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> LIGHT_BLUE_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("light_blue_concrete_sink", ModBlocks.LIGHT_BLUE_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> BLUE_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("blue_concrete_countertop", ModBlocks.BLUE_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> BLUE_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("blue_concrete_sink", ModBlocks.BLUE_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> PURPLE_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("purple_concrete_countertop", ModBlocks.PURPLE_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> PURPLE_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("purple_concrete_sink", ModBlocks.PURPLE_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> MAGENTA_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("magenta_concrete_countertop", ModBlocks.MAGENTA_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> MAGENTA_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("magenta_concrete_sink", ModBlocks.MAGENTA_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> PINK_CONCRETE_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("pink_concrete_countertop", ModBlocks.PINK_CONCRETE_COUNTERTOP);
    public static final DeferredItem<BlockItem> PINK_CONCRETE_SINK =
            ITEMS.registerSimpleBlockItem("pink_concrete_sink", ModBlocks.PINK_CONCRETE_SINK);
    public static final DeferredItem<BlockItem> CUT_COPPER_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("cut_copper_countertop", ModBlocks.CUT_COPPER_COUNTERTOP);
    public static final DeferredItem<BlockItem> CUT_COPPER_SINK =
            ITEMS.registerSimpleBlockItem("cut_copper_sink", ModBlocks.CUT_COPPER_SINK);
    public static final DeferredItem<BlockItem> EXPOSED_CUT_COPPER_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("exposed_cut_copper_countertop", ModBlocks.EXPOSED_CUT_COPPER_COUNTERTOP);
    public static final DeferredItem<BlockItem> EXPOSED_CUT_COPPER_SINK =
            ITEMS.registerSimpleBlockItem("exposed_cut_copper_sink", ModBlocks.EXPOSED_CUT_COPPER_SINK);
    public static final DeferredItem<BlockItem> WEATHERED_CUT_COPPER_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("weathered_cut_copper_countertop", ModBlocks.WEATHERED_CUT_COPPER_COUNTERTOP);
    public static final DeferredItem<BlockItem> WEATHERED_CUT_COPPER_SINK =
            ITEMS.registerSimpleBlockItem("weathered_cut_copper_sink", ModBlocks.WEATHERED_CUT_COPPER_SINK);
    public static final DeferredItem<BlockItem> OXIDIZED_CUT_COPPER_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("oxidized_cut_copper_countertop", ModBlocks.OXIDIZED_CUT_COPPER_COUNTERTOP);
    public static final DeferredItem<BlockItem> OXIDIZED_CUT_COPPER_SINK =
            ITEMS.registerSimpleBlockItem("oxidized_cut_copper_sink", ModBlocks.OXIDIZED_CUT_COPPER_SINK);
    public static final DeferredItem<BlockItem> WAXED_CUT_COPPER_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("waxed_cut_copper_countertop", ModBlocks.WAXED_CUT_COPPER_COUNTERTOP);
    public static final DeferredItem<BlockItem> WAXED_CUT_COPPER_SINK =
            ITEMS.registerSimpleBlockItem("waxed_cut_copper_sink", ModBlocks.WAXED_CUT_COPPER_SINK);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_CUT_COPPER_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("waxed_exposed_cut_copper_countertop", ModBlocks.WAXED_EXPOSED_CUT_COPPER_COUNTERTOP);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_CUT_COPPER_SINK =
            ITEMS.registerSimpleBlockItem("waxed_exposed_cut_copper_sink", ModBlocks.WAXED_EXPOSED_CUT_COPPER_SINK);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_CUT_COPPER_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("waxed_weathered_cut_copper_countertop", ModBlocks.WAXED_WEATHERED_CUT_COPPER_COUNTERTOP);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_CUT_COPPER_SINK =
            ITEMS.registerSimpleBlockItem("waxed_weathered_cut_copper_sink", ModBlocks.WAXED_WEATHERED_CUT_COPPER_SINK);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_CUT_COPPER_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("waxed_oxidized_cut_copper_countertop", ModBlocks.WAXED_OXIDIZED_CUT_COPPER_COUNTERTOP);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_CUT_COPPER_SINK =
            ITEMS.registerSimpleBlockItem("waxed_oxidized_cut_copper_sink", ModBlocks.WAXED_OXIDIZED_CUT_COPPER_SINK);
    public static final DeferredItem<BlockItem> OAK_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("oak_door_cabinet", ModBlocks.OAK_DOOR_CABINET);
    public static final DeferredItem<BlockItem> OAK_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("oak_drawer_cabinet", ModBlocks.OAK_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> SPRUCE_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("spruce_door_cabinet", ModBlocks.SPRUCE_DOOR_CABINET);
    public static final DeferredItem<BlockItem> SPRUCE_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("spruce_drawer_cabinet", ModBlocks.SPRUCE_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> BIRCH_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("birch_door_cabinet", ModBlocks.BIRCH_DOOR_CABINET);

    public static final DeferredItem<BlockItem> POPLAR_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("poplar_door_cabinet", ModBlocks.POPLAR_DOOR_CABINET);
    public static final DeferredItem<BlockItem> POPLAR_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("poplar_drawer_cabinet", ModBlocks.POPLAR_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> BIRCH_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("birch_drawer_cabinet", ModBlocks.BIRCH_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> JUNGLE_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("jungle_door_cabinet", ModBlocks.JUNGLE_DOOR_CABINET);
    public static final DeferredItem<BlockItem> JUNGLE_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("jungle_drawer_cabinet", ModBlocks.JUNGLE_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> ACACIA_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("acacia_door_cabinet", ModBlocks.ACACIA_DOOR_CABINET);
    public static final DeferredItem<BlockItem> ACACIA_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("acacia_drawer_cabinet", ModBlocks.ACACIA_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> MANGROVE_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("mangrove_door_cabinet", ModBlocks.MANGROVE_DOOR_CABINET);
    public static final DeferredItem<BlockItem> MANGROVE_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("mangrove_drawer_cabinet", ModBlocks.MANGROVE_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> CHERRY_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("cherry_door_cabinet", ModBlocks.CHERRY_DOOR_CABINET);
    public static final DeferredItem<BlockItem> CHERRY_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("cherry_drawer_cabinet", ModBlocks.CHERRY_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> BAMBOO_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("bamboo_door_cabinet", ModBlocks.BAMBOO_DOOR_CABINET);
    public static final DeferredItem<BlockItem> BAMBOO_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("bamboo_drawer_cabinet", ModBlocks.BAMBOO_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> CRIMSON_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("crimson_door_cabinet", ModBlocks.CRIMSON_DOOR_CABINET);
    public static final DeferredItem<BlockItem> CRIMSON_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("crimson_drawer_cabinet", ModBlocks.CRIMSON_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> WARPED_DOOR_CABINET =
            ITEMS.registerSimpleBlockItem("warped_door_cabinet", ModBlocks.WARPED_DOOR_CABINET);
    public static final DeferredItem<BlockItem> WARPED_DRAWER_CABINET =
            ITEMS.registerSimpleBlockItem("warped_drawer_cabinet", ModBlocks.WARPED_DRAWER_CABINET);
    public static final DeferredItem<BlockItem> CINNABAR_COUNTERTOP =
            ITEMS.registerSimpleBlockItem("cinnabar_countertop", ModBlocks.CINNABAR_COUNTERTOP);
    public static final DeferredItem<BlockItem> CINNABAR_SINK =
            ITEMS.registerSimpleBlockItem("cinnabar_sink", ModBlocks.CINNABAR_SINK);
    // FURNITURE_VARIANT_ITEM_REGISTRATIONS

    public static final java.util.Map<String, DeferredItem<BlockItem>> DRESSER_ITEMS = registerDresserItems();
    public static final java.util.Map<String, DeferredItem<BlockItem>> SIDE_TABLE_ITEMS = registerSideTableItems();
    public static final DeferredItem<BlockItem> OAK_SIDE_TABLE_ITEM = SIDE_TABLE_ITEMS.get("oak_side_table");
    public static final java.util.Map<String, DeferredItem<BlockItem>> DINING_TABLE_ITEMS = registerDiningTableItems();
    public static final DeferredItem<BlockItem> OAK_DINING_TABLE_ITEM = DINING_TABLE_ITEMS.get("oak_dining_table");
    public static final java.util.Map<String, DeferredItem<BlockItem>> NIGHTSTAND_ITEMS = registerNightstandItems();
    public static final DeferredItem<BlockItem> OAK_NIGHTSTAND_ITEM = NIGHTSTAND_ITEMS.get("oak_nightstand");
    public static final DeferredItem<BlockItem> LEATHER_LOVE_SEAT_ITEM =
            ITEMS.registerSimpleBlockItem("leather_love_seat", ModBlocks.LEATHER_LOVE_SEAT);
    public static final DeferredItem<BlockItem> LEATHER_SOFA_ITEM =
            ITEMS.registerSimpleBlockItem("leather_sofa", ModBlocks.LEATHER_SOFA);
    public static final DeferredItem<BlockItem> LEATHER_CHAIR_ITEM =
            ITEMS.registerSimpleBlockItem("leather_chair", ModBlocks.LEATHER_CHAIR);
    public static final java.util.Map<String, DeferredItem<BlockItem>> DINING_CHAIR_ITEMS = registerDiningChairItems();
    public static final DeferredItem<BlockItem> OAK_DINING_CHAIR_ITEM = DINING_CHAIR_ITEMS.get("oak_dining_chair");

    private static java.util.Map<String, DeferredItem<BlockItem>> registerDresserItems() {
        java.util.Map<String, DeferredItem<BlockItem>> items = new java.util.LinkedHashMap<>();
        for (String id : DresserVariants.IDS) {
            items.put(id, ITEMS.registerSimpleBlockItem(id, ModBlocks.DRESSERS.get(id)));
        }
        return java.util.Collections.unmodifiableMap(items);
    }

    private static java.util.Map<String, DeferredItem<BlockItem>> registerDiningChairItems() {
        java.util.Map<String, DeferredItem<BlockItem>> items = new java.util.LinkedHashMap<>();
        DiningChairVariants.VALUES.forEach(definition -> items.put(definition.id(),
                ITEMS.registerSimpleBlockItem(definition.id(), ModBlocks.DINING_CHAIRS.get(definition.id()))));
        return java.util.Collections.unmodifiableMap(items);
    }

    private static java.util.Map<String, DeferredItem<BlockItem>> registerDiningTableItems() {
        java.util.Map<String, DeferredItem<BlockItem>> items = new java.util.LinkedHashMap<>();
        DiningTableVariants.VALUES.forEach(definition -> items.put(definition.id(),
                ITEMS.registerSimpleBlockItem(definition.id(), ModBlocks.DINING_TABLES.get(definition.id()))));
        return java.util.Collections.unmodifiableMap(items);
    }

    private static java.util.Map<String, DeferredItem<BlockItem>> registerSideTableItems() {
        java.util.Map<String, DeferredItem<BlockItem>> items = new java.util.LinkedHashMap<>();
        SideTableVariants.VALUES.forEach(definition -> items.put(definition.id(),
                ITEMS.registerSimpleBlockItem(definition.id(), ModBlocks.SIDE_TABLES.get(definition.id()))));
        return java.util.Collections.unmodifiableMap(items);
    }

    private static java.util.Map<String, DeferredItem<BlockItem>> registerNightstandItems() {
        java.util.Map<String, DeferredItem<BlockItem>> items = new java.util.LinkedHashMap<>();
        NightstandVariants.VALUES.forEach(definition -> items.put(definition.id(),
                ITEMS.registerSimpleBlockItem(definition.id(), ModBlocks.NIGHTSTANDS.get(definition.id()))));
        return java.util.Collections.unmodifiableMap(items);
    }

    private static java.util.List<DeferredItem<BlockItem>> registerBasicBlockItems() {
        var items = new java.util.ArrayList<DeferredItem<BlockItem>>();
        ModBlocks.BASIC_BLOCKS.forEach((name, block) -> items.add(ITEMS.registerSimpleBlockItem(name, block)));
        return java.util.List.copyOf(items);
    }

    public static final DeferredItem<BlockItem> OAK_RAILING = ITEMS.registerSimpleBlockItem("oak_railing", ModBlocks.OAK_RAILING);

    public static final DeferredItem<BlockItem> OAK_BALUSTRADE = ITEMS.registerSimpleBlockItem("oak_balustrade", ModBlocks.OAK_BALUSTRADE);
    public static final DeferredItem<BlockItem> OAK_BALUSTER = ITEMS.registerSimpleBlockItem("oak_baluster", ModBlocks.OAK_BALUSTER);

    public static final DeferredItem<BlockItem> OAK_NEWEL_POST = ITEMS.registerSimpleBlockItem("oak_newel_post",ModBlocks.OAK_NEWEL_POST);
    public static final java.util.List<DeferredItem<BlockItem>> WOOD_RAILINGS = registerWoodRailings();
    private static java.util.List<DeferredItem<BlockItem>> registerWoodRailings() {
        var items=new java.util.ArrayList<DeferredItem<BlockItem>>();
        ModBlocks.WOOD_RAILINGS.forEach((wood,family)->{
            items.add(ITEMS.registerSimpleBlockItem(wood+"_railing",family.railing()));
            items.add(ITEMS.registerSimpleBlockItem(wood+"_balustrade",family.balustrade()));
            items.add(ITEMS.registerSimpleBlockItem(wood+"_baluster",family.baluster()));
            items.add(ITEMS.registerSimpleBlockItem(wood+"_newel_post",family.newelPost()));
        });
        return java.util.List.copyOf(items);
    }


    public static final DeferredItem<WallpaperItem> CORNFLOWER_WALLPAPER = registerWallpaper("cornflower_wallpaper");
    public static final DeferredItem<WallpaperItem> SCALLOPED_WALLPAPER = registerWallpaper("scalloped_wallpaper");
    public static final DeferredItem<WallpaperItem> OGEE_LATTICE_WALLPAPER = registerWallpaper("ogee_lattice_wallpaper");
    public static final DeferredItem<WallpaperItem> STRIPED_WALLPAPER = registerWallpaper("striped_wallpaper");
    public static final DeferredItem<WallpaperItem> GOTHIC_WALLPAPER = registerWallpaper("gothic_wallpaper");
    public static final DeferredItem<WallpaperItem> PINK_FLORAL_WALLPAPER = registerWallpaper("pink_floral_wallpaper");

    public static final DeferredItem<StoneQuoinItem> STONE_QUOIN =
            ITEMS.registerItem("stone_quoin", StoneQuoinItem::new, () -> new Item.Properties());

    public static final DeferredItem<WallFinishItem> SHAKE_SIDING = registerWallFinish(
            "shake_siding", WallFinishItem.Placement.WALLS_ONLY
    );
    public static final DeferredItem<WallFinishItem> WHITE_STUCCO = registerWallFinish(
            "white_stucco", WallFinishItem.Placement.ALL_FACES
    );
    public static final DeferredItem<WallFinishItem> RED_BRICK = registerWallFinish(
            "red_brick", WallFinishItem.Placement.WALLS_AND_TOP
    );
    public static final DeferredItem<WallFinishItem> BUFF_BRICK = registerWallFinish(
            "buff_brick", WallFinishItem.Placement.WALLS_AND_TOP
    );
    public static final DeferredItem<WainscotingItem> WAINSCOTING_ITEM = ITEMS.registerItem(
            "wainscoting",
            WainscotingItem::new,
            () -> new Item.Properties()
    );

    public static final DeferredItem<Item> QUICKLIME_POWDER = ITEMS.registerSimpleItem(
            "quicklime_powder",
            () -> new Item.Properties()
    );

    public static final DeferredItem<Item> CLAY_TOILET = ITEMS.registerSimpleItem(
            "clay_toilet",
            () -> new Item.Properties()
    );

    public static final DeferredItem<Item> CLAY_CLAWFOOT_BATHTUB = ITEMS.registerSimpleItem(
            "clay_clawfoot_bathtub",
            () -> new Item.Properties()
    );

    public static final DeferredItem<BucketOfPlasterItem> BUCKET_OF_PLASTER = ITEMS.registerItem(
            "bucket_of_plaster",
            BucketOfPlasterItem::new,
            () -> new Item.Properties().durability(BucketOfPlasterItem.APPLICATIONS)
    );

    public static final DeferredItem<KeyRingItem> KEY_RING = ITEMS.registerItem("key_ring", KeyRingItem::new, () -> new Item.Properties());

    public static final DeferredItem<DoorKeyItem> DOOR_KEY = ITEMS.registerItem(
            "door_key",
            DoorKeyItem::new,
            () -> new Item.Properties()
    );

    public static final DeferredItem<TallDoorItem> TALL_MANGROVE_DOOR_ITEM = ITEMS.register(
            "tall_mangrove_door",
            (key) -> new TallDoorItem(ModBlocks.TALL_MANGROVE_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<BlockItem> TALL_MANGROVE_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_mangrove_sidelight", ModBlocks.TALL_MANGROVE_SIDELIGHT);

    public static final DeferredItem<TallDoorItem> TALL_BIRCH_DOOR_ITEM = ITEMS.register(
            "tall_birch_door",
            key -> new TallDoorItem(ModBlocks.TALL_BIRCH_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<BlockItem> TALL_BIRCH_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_birch_sidelight", ModBlocks.TALL_BIRCH_SIDELIGHT);

    public static final DeferredItem<TallDoorItem> TALL_OAK_DOOR_ITEM = ITEMS.register(
            "tall_oak_door",
            key -> new TallDoorItem(ModBlocks.TALL_OAK_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final java.util.Map<String, DeferredItem<BlockItem>> TRANSOM_ITEMS = registerTransomItems();

    private static java.util.Map<String, DeferredItem<BlockItem>> registerTransomItems() {
        var items = new java.util.LinkedHashMap<String, DeferredItem<BlockItem>>();
        ModBlocks.TRANSOMS.forEach((id, block) -> items.put(id, ITEMS.registerSimpleBlockItem(id, block)));
        return java.util.Collections.unmodifiableMap(items);
    }

    public static final java.util.Map<String, DeferredItem<BlockItem>> WINDOW_PANE_ITEMS = registerWindowPaneItems();

    private static java.util.Map<String, DeferredItem<BlockItem>> registerWindowPaneItems() {
        var items = new java.util.LinkedHashMap<String, DeferredItem<BlockItem>>();
        ModBlocks.WINDOW_PANES.forEach((id, block) -> items.put(id, ITEMS.registerSimpleBlockItem(id, block)));
        return java.util.Collections.unmodifiableMap(items);
    }

    public static final java.util.Map<String, DeferredItem<TallDoorItem>> TALL_PATIO_DOOR_ITEMS = registerPatioDoorItems();
    public static final DeferredItem<TallDoorItem> TALL_OAK_PATIO_DOOR_ITEM = TALL_PATIO_DOOR_ITEMS.get("tall_oak_patio_door");

    private static java.util.Map<String, DeferredItem<TallDoorItem>> registerPatioDoorItems() {
        var items = new java.util.LinkedHashMap<String, DeferredItem<TallDoorItem>>();
        PatioDoorVariants.VALUES.forEach(definition -> items.put(definition.id(), ITEMS.register(definition.id(),
                key -> new TallDoorItem(ModBlocks.TALL_PATIO_DOORS.get(definition.id()).get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix()))));
        return java.util.Collections.unmodifiableMap(items);
    }

    public static final DeferredItem<TallDoorItem> TALL_SPRUCE_DOOR_ITEM = ITEMS.register(
            "tall_spruce_door",
            key -> new TallDoorItem(ModBlocks.TALL_SPRUCE_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_JUNGLE_DOOR_ITEM = ITEMS.register(
            "tall_jungle_door",
            key -> new TallDoorItem(ModBlocks.TALL_JUNGLE_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_ACACIA_DOOR_ITEM = ITEMS.register(
            "tall_acacia_door",
            key -> new TallDoorItem(ModBlocks.TALL_ACACIA_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_DARK_OAK_DOOR_ITEM = ITEMS.register(
            "tall_dark_oak_door",
            key -> new TallDoorItem(ModBlocks.TALL_DARK_OAK_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_CHERRY_DOOR_ITEM = ITEMS.register(
            "tall_cherry_door",
            key -> new TallDoorItem(ModBlocks.TALL_CHERRY_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_BAMBOO_DOOR_ITEM = ITEMS.register(
            "tall_bamboo_door",
            key -> new TallDoorItem(ModBlocks.TALL_BAMBOO_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_PALE_OAK_DOOR_ITEM = ITEMS.register(
            "tall_pale_oak_door",
            key -> new TallDoorItem(ModBlocks.TALL_PALE_OAK_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_CRIMSON_DOOR_ITEM = ITEMS.register(
            "tall_crimson_door",
            key -> new TallDoorItem(ModBlocks.TALL_CRIMSON_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_WARPED_DOOR_ITEM = ITEMS.register(
            "tall_warped_door",
            key -> new TallDoorItem(ModBlocks.TALL_WARPED_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<TallDoorItem> TALL_POPLAR_DOOR_ITEM = ITEMS.register(
            "tall_poplar_door",
            key -> new TallDoorItem(ModBlocks.TALL_POPLAR_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    // TALL_DOOR_VARIANT_ITEM_REGISTRATIONS
    public static final DeferredItem<BlockItem> TALL_OAK_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_oak_sidelight", ModBlocks.TALL_OAK_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_SPRUCE_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_spruce_sidelight", ModBlocks.TALL_SPRUCE_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_JUNGLE_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_jungle_sidelight", ModBlocks.TALL_JUNGLE_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_ACACIA_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_acacia_sidelight", ModBlocks.TALL_ACACIA_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_DARK_OAK_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_dark_oak_sidelight", ModBlocks.TALL_DARK_OAK_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_CHERRY_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_cherry_sidelight", ModBlocks.TALL_CHERRY_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_BAMBOO_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_bamboo_sidelight", ModBlocks.TALL_BAMBOO_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_PALE_OAK_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_pale_oak_sidelight", ModBlocks.TALL_PALE_OAK_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_CRIMSON_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_crimson_sidelight", ModBlocks.TALL_CRIMSON_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_WARPED_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_warped_sidelight", ModBlocks.TALL_WARPED_SIDELIGHT);

    public static final DeferredItem<BlockItem> TALL_POPLAR_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_poplar_sidelight", ModBlocks.TALL_POPLAR_SIDELIGHT);

    // TALL_SIDELIGHT_VARIANT_ITEM_REGISTRATIONS

    public static final DeferredItem<BlockItem> GLASS_SHOWER_SURROUND_ITEM =
            ITEMS.registerSimpleBlockItem("glass_shower_surround", ModBlocks.GLASS_SHOWER_SURROUND);

    public static final DeferredItem<BlockItem> GLASS_SHOWER_DOOR_ITEM =
            ITEMS.registerSimpleBlockItem("glass_shower_door", ModBlocks.GLASS_SHOWER_DOOR);

    public static final DeferredItem<BlockItem> WALL_SHOWER_HEAD_ITEM =
            ITEMS.registerSimpleBlockItem("wall_shower_head", ModBlocks.WALL_SHOWER_HEAD);

    public static final DeferredItem<BlockItem> RAIN_SHOWER_HEAD_ITEM =
            ITEMS.registerSimpleBlockItem("rain_shower_head", ModBlocks.RAIN_SHOWER_HEAD);

    public static final DeferredItem<BlockItem> KEY_CUTTER_ITEM = ITEMS.registerSimpleBlockItem("key_cutter", ModBlocks.KEY_CUTTER);
    public static final DeferredItem<BlockItem> TOILET_ITEM = ITEMS.registerSimpleBlockItem("toilet", ModBlocks.TOILET);
    public static final DeferredItem<BlockItem> CLAWFOOT_BATHTUB_ITEM = ITEMS.registerSimpleBlockItem("clawfoot_bathtub", ModBlocks.CLAWFOOT_BATHTUB);
    public static final java.util.Map<String, DeferredItem<BlockItem>> FOUR_POST_BED_ITEMS = registerFourPostBedItems();
    public static final DeferredItem<BlockItem> DARK_OAK_RED_FOUR_POST_BED_ITEM =
            FOUR_POST_BED_ITEMS.get("dark_oak_red_four_post_bed");

    private static java.util.Map<String, DeferredItem<BlockItem>> registerFourPostBedItems() {
        java.util.Map<String, DeferredItem<BlockItem>> items = new java.util.LinkedHashMap<>();
        for (String id : FourPostBedVariants.IDS) {
            items.put(id, ITEMS.registerSimpleBlockItem(id, ModBlocks.FOUR_POST_BEDS.get(id)));
        }
        return java.util.Collections.unmodifiableMap(items);
    }
    public static final DeferredItem<BlockItem> REFRIGERATOR_ITEM = ITEMS.registerSimpleBlockItem("refrigerator", ModBlocks.REFRIGERATOR);
    public static final DeferredItem<BlockItem> OVEN_ITEM = ITEMS.registerSimpleBlockItem("oven", ModBlocks.OVEN);
    public static final DeferredItem<BlockItem> INDUCTION_COOKTOP_ITEM =
            ITEMS.registerSimpleBlockItem("induction_cooktop", ModBlocks.INDUCTION_COOKTOP);
    public static final DeferredItem<BlockItem> ELEVATOR_SHAFT_PANEL_ITEM = ITEMS.registerSimpleBlockItem("elevator_shaft_panel", ModBlocks.ELEVATOR_SHAFT_PANEL);
    public static final DeferredItem<BlockItem> ELEVATOR_FLOOR_ITEM = ITEMS.registerSimpleBlockItem("elevator_floor", ModBlocks.ELEVATOR_FLOOR);
    public static final DeferredItem<BlockItem> ELEVATOR_DOOR_ITEM = ITEMS.registerSimpleBlockItem("elevator_door", ModBlocks.ELEVATOR_DOOR);
    public static final DeferredItem<BlockItem> ELEVATOR_CALL_BUTTON_ITEM = ITEMS.registerSimpleBlockItem("elevator_call_button", ModBlocks.ELEVATOR_CALL_BUTTON);
    public static final DeferredItem<BlockItem> ELEVATOR_DESTINATION_CONTROL_ITEM = ITEMS.registerSimpleBlockItem("elevator_destination_control", ModBlocks.ELEVATOR_DESTINATION_CONTROL);
    public static final DeferredItem<BlockItem> BRUSHED_STAINLESS_STEEL_ITEM = ITEMS.registerSimpleBlockItem("brushed_stainless_steel", ModBlocks.BRUSHED_STAINLESS_STEEL);
    public static final DeferredItem<BlockItem> CASING_ITEM = ITEMS.registerSimpleBlockItem("casing", ModBlocks.CASING);
    public static final DeferredItem<BlockItem> WINDOW_APRON_ITEM = ITEMS.registerSimpleBlockItem("window_apron", ModBlocks.WINDOW_APRON);
    public static final DeferredItem<BlockItem> CROWN_MOULDING_ITEM = ITEMS.registerSimpleBlockItem("crown_moulding", ModBlocks.CROWN_MOULDING);
    public static final DeferredItem<BlockItem> BASEBOARD_ITEM = ITEMS.registerSimpleBlockItem("baseboard", ModBlocks.BASEBOARD);
    public static final DeferredItem<BlockItem> PLINTH_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("plinth_block", ModBlocks.PLINTH_BLOCK);
    public static final DeferredItem<BlockItem> SQUARE_PLASTER_COLUMN_ITEM =
            ITEMS.registerSimpleBlockItem("square_plaster_column", ModBlocks.SQUARE_PLASTER_COLUMN);
    public static final DeferredItem<BlockItem> SQUARE_PLASTER_COLUMN_BASE_ITEM =
            ITEMS.registerSimpleBlockItem("square_plaster_column_base", ModBlocks.SQUARE_PLASTER_COLUMN_BASE);
    public static final DeferredItem<BlockItem> SQUARE_PLASTER_COLUMN_CAPITAL_ITEM =
            ITEMS.registerSimpleBlockItem("square_plaster_column_capital", ModBlocks.SQUARE_PLASTER_COLUMN_CAPITAL);
    public static final DeferredItem<BlockItem> ROUND_PLASTER_COLUMN_ITEM =
            ITEMS.registerSimpleBlockItem("round_plaster_column", ModBlocks.ROUND_PLASTER_COLUMN);
    public static final DeferredItem<BlockItem> ROUND_PLASTER_COLUMN_BASE_ITEM =
            ITEMS.registerSimpleBlockItem("round_plaster_column_base", ModBlocks.ROUND_PLASTER_COLUMN_BASE);
    public static final DeferredItem<BlockItem> ROUND_PLASTER_COLUMN_CAPITAL_ITEM =
            ITEMS.registerSimpleBlockItem("round_plaster_column_capital", ModBlocks.ROUND_PLASTER_COLUMN_CAPITAL);
    public static final DeferredItem<BlockItem> CORNER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("corner_block", ModBlocks.CORNER_BLOCK);

    public static final DeferredItem<BlockItem> COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("copper_wall_sconce", ModBlocks.COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("exposed_copper_wall_sconce", ModBlocks.EXPOSED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("weathered_copper_wall_sconce", ModBlocks.WEATHERED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("oxidized_copper_wall_sconce", ModBlocks.OXIDIZED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WAXED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("waxed_copper_wall_sconce", ModBlocks.WAXED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_wall_sconce", ModBlocks.WAXED_EXPOSED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_wall_sconce", ModBlocks.WAXED_WEATHERED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_wall_sconce", ModBlocks.WAXED_OXIDIZED_COPPER_WALL_SCONCE);

    public static final DeferredItem<BlockItem> COPPER_CHANDELIER_CHAIN = ITEMS.registerSimpleBlockItem("copper_chain", ModBlocks.COPPER_CHANDELIER_CHAIN);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_CHANDELIER_CHAIN = ITEMS.registerSimpleBlockItem("exposed_copper_chain", ModBlocks.EXPOSED_COPPER_CHANDELIER_CHAIN);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_CHANDELIER_CHAIN = ITEMS.registerSimpleBlockItem("weathered_copper_chain", ModBlocks.WEATHERED_COPPER_CHANDELIER_CHAIN);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_CHANDELIER_CHAIN = ITEMS.registerSimpleBlockItem("oxidized_copper_chain", ModBlocks.OXIDIZED_COPPER_CHANDELIER_CHAIN);
    public static final DeferredItem<BlockItem> WAXED_COPPER_CHANDELIER_CHAIN = ITEMS.registerSimpleBlockItem("waxed_copper_chain", ModBlocks.WAXED_COPPER_CHANDELIER_CHAIN);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_CHANDELIER_CHAIN = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_chain", ModBlocks.WAXED_EXPOSED_COPPER_CHANDELIER_CHAIN);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_CHANDELIER_CHAIN = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_chain", ModBlocks.WAXED_WEATHERED_COPPER_CHANDELIER_CHAIN);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_CHANDELIER_CHAIN = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_chain", ModBlocks.WAXED_OXIDIZED_COPPER_CHANDELIER_CHAIN);
    public static final DeferredItem<BlockItem> COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("copper_chandelier", ModBlocks.COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("exposed_copper_chandelier", ModBlocks.EXPOSED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("weathered_copper_chandelier", ModBlocks.WEATHERED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("oxidized_copper_chandelier", ModBlocks.OXIDIZED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WAXED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("waxed_copper_chandelier", ModBlocks.WAXED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_chandelier", ModBlocks.WAXED_EXPOSED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_chandelier", ModBlocks.WAXED_WEATHERED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_chandelier", ModBlocks.WAXED_OXIDIZED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> COPPER_LAMP_POST = ITEMS.registerSimpleBlockItem("copper_lamp_post", ModBlocks.COPPER_LAMP_POST);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_LAMP_POST = ITEMS.registerSimpleBlockItem("exposed_copper_lamp_post", ModBlocks.EXPOSED_COPPER_LAMP_POST);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_LAMP_POST = ITEMS.registerSimpleBlockItem("weathered_copper_lamp_post", ModBlocks.WEATHERED_COPPER_LAMP_POST);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_LAMP_POST = ITEMS.registerSimpleBlockItem("oxidized_copper_lamp_post", ModBlocks.OXIDIZED_COPPER_LAMP_POST);
    public static final DeferredItem<BlockItem> WAXED_COPPER_LAMP_POST = ITEMS.registerSimpleBlockItem("waxed_copper_lamp_post", ModBlocks.WAXED_COPPER_LAMP_POST);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_LAMP_POST = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_lamp_post", ModBlocks.WAXED_EXPOSED_COPPER_LAMP_POST);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_LAMP_POST = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_lamp_post", ModBlocks.WAXED_WEATHERED_COPPER_LAMP_POST);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_LAMP_POST = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_lamp_post", ModBlocks.WAXED_OXIDIZED_COPPER_LAMP_POST);
    public static final DeferredItem<BlockItem> COPPER_FLOOR_LAMP = ITEMS.registerSimpleBlockItem("copper_floor_lamp", ModBlocks.COPPER_FLOOR_LAMP);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_FLOOR_LAMP = ITEMS.registerSimpleBlockItem("exposed_copper_floor_lamp", ModBlocks.EXPOSED_COPPER_FLOOR_LAMP);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_FLOOR_LAMP = ITEMS.registerSimpleBlockItem("weathered_copper_floor_lamp", ModBlocks.WEATHERED_COPPER_FLOOR_LAMP);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_FLOOR_LAMP = ITEMS.registerSimpleBlockItem("oxidized_copper_floor_lamp", ModBlocks.OXIDIZED_COPPER_FLOOR_LAMP);
    public static final DeferredItem<BlockItem> WAXED_COPPER_FLOOR_LAMP = ITEMS.registerSimpleBlockItem("waxed_copper_floor_lamp", ModBlocks.WAXED_COPPER_FLOOR_LAMP);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_FLOOR_LAMP = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_floor_lamp", ModBlocks.WAXED_EXPOSED_COPPER_FLOOR_LAMP);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_FLOOR_LAMP = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_floor_lamp", ModBlocks.WAXED_WEATHERED_COPPER_FLOOR_LAMP);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_FLOOR_LAMP = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_floor_lamp", ModBlocks.WAXED_OXIDIZED_COPPER_FLOOR_LAMP);
    public static final DeferredItem<BlockItem> COPPER_BOWL_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("copper_bowl_ceiling_light", ModBlocks.COPPER_BOWL_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_BOWL_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("exposed_copper_bowl_ceiling_light", ModBlocks.EXPOSED_COPPER_BOWL_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_BOWL_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("weathered_copper_bowl_ceiling_light", ModBlocks.WEATHERED_COPPER_BOWL_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_BOWL_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("oxidized_copper_bowl_ceiling_light", ModBlocks.OXIDIZED_COPPER_BOWL_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WAXED_COPPER_BOWL_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("waxed_copper_bowl_ceiling_light", ModBlocks.WAXED_COPPER_BOWL_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_BOWL_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_bowl_ceiling_light", ModBlocks.WAXED_EXPOSED_COPPER_BOWL_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_BOWL_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_bowl_ceiling_light", ModBlocks.WAXED_WEATHERED_COPPER_BOWL_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_BOWL_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_bowl_ceiling_light", ModBlocks.WAXED_OXIDIZED_COPPER_BOWL_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> COPPER_COFFERED_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("copper_coffered_ceiling_light", ModBlocks.COPPER_COFFERED_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_COFFERED_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("exposed_copper_coffered_ceiling_light", ModBlocks.EXPOSED_COPPER_COFFERED_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_COFFERED_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("weathered_copper_coffered_ceiling_light", ModBlocks.WEATHERED_COPPER_COFFERED_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_COFFERED_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("oxidized_copper_coffered_ceiling_light", ModBlocks.OXIDIZED_COPPER_COFFERED_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WAXED_COPPER_COFFERED_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("waxed_copper_coffered_ceiling_light", ModBlocks.WAXED_COPPER_COFFERED_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_COFFERED_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_coffered_ceiling_light", ModBlocks.WAXED_EXPOSED_COPPER_COFFERED_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_COFFERED_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_coffered_ceiling_light", ModBlocks.WAXED_WEATHERED_COPPER_COFFERED_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_COFFERED_CEILING_LIGHT = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_coffered_ceiling_light", ModBlocks.WAXED_OXIDIZED_COPPER_COFFERED_CEILING_LIGHT);
    public static final DeferredItem<BlockItem> COPPER_TABLE_LAMP = ITEMS.registerSimpleBlockItem("copper_table_lamp", ModBlocks.COPPER_TABLE_LAMP);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_TABLE_LAMP = ITEMS.registerSimpleBlockItem("exposed_copper_table_lamp", ModBlocks.EXPOSED_COPPER_TABLE_LAMP);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_TABLE_LAMP = ITEMS.registerSimpleBlockItem("weathered_copper_table_lamp", ModBlocks.WEATHERED_COPPER_TABLE_LAMP);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_TABLE_LAMP = ITEMS.registerSimpleBlockItem("oxidized_copper_table_lamp", ModBlocks.OXIDIZED_COPPER_TABLE_LAMP);
    public static final DeferredItem<BlockItem> WAXED_COPPER_TABLE_LAMP = ITEMS.registerSimpleBlockItem("waxed_copper_table_lamp", ModBlocks.WAXED_COPPER_TABLE_LAMP);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_TABLE_LAMP = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_table_lamp", ModBlocks.WAXED_EXPOSED_COPPER_TABLE_LAMP);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_TABLE_LAMP = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_table_lamp", ModBlocks.WAXED_WEATHERED_COPPER_TABLE_LAMP);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_TABLE_LAMP = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_table_lamp", ModBlocks.WAXED_OXIDIZED_COPPER_TABLE_LAMP);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.addListener(ModItems::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DOOR_KEY);
            event.accept(KEY_RING);
            event.accept(CORNFLOWER_WALLPAPER);
            event.accept(SCALLOPED_WALLPAPER);
            event.accept(OGEE_LATTICE_WALLPAPER);
            event.accept(STRIPED_WALLPAPER);
            event.accept(GOTHIC_WALLPAPER);
            event.accept(PINK_FLORAL_WALLPAPER);
            event.accept(SHAKE_SIDING);
            event.accept(STONE_QUOIN);
            event.accept(WHITE_STUCCO);
            event.accept(RED_BRICK);
            event.accept(BUFF_BRICK);
            event.accept(WAINSCOTING_ITEM);
            event.accept(BUCKET_OF_PLASTER);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(TOILET_ITEM);
            event.accept(CLAWFOOT_BATHTUB_ITEM);
            FOUR_POST_BED_ITEMS.values().forEach(event::accept);
            DRESSER_ITEMS.values().forEach(event::accept);
            SIDE_TABLE_ITEMS.values().forEach(event::accept);
            DINING_TABLE_ITEMS.values().forEach(event::accept);
            NIGHTSTAND_ITEMS.values().forEach(event::accept);
            event.accept(LEATHER_LOVE_SEAT_ITEM);
            event.accept(LEATHER_SOFA_ITEM);
            event.accept(LEATHER_CHAIR_ITEM);
            DINING_CHAIR_ITEMS.values().forEach(event::accept);
            event.accept(REFRIGERATOR_ITEM);
            event.accept(OVEN_ITEM);
            event.accept(INDUCTION_COOKTOP_ITEM);
            event.accept(COPPER_CHANDELIER_CHAIN);
            event.accept(EXPOSED_COPPER_CHANDELIER_CHAIN);
            event.accept(WEATHERED_COPPER_CHANDELIER_CHAIN);
            event.accept(OXIDIZED_COPPER_CHANDELIER_CHAIN);
            event.accept(WAXED_COPPER_CHANDELIER_CHAIN);
            event.accept(WAXED_EXPOSED_COPPER_CHANDELIER_CHAIN);
            event.accept(WAXED_WEATHERED_COPPER_CHANDELIER_CHAIN);
            event.accept(WAXED_OXIDIZED_COPPER_CHANDELIER_CHAIN);
            event.accept(COPPER_CHANDELIER);
            event.accept(EXPOSED_COPPER_CHANDELIER);
            event.accept(WEATHERED_COPPER_CHANDELIER);
            event.accept(OXIDIZED_COPPER_CHANDELIER);
            event.accept(WAXED_COPPER_CHANDELIER);
            event.accept(WAXED_EXPOSED_COPPER_CHANDELIER);
            event.accept(WAXED_WEATHERED_COPPER_CHANDELIER);
            event.accept(WAXED_OXIDIZED_COPPER_CHANDELIER);
            event.accept(COPPER_LAMP_POST);
            event.accept(EXPOSED_COPPER_LAMP_POST);
            event.accept(WEATHERED_COPPER_LAMP_POST);
            event.accept(OXIDIZED_COPPER_LAMP_POST);
            event.accept(WAXED_COPPER_LAMP_POST);
            event.accept(WAXED_EXPOSED_COPPER_LAMP_POST);
            event.accept(WAXED_WEATHERED_COPPER_LAMP_POST);
            event.accept(WAXED_OXIDIZED_COPPER_LAMP_POST);
            event.accept(COPPER_FLOOR_LAMP);
            event.accept(EXPOSED_COPPER_FLOOR_LAMP);
            event.accept(WEATHERED_COPPER_FLOOR_LAMP);
            event.accept(OXIDIZED_COPPER_FLOOR_LAMP);
            event.accept(WAXED_COPPER_FLOOR_LAMP);
            event.accept(WAXED_EXPOSED_COPPER_FLOOR_LAMP);
            event.accept(WAXED_WEATHERED_COPPER_FLOOR_LAMP);
            event.accept(WAXED_OXIDIZED_COPPER_FLOOR_LAMP);
            event.accept(COPPER_BOWL_CEILING_LIGHT);
            event.accept(EXPOSED_COPPER_BOWL_CEILING_LIGHT);
            event.accept(WEATHERED_COPPER_BOWL_CEILING_LIGHT);
            event.accept(OXIDIZED_COPPER_BOWL_CEILING_LIGHT);
            event.accept(WAXED_COPPER_BOWL_CEILING_LIGHT);
            event.accept(WAXED_EXPOSED_COPPER_BOWL_CEILING_LIGHT);
            event.accept(WAXED_WEATHERED_COPPER_BOWL_CEILING_LIGHT);
            event.accept(WAXED_OXIDIZED_COPPER_BOWL_CEILING_LIGHT);
            event.accept(COPPER_COFFERED_CEILING_LIGHT);
            event.accept(EXPOSED_COPPER_COFFERED_CEILING_LIGHT);
            event.accept(WEATHERED_COPPER_COFFERED_CEILING_LIGHT);
            event.accept(OXIDIZED_COPPER_COFFERED_CEILING_LIGHT);
            event.accept(WAXED_COPPER_COFFERED_CEILING_LIGHT);
            event.accept(WAXED_EXPOSED_COPPER_COFFERED_CEILING_LIGHT);
            event.accept(WAXED_WEATHERED_COPPER_COFFERED_CEILING_LIGHT);
            event.accept(WAXED_OXIDIZED_COPPER_COFFERED_CEILING_LIGHT);
            event.accept(COPPER_TABLE_LAMP);
            event.accept(EXPOSED_COPPER_TABLE_LAMP);
            event.accept(WEATHERED_COPPER_TABLE_LAMP);
            event.accept(OXIDIZED_COPPER_TABLE_LAMP);
            event.accept(WAXED_COPPER_TABLE_LAMP);
            event.accept(WAXED_EXPOSED_COPPER_TABLE_LAMP);
            event.accept(WAXED_WEATHERED_COPPER_TABLE_LAMP);
            event.accept(WAXED_OXIDIZED_COPPER_TABLE_LAMP);
            event.accept(COPPER_WALL_SCONCE);
            event.accept(EXPOSED_COPPER_WALL_SCONCE);
            event.accept(WEATHERED_COPPER_WALL_SCONCE);
            event.accept(OXIDIZED_COPPER_WALL_SCONCE);
            event.accept(WAXED_COPPER_WALL_SCONCE);
            event.accept(WAXED_EXPOSED_COPPER_WALL_SCONCE);
            event.accept(WAXED_WEATHERED_COPPER_WALL_SCONCE);
            event.accept(WAXED_OXIDIZED_COPPER_WALL_SCONCE);
            event.accept(DARK_OAK_DOOR_CABINET);
            event.accept(DARK_OAK_DRAWER_CABINET);
            event.accept(PALE_OAK_DOOR_CABINET);
            event.accept(PALE_OAK_DRAWER_CABINET);
            event.accept(POPLAR_DOOR_CABINET);
            event.accept(POPLAR_DRAWER_CABINET);
            event.accept(OAK_DOOR_CABINET);
            event.accept(OAK_DRAWER_CABINET);
            event.accept(SPRUCE_DOOR_CABINET);
            event.accept(SPRUCE_DRAWER_CABINET);
            event.accept(BIRCH_DOOR_CABINET);
            event.accept(BIRCH_DRAWER_CABINET);
            event.accept(JUNGLE_DOOR_CABINET);
            event.accept(JUNGLE_DRAWER_CABINET);
            event.accept(ACACIA_DOOR_CABINET);
            event.accept(ACACIA_DRAWER_CABINET);
            event.accept(MANGROVE_DOOR_CABINET);
            event.accept(MANGROVE_DRAWER_CABINET);
            event.accept(CHERRY_DOOR_CABINET);
            event.accept(CHERRY_DRAWER_CABINET);
            event.accept(BAMBOO_DOOR_CABINET);
            event.accept(BAMBOO_DRAWER_CABINET);
            event.accept(CRIMSON_DOOR_CABINET);
            event.accept(CRIMSON_DRAWER_CABINET);
            event.accept(WARPED_DOOR_CABINET);
            event.accept(WARPED_DRAWER_CABINET);
            event.accept(KEY_CUTTER_ITEM);
            event.accept(ELEVATOR_DOOR_ITEM);
            event.accept(ELEVATOR_CALL_BUTTON_ITEM);
            event.accept(ELEVATOR_DESTINATION_CONTROL_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(QUICKLIME_POWDER);
            event.accept(CLAY_TOILET);
            event.accept(CLAY_CLAWFOOT_BATHTUB);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            BASIC_BLOCK_ITEMS.forEach(event::accept);
            event.accept(CALCITE_SLAB);
            event.accept(BRICK_WINDOW_LEDGE);
            event.accept(BUFF_BRICK_WINDOW_LEDGE);
            event.accept(BUFF_BRICKS);
            event.accept(BUFF_BRICK_SLAB);
            event.accept(BUFF_BRICK_STAIRS);
            event.accept(BUFF_BRICK_WALL);
            event.accept(CALCITE_COUNTERTOP);
            event.accept(CALCITE_SINK);
            event.accept(KITCHEN_SINK);
            event.accept(SMOOTH_STONE_COUNTERTOP);
            event.accept(SMOOTH_STONE_SINK);
            event.accept(STONE_COUNTERTOP);
            event.accept(STONE_SINK);
            event.accept(STONE_BRICK_COUNTERTOP);
            event.accept(STONE_BRICK_SINK);
            event.accept(NETHER_BRICK_COUNTERTOP);
            event.accept(NETHER_BRICK_SINK);
            event.accept(QUARTZ_COUNTERTOP);
            event.accept(QUARTZ_SINK);
            event.accept(POLISHED_GRANITE_COUNTERTOP);
            event.accept(POLISHED_GRANITE_SINK);
            event.accept(POLISHED_DIORITE_COUNTERTOP);
            event.accept(POLISHED_DIORITE_SINK);
            event.accept(END_STONE_BRICK_COUNTERTOP);
            event.accept(END_STONE_BRICK_SINK);
            event.accept(SMOOTH_QUARTZ_COUNTERTOP);
            event.accept(SMOOTH_QUARTZ_SINK);
            event.accept(GRANITE_COUNTERTOP);
            event.accept(GRANITE_SINK);
            event.accept(ANDESITE_COUNTERTOP);
            event.accept(ANDESITE_SINK);
            event.accept(RED_NETHER_BRICK_COUNTERTOP);
            event.accept(RED_NETHER_BRICK_SINK);
            event.accept(POLISHED_ANDESITE_COUNTERTOP);
            event.accept(POLISHED_ANDESITE_SINK);
            event.accept(DIORITE_COUNTERTOP);
            event.accept(DIORITE_SINK);
            event.accept(POLISHED_BLACKSTONE_COUNTERTOP);
            event.accept(POLISHED_BLACKSTONE_SINK);
            event.accept(POLISHED_BLACKSTONE_BRICK_COUNTERTOP);
            event.accept(POLISHED_BLACKSTONE_BRICK_SINK);
            event.accept(POLISHED_DEEPSLATE_COUNTERTOP);
            event.accept(POLISHED_DEEPSLATE_SINK);
            event.accept(DEEPSLATE_BRICK_COUNTERTOP);
            event.accept(DEEPSLATE_BRICK_SINK);
            event.accept(DEEPSLATE_TILE_COUNTERTOP);
            event.accept(DEEPSLATE_TILE_SINK);
            event.accept(RESIN_BRICK_COUNTERTOP);
            event.accept(RESIN_BRICK_SINK);
            event.accept(PRISMARINE_BRICK_COUNTERTOP);
            event.accept(PRISMARINE_BRICK_SINK);
            event.accept(DARK_PRISMARINE_COUNTERTOP);
            event.accept(DARK_PRISMARINE_SINK);
            event.accept(PURPUR_COUNTERTOP);
            event.accept(PURPUR_SINK);
            event.accept(WHITE_CONCRETE_COUNTERTOP);
            event.accept(WHITE_CONCRETE_SINK);
            event.accept(LIGHT_GRAY_CONCRETE_COUNTERTOP);
            event.accept(LIGHT_GRAY_CONCRETE_SINK);
            event.accept(GRAY_CONCRETE_COUNTERTOP);
            event.accept(GRAY_CONCRETE_SINK);
            event.accept(BLACK_CONCRETE_COUNTERTOP);
            event.accept(BLACK_CONCRETE_SINK);
            event.accept(BROWN_CONCRETE_COUNTERTOP);
            event.accept(BROWN_CONCRETE_SINK);
            event.accept(RED_CONCRETE_COUNTERTOP);
            event.accept(RED_CONCRETE_SINK);
            event.accept(ORANGE_CONCRETE_COUNTERTOP);
            event.accept(ORANGE_CONCRETE_SINK);
            event.accept(YELLOW_CONCRETE_COUNTERTOP);
            event.accept(YELLOW_CONCRETE_SINK);
            event.accept(LIME_CONCRETE_COUNTERTOP);
            event.accept(LIME_CONCRETE_SINK);
            event.accept(GREEN_CONCRETE_COUNTERTOP);
            event.accept(GREEN_CONCRETE_SINK);
            event.accept(CYAN_CONCRETE_COUNTERTOP);
            event.accept(CYAN_CONCRETE_SINK);
            event.accept(LIGHT_BLUE_CONCRETE_COUNTERTOP);
            event.accept(LIGHT_BLUE_CONCRETE_SINK);
            event.accept(BLUE_CONCRETE_COUNTERTOP);
            event.accept(BLUE_CONCRETE_SINK);
            event.accept(PURPLE_CONCRETE_COUNTERTOP);
            event.accept(PURPLE_CONCRETE_SINK);
            event.accept(MAGENTA_CONCRETE_COUNTERTOP);
            event.accept(MAGENTA_CONCRETE_SINK);
            event.accept(PINK_CONCRETE_COUNTERTOP);
            event.accept(PINK_CONCRETE_SINK);
            event.accept(CUT_COPPER_COUNTERTOP);
            event.accept(CUT_COPPER_SINK);
            event.accept(EXPOSED_CUT_COPPER_COUNTERTOP);
            event.accept(EXPOSED_CUT_COPPER_SINK);
            event.accept(WEATHERED_CUT_COPPER_COUNTERTOP);
            event.accept(WEATHERED_CUT_COPPER_SINK);
            event.accept(OXIDIZED_CUT_COPPER_COUNTERTOP);
            event.accept(OXIDIZED_CUT_COPPER_SINK);
            event.accept(WAXED_CUT_COPPER_COUNTERTOP);
            event.accept(WAXED_CUT_COPPER_SINK);
            event.accept(WAXED_EXPOSED_CUT_COPPER_COUNTERTOP);
            event.accept(WAXED_EXPOSED_CUT_COPPER_SINK);
            event.accept(WAXED_WEATHERED_CUT_COPPER_COUNTERTOP);
            event.accept(WAXED_WEATHERED_CUT_COPPER_SINK);
            event.accept(WAXED_OXIDIZED_CUT_COPPER_COUNTERTOP);
            event.accept(WAXED_OXIDIZED_CUT_COPPER_SINK);
            event.accept(CINNABAR_COUNTERTOP);
            event.accept(CINNABAR_SINK);
            // FURNITURE_VARIANT_CREATIVE_ENTRIES
            event.accept(OAK_RAILING);
            event.accept(OAK_BALUSTRADE);
            event.accept(OAK_BALUSTER);
            event.accept(OAK_NEWEL_POST);
            WOOD_RAILINGS.forEach(event::accept);
            event.accept(TALL_MANGROVE_DOOR_ITEM);
            event.accept(TALL_MANGROVE_SIDELIGHT_ITEM);
            event.accept(TALL_BIRCH_DOOR_ITEM);
            event.accept(TALL_BIRCH_SIDELIGHT_ITEM);
            event.accept(TALL_OAK_DOOR_ITEM);
            TALL_PATIO_DOOR_ITEMS.values().forEach(event::accept);
            TRANSOM_ITEMS.values().forEach(event::accept);
            WINDOW_PANE_ITEMS.values().forEach(event::accept);
            event.accept(TALL_SPRUCE_DOOR_ITEM);
            event.accept(TALL_JUNGLE_DOOR_ITEM);
            event.accept(TALL_ACACIA_DOOR_ITEM);
            event.accept(TALL_DARK_OAK_DOOR_ITEM);
            event.accept(TALL_CHERRY_DOOR_ITEM);
            event.accept(TALL_BAMBOO_DOOR_ITEM);
            event.accept(TALL_PALE_OAK_DOOR_ITEM);
            event.accept(TALL_CRIMSON_DOOR_ITEM);
            event.accept(TALL_WARPED_DOOR_ITEM);
            event.accept(TALL_POPLAR_DOOR_ITEM);
            // TALL_DOOR_VARIANT_CREATIVE_ENTRIES
            event.accept(TALL_OAK_SIDELIGHT_ITEM);
            event.accept(TALL_SPRUCE_SIDELIGHT_ITEM);
            event.accept(TALL_JUNGLE_SIDELIGHT_ITEM);
            event.accept(TALL_ACACIA_SIDELIGHT_ITEM);
            event.accept(TALL_DARK_OAK_SIDELIGHT_ITEM);
            event.accept(TALL_CHERRY_SIDELIGHT_ITEM);
            event.accept(TALL_BAMBOO_SIDELIGHT_ITEM);
            event.accept(TALL_PALE_OAK_SIDELIGHT_ITEM);
            event.accept(TALL_CRIMSON_SIDELIGHT_ITEM);
            event.accept(TALL_WARPED_SIDELIGHT_ITEM);
            event.accept(TALL_POPLAR_SIDELIGHT_ITEM);
            // TALL_SIDELIGHT_VARIANT_CREATIVE_ENTRIES
            event.accept(GLASS_SHOWER_SURROUND_ITEM);
            event.accept(GLASS_SHOWER_DOOR_ITEM);
            event.accept(WALL_SHOWER_HEAD_ITEM);
            event.accept(RAIN_SHOWER_HEAD_ITEM);
            event.accept(CASING_ITEM);
            event.accept(WINDOW_APRON_ITEM);
            event.accept(CROWN_MOULDING_ITEM);
            event.accept(BASEBOARD_ITEM);
            event.accept(PLINTH_BLOCK_ITEM);
            event.accept(SQUARE_PLASTER_COLUMN_ITEM);
            event.accept(SQUARE_PLASTER_COLUMN_BASE_ITEM);
            event.accept(SQUARE_PLASTER_COLUMN_CAPITAL_ITEM);
            event.accept(ROUND_PLASTER_COLUMN_ITEM);
            event.accept(ROUND_PLASTER_COLUMN_BASE_ITEM);
            event.accept(ROUND_PLASTER_COLUMN_CAPITAL_ITEM);
            event.accept(CORNER_BLOCK_ITEM);
            event.accept(ELEVATOR_SHAFT_PANEL_ITEM);
            event.accept(ELEVATOR_FLOOR_ITEM);
            event.accept(BRUSHED_STAINLESS_STEEL_ITEM);
        }
    }

    private static DeferredItem<WallpaperItem> registerWallpaper(String name) {
        return ITEMS.registerItem(
                name,
                properties -> new WallpaperItem(
                        properties,
                        Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "block/" + name)
                ),
                () -> new Item.Properties()
        );
    }

    private static DeferredItem<WallFinishItem> registerWallFinish(
            String name,
            WallFinishItem.Placement placement
    ) {
        return ITEMS.registerItem(
                name,
                properties -> new WallFinishItem(
                        properties,
                        Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "block/" + name),
                        placement
                ),
                () -> new Item.Properties()
        );
    }
}
