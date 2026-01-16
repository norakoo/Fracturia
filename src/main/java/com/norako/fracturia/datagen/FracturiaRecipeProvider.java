package com.norako.fracturia.datagen;

import com.norako.fracturia.block.FracturiaBlocks;
import com.norako.fracturia.item.FracturiaItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FracturiaRecipeProvider extends FabricRecipeProvider
{
    public FracturiaRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture)
    {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter)
    {
        List<ItemConvertible> MAGMITITE_SMELTABLES = List.of(FracturiaItems.RAW_MAGMITITE, FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE);
        List<ItemConvertible> XYOPHITE_SMELTABLES = List.of(FracturiaItems.RAW_XYOPHITE, FracturiaBlocks.DEEPSLATE_XYOPHITE_ORE);
        List<ItemConvertible> BELITE_SMELTABLES = List.of(FracturiaBlocks.DEEPSLATE_BELITE_ORE);
        List<ItemConvertible> AVIDITE_SMELTABLES = List.of(FracturiaBlocks.DEEPSLATE_AVIDITE_ORE);

        offerSmelting(exporter, MAGMITITE_SMELTABLES, RecipeCategory.MISC, FracturiaItems.MAGMITITE_INGOT, 0.25f, 200, "magmitite");
        offerBlasting(exporter, MAGMITITE_SMELTABLES, RecipeCategory.MISC, FracturiaItems.MAGMITITE_INGOT, 0.25f, 100, "magmitite");

        offerSmelting(exporter, XYOPHITE_SMELTABLES, RecipeCategory.MISC, FracturiaItems.XYOPHITE_INGOT, 0.25f, 200, "xyophite");
        offerBlasting(exporter, XYOPHITE_SMELTABLES, RecipeCategory.MISC, FracturiaItems.XYOPHITE_INGOT, 0.25f, 100, "xyophite");

        offerSmelting(exporter, BELITE_SMELTABLES, RecipeCategory.MISC, FracturiaItems.BELITE_INGOT, 0.25f, 200, "belite");
        offerBlasting(exporter, BELITE_SMELTABLES, RecipeCategory.MISC, FracturiaItems.BELITE_INGOT, 0.25f, 100, "belite");

        offerSmelting(exporter, AVIDITE_SMELTABLES, RecipeCategory.MISC, FracturiaItems.AVIDITE_INGOT, 0.25f, 200, "avidite");
        offerBlasting(exporter, AVIDITE_SMELTABLES, RecipeCategory.MISC, FracturiaItems.AVIDITE_INGOT, 0.25f, 100, "avidite");
    }
}
