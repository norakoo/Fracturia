package com.norako.fracturia.datagen;

import com.norako.fracturia.block.FracturiaBlocks;
import com.norako.fracturia.item.FracturiaItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;

public class FracturiaModelProvider extends FabricModelProvider
{
    public FracturiaModelProvider(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator)
    {
        blockStateModelGenerator.registerSimpleCubeAll(FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(FracturiaBlocks.DEEPSLATE_XYOPHITE_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(FracturiaBlocks.DEEPSLATE_BELITE_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(FracturiaBlocks.DEEPSLATE_AVIDITE_ORE);
        blockStateModelGenerator.registerParentedItemModel(FracturiaItems.MOUNTAINEER_SPAWN_EGG, ModelIds.getMinecraftNamespacedItem("template_spawn_egg"));
        blockStateModelGenerator.registerParentedItemModel(FracturiaItems.WINDCALLER_SPAWN_EGG, ModelIds.getMinecraftNamespacedItem("template_spawn_egg"));
        blockStateModelGenerator.registerParentedItemModel(FracturiaItems.SQUALL_GOLEM_SPAWN_EGG, ModelIds.getMinecraftNamespacedItem("template_spawn_egg"));
        blockStateModelGenerator.registerParentedItemModel(FracturiaItems.ILLUSIONER_SPAWN_EGG, ModelIds.getMinecraftNamespacedItem("template_spawn_egg"));
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator)
    {
        itemModelGenerator.register(FracturiaItems.RAW_MAGMITITE, Models.GENERATED);
        itemModelGenerator.register(FracturiaItems.RAW_XYOPHITE, Models.GENERATED);
        itemModelGenerator.register(FracturiaItems.MAGMITITE_INGOT, Models.GENERATED);
        itemModelGenerator.register(FracturiaItems.XYOPHITE_INGOT, Models.GENERATED);
        itemModelGenerator.register(FracturiaItems.BELITE_INGOT, Models.GENERATED);
        itemModelGenerator.register(FracturiaItems.AVIDITE_INGOT, Models.GENERATED);
    }
}
