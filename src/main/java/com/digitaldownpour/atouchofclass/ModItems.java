package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.items.DoorKeyItem;
import com.digitaldownpour.atouchofclass.items.BucketOfPlasterItem;
import com.digitaldownpour.atouchofclass.items.TallDoorItem;
import com.digitaldownpour.atouchofclass.items.WallpaperItem;
import com.digitaldownpour.atouchofclass.items.WainscotingItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

    public static final DeferredItem<Item> COPPER_NUGGET = ITEMS.registerSimpleItem("copper_nugget", new Item.Properties());

    public static final DeferredItem<WallpaperItem> CORNFLOWER_WALLPAPER = registerWallpaper("cornflower_wallpaper");
    public static final DeferredItem<WallpaperItem> SCALLOPED_WALLPAPER = registerWallpaper("scalloped_wallpaper");
    public static final DeferredItem<WallpaperItem> OGEE_LATTICE_WALLPAPER = registerWallpaper("ogee_lattice_wallpaper");
    public static final DeferredItem<WallpaperItem> STRIPED_WALLPAPER = registerWallpaper("striped_wallpaper");
    public static final DeferredItem<WallpaperItem> GOTHIC_WALLPAPER = registerWallpaper("gothic_wallpaper");

    public static final DeferredItem<WainscotingItem> WAINSCOTING_ITEM = ITEMS.registerItem(
            "wainscoting",
            WainscotingItem::new,
            new Item.Properties()
    );

    public static final DeferredItem<Item> QUICKLIME_POWDER = ITEMS.registerSimpleItem(
            "quicklime_powder",
            new Item.Properties()
    );

    public static final DeferredItem<BucketOfPlasterItem> BUCKET_OF_PLASTER = ITEMS.registerItem(
            "bucket_of_plaster",
            BucketOfPlasterItem::new,
            new Item.Properties().durability(BucketOfPlasterItem.APPLICATIONS)
    );

    public static final DeferredItem<DoorKeyItem> DOOR_KEY = ITEMS.registerItem(
            "door_key",
            DoorKeyItem::new,
            new Item.Properties()
    );

    public static final DeferredItem<TallDoorItem> TALL_MANGROVE_DOOR_ITEM = ITEMS.register(
            "tall_mangrove_door",
            (key) -> new TallDoorItem(ModBlocks.TALL_MANGROVE_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static final DeferredItem<BlockItem> TALL_MANGROVE_SIDELIGHT_ITEM =
            ITEMS.registerSimpleBlockItem("tall_mangrove_sidelight", ModBlocks.TALL_MANGROVE_SIDELIGHT);

    public static final DeferredItem<BlockItem> KEY_CUTTER_ITEM = ITEMS.registerSimpleBlockItem("key_cutter", ModBlocks.KEY_CUTTER);
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
    public static final DeferredItem<BlockItem> CORNER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("corner_block", ModBlocks.CORNER_BLOCK);

    public static final DeferredItem<BlockItem> COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("copper_wall_sconce", ModBlocks.COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("exposed_copper_wall_sconce", ModBlocks.EXPOSED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("weathered_copper_wall_sconce", ModBlocks.WEATHERED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("oxidized_copper_wall_sconce", ModBlocks.OXIDIZED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WAXED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("waxed_copper_wall_sconce", ModBlocks.WAXED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_wall_sconce", ModBlocks.WAXED_EXPOSED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_wall_sconce", ModBlocks.WAXED_WEATHERED_COPPER_WALL_SCONCE);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_WALL_SCONCE = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_wall_sconce", ModBlocks.WAXED_OXIDIZED_COPPER_WALL_SCONCE);

    public static final DeferredItem<BlockItem> COPPER_CHAIN = ITEMS.registerSimpleBlockItem("copper_chain", ModBlocks.COPPER_CHAIN);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_CHAIN = ITEMS.registerSimpleBlockItem("exposed_copper_chain", ModBlocks.EXPOSED_COPPER_CHAIN);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_CHAIN = ITEMS.registerSimpleBlockItem("weathered_copper_chain", ModBlocks.WEATHERED_COPPER_CHAIN);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_CHAIN = ITEMS.registerSimpleBlockItem("oxidized_copper_chain", ModBlocks.OXIDIZED_COPPER_CHAIN);
    public static final DeferredItem<BlockItem> WAXED_COPPER_CHAIN = ITEMS.registerSimpleBlockItem("waxed_copper_chain", ModBlocks.WAXED_COPPER_CHAIN);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_CHAIN = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_chain", ModBlocks.WAXED_EXPOSED_COPPER_CHAIN);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_CHAIN = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_chain", ModBlocks.WAXED_WEATHERED_COPPER_CHAIN);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_CHAIN = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_chain", ModBlocks.WAXED_OXIDIZED_COPPER_CHAIN);
    public static final DeferredItem<BlockItem> COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("copper_chandelier", ModBlocks.COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> EXPOSED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("exposed_copper_chandelier", ModBlocks.EXPOSED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WEATHERED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("weathered_copper_chandelier", ModBlocks.WEATHERED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> OXIDIZED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("oxidized_copper_chandelier", ModBlocks.OXIDIZED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WAXED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("waxed_copper_chandelier", ModBlocks.WAXED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("waxed_exposed_copper_chandelier", ModBlocks.WAXED_EXPOSED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("waxed_weathered_copper_chandelier", ModBlocks.WAXED_WEATHERED_COPPER_CHANDELIER);
    public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_CHANDELIER = ITEMS.registerSimpleBlockItem("waxed_oxidized_copper_chandelier", ModBlocks.WAXED_OXIDIZED_COPPER_CHANDELIER);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.addListener(ModItems::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DOOR_KEY);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(COPPER_CHAIN);
            event.accept(EXPOSED_COPPER_CHAIN);
            event.accept(WEATHERED_COPPER_CHAIN);
            event.accept(OXIDIZED_COPPER_CHAIN);
            event.accept(WAXED_COPPER_CHAIN);
            event.accept(WAXED_EXPOSED_COPPER_CHAIN);
            event.accept(WAXED_WEATHERED_COPPER_CHAIN);
            event.accept(WAXED_OXIDIZED_COPPER_CHAIN);
            event.accept(COPPER_CHANDELIER);
            event.accept(EXPOSED_COPPER_CHANDELIER);
            event.accept(WEATHERED_COPPER_CHANDELIER);
            event.accept(OXIDIZED_COPPER_CHANDELIER);
            event.accept(WAXED_COPPER_CHANDELIER);
            event.accept(WAXED_EXPOSED_COPPER_CHANDELIER);
            event.accept(WAXED_WEATHERED_COPPER_CHANDELIER);
            event.accept(WAXED_OXIDIZED_COPPER_CHANDELIER);
            event.accept(COPPER_WALL_SCONCE);
            event.accept(EXPOSED_COPPER_WALL_SCONCE);
            event.accept(WEATHERED_COPPER_WALL_SCONCE);
            event.accept(OXIDIZED_COPPER_WALL_SCONCE);
            event.accept(WAXED_COPPER_WALL_SCONCE);
            event.accept(WAXED_EXPOSED_COPPER_WALL_SCONCE);
            event.accept(WAXED_WEATHERED_COPPER_WALL_SCONCE);
            event.accept(WAXED_OXIDIZED_COPPER_WALL_SCONCE);
            event.accept(CORNFLOWER_WALLPAPER);
            event.accept(SCALLOPED_WALLPAPER);
            event.accept(OGEE_LATTICE_WALLPAPER);
            event.accept(STRIPED_WALLPAPER);
            event.accept(GOTHIC_WALLPAPER);
            event.accept(WAINSCOTING_ITEM);
            event.accept(BUCKET_OF_PLASTER);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(COPPER_NUGGET);
            event.accept(QUICKLIME_POWDER);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            BASIC_BLOCK_ITEMS.forEach(event::accept);
            event.accept(OAK_RAILING);
            event.accept(OAK_BALUSTRADE);
            event.accept(OAK_BALUSTER);
            event.accept(OAK_NEWEL_POST);
            WOOD_RAILINGS.forEach(event::accept);
            event.accept(TALL_MANGROVE_DOOR_ITEM);
            event.accept(TALL_MANGROVE_SIDELIGHT_ITEM);
            event.accept(KEY_CUTTER_ITEM);
            event.accept(CASING_ITEM);
            event.accept(WINDOW_APRON_ITEM);
            event.accept(CROWN_MOULDING_ITEM);
            event.accept(BASEBOARD_ITEM);
            event.accept(PLINTH_BLOCK_ITEM);
            event.accept(CORNER_BLOCK_ITEM);
            event.accept(ELEVATOR_SHAFT_PANEL_ITEM);
            event.accept(ELEVATOR_FLOOR_ITEM);
            event.accept(ELEVATOR_DOOR_ITEM);
            event.accept(ELEVATOR_CALL_BUTTON_ITEM);
            event.accept(ELEVATOR_DESTINATION_CONTROL_ITEM);
            event.accept(BRUSHED_STAINLESS_STEEL_ITEM);
        }
    }

    private static DeferredItem<WallpaperItem> registerWallpaper(String name) {
        return ITEMS.registerItem(
                name,
                properties -> new WallpaperItem(
                        properties,
                        ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "block/" + name)
                ),
                new Item.Properties()
        );
    }
}
