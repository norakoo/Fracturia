package com.norako.fracturia.item;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.FracturiaEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FracturiaItems
{
    // OVERWORLD

    public static final Item RAW_MAGMITITE = registerItem("raw_magmitite", new Item(new Item.Settings()));
    public static final Item MAGMITITE_INGOT = registerItem("magmitite_ingot", new Item(new Item.Settings()));
    public static final Item RAW_XYOPHITE = registerItem("raw_xyophite", new Item(new Item.Settings()));
    public static final Item XYOPHITE_INGOT = registerItem("xyophite_ingot", new Item(new Item.Settings()));
    public static final Item BELITE_INGOT = registerItem("belite_ingot", new Item(new Item.Settings()));
    public static final Item AVIDITE_INGOT = registerItem("avidite_ingot", new Item(new Item.Settings()));
    public static final Item MOUNTAINEER_SPAWN_EGG = registerItem("mountaineer_spawn_egg", new SpawnEggItem(FracturiaEntities.MOUNTAINEER_ENTITY, 0x959B9B, 0xD9F2F2, new Item.Settings()));
    public static final Item WINDCALLER_SPAWN_EGG = registerItem("windcaller_spawn_egg", new SpawnEggItem(FracturiaEntities.WINDCALLER_ENTITY, 0x959B9B, 0xD07444, new Item.Settings()));
    public static final Item SQUALL_GOLEM_SPAWN_EGG = registerItem("squall_golem_spawn_egg", new SpawnEggItem(FracturiaEntities.SQUALL_GOLEM_ENTITY, 0x959B9B, 0xFFA300, new Item.Settings()));
    public static final Item ILLUSIONER_SPAWN_EGG = registerItem("illusioner_spawn_egg", new SpawnEggItem(FracturiaEntities.ILLUSIONER_ENTITY, 0x959B9B, 0x50344D, new Item.Settings()));

    // NETHER

    // END

    // ARKONIA

    // BIRUNIA

    // ICEOLIA

    // IRIDIA

    // LUMINIA

    // STELLARIA

    // VELIA

    // ZYPHIA

    // XYLOHIA

    // FRACTURIA

    private static Item registerItem(String name, Item item)
    {
        return Registry.register(Registries.ITEM, Identifier.of(Fracturia.MOD_ID, name), item);
    }

    public static void registerFracturiaItems()
    {
        Fracturia.LOGGER.info("Registering Fracturia Items for " + Fracturia.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(MAGMITITE_INGOT);
            fabricItemGroupEntries.add(RAW_MAGMITITE);
            fabricItemGroupEntries.add(RAW_XYOPHITE);
            fabricItemGroupEntries.add(XYOPHITE_INGOT);
            fabricItemGroupEntries.add(BELITE_INGOT);
            fabricItemGroupEntries.add(AVIDITE_INGOT);
        });
    }
}
