package com.norako.fracturia.datagen;

import com.norako.fracturia.block.FracturiaBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class FracturiaBlockTagProvider extends FabricTagProvider.BlockTagProvider
{
    public FracturiaBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture)
    {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup)
    {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE)
                .add(FracturiaBlocks.DEEPSLATE_XYOPHITE_ORE)
                .add(FracturiaBlocks.DEEPSLATE_BELITE_ORE)
                .add(FracturiaBlocks.DEEPSLATE_AVIDITE_ORE);

        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
                .add(FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE)
                .add(FracturiaBlocks.DEEPSLATE_XYOPHITE_ORE)
                .add(FracturiaBlocks.DEEPSLATE_BELITE_ORE)
                .add(FracturiaBlocks.DEEPSLATE_AVIDITE_ORE);
    }
}
