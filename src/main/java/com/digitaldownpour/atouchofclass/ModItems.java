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

    public static DeferredItem<TallDoorItem> TALL_DOOR_ITEM = ITEMS.register(
            "tall_door",
            (key) -> new TallDoorItem(ModBlocks.TALL_DOOR.get(),
                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())
    );

    public static DeferredItem<BlockItem> TALL_SIDELIGHT_ITEM = ITEMS.registerSimpleBlockItem("tall_sidelight", ModBlocks.TALL_SIDELIGHT);

    public static final DeferredItem<BlockItem> KEY_CUTTER_ITEM = ITEMS.registerSimpleBlockItem("key_cutter", ModBlocks.KEY_CUTTER);
    public static final DeferredItem<BlockItem> CASING_ITEM = ITEMS.registerSimpleBlockItem("casing", ModBlocks.CASING);
    public static final DeferredItem<BlockItem> PLINTH_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("plinth_block", ModBlocks.PLINTH_BLOCK);
    public static final DeferredItem<BlockItem> CORNER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("corner_block", ModBlocks.CORNER_BLOCK);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.addListener(ModItems::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DOOR_KEY);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(CORNFLOWER_WALLPAPER);
            event.accept(SCALLOPED_WALLPAPER);
            event.accept(OGEE_LATTICE_WALLPAPER);
            event.accept(STRIPED_WALLPAPER);
            event.accept(GOTHIC_WALLPAPER);
            event.accept(WAINSCOTING_ITEM);
            event.accept(BUCKET_OF_PLASTER);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(QUICKLIME_POWDER);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(TALL_DOOR_ITEM);
            event.accept(TALL_SIDELIGHT_ITEM);
            event.accept(KEY_CUTTER_ITEM);
            event.accept(CASING_ITEM);
            event.accept(PLINTH_BLOCK_ITEM);
            event.accept(CORNER_BLOCK_ITEM);
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
