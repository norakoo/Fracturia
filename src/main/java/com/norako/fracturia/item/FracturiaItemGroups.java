package com.norako.fracturia.item;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.block.FracturiaBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FracturiaItemGroups
{
    public static final ItemGroup FRACTURIA_VANILLA_ITEMS = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Fracturia.MOD_ID, "fracturia_vanilla_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(FracturiaItems.RAW_MAGMITITE))
                    .displayName(Text.translatable("itemgroup.fracturia.fracturia_vanilla_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(FracturiaItems.RAW_MAGMITITE);
                        entries.add(FracturiaItems.MAGMITITE_INGOT);
                        entries.add(FracturiaItems.RAW_XYOPHITE);
                        entries.add(FracturiaItems.XYOPHITE_INGOT);
                        entries.add(FracturiaItems.BELITE_INGOT);
                        entries.add(FracturiaItems.AVIDITE_INGOT);
                    }).build());

    public static final ItemGroup FRACTURIA_VANILLA_BLOCKS = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Fracturia.MOD_ID, "fracturia_vanilla_blocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE))
                    .displayName(Text.translatable("itemgroup.fracturia.fracturia_vanilla_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE);
                        entries.add(FracturiaBlocks.DEEPSLATE_XYOPHITE_ORE);
                        entries.add(FracturiaBlocks.DEEPSLATE_BELITE_ORE);
                        entries.add(FracturiaBlocks.DEEPSLATE_AVIDITE_ORE);
                        entries.add(FracturiaBlocks.NETHER_ONYX_ORE);
                    }).build());

    public static void registerItemGroups()
    {
        Fracturia.LOGGER.info("Registering Item Groups for " + Fracturia.MOD_ID);
    }
}
