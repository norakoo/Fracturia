package com.norako.fracturia;

import com.norako.fracturia.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

public class FracturiaDataGenerator implements DataGeneratorEntrypoint
{
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
    {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(FracturiaBlockTagProvider::new);
        pack.addProvider(FracturiaItemTagProvider::new);
        pack.addProvider(FracturiaLootTableProvider::new);
        pack.addProvider(FracturiaModelProvider::new);
        pack.addProvider(FracturiaRecipeProvider::new);
	}

    @Override
    public void buildRegistry(net.minecraft.registry.RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(net.minecraft.registry.RegistryKeys.CONFIGURED_FEATURE,
                com.norako.fracturia.world.FracturiaConfiguredFeatures::bootstrap);
        registryBuilder.addRegistry(net.minecraft.registry.RegistryKeys.PLACED_FEATURE,
                com.norako.fracturia.world.FracturiaPlacedFeatures::bootstrap);
    }
}
