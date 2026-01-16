package com.norako.fracturia.datagen;

import com.norako.fracturia.block.FracturiaBlocks;
import com.norako.fracturia.item.FracturiaItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class FracturiaLootTableProvider extends FabricBlockLootTableProvider
{
    public FracturiaLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup)
    {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate()
    {
        addDrop(FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE, oreDrops(FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE, FracturiaItems.RAW_MAGMITITE));
        addDrop(FracturiaBlocks.DEEPSLATE_XYOPHITE_ORE, oreDrops(FracturiaBlocks.DEEPSLATE_XYOPHITE_ORE, FracturiaItems.RAW_XYOPHITE));
        addDrop(FracturiaBlocks.DEEPSLATE_BELITE_ORE, oreDrops(FracturiaBlocks.DEEPSLATE_BELITE_ORE, FracturiaItems.BELITE_INGOT));
        addDrop(FracturiaBlocks.DEEPSLATE_AVIDITE_ORE, oreDrops(FracturiaBlocks.DEEPSLATE_AVIDITE_ORE, FracturiaItems.AVIDITE_INGOT));
    }
}
