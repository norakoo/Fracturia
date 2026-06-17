package com.norako.fracturia.world;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.block.FracturiaBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;

import java.util.List;

public class FracturiaConfiguredFeatures {

    public static final RegistryKey<ConfiguredFeature<?, ?>> MAGMITITE_ORE_KEY = key("magmitite_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> XYOPHITE_ORE_KEY = key("xyophite_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> BELITE_ORE_KEY = key("belite_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> AVIDITE_ORE_KEY = key("avidite_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ONYX_ORE_KEY = key("onyx_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ENDERLITE_KEY = key("enderlite");

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        var deepslateRule = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        var netherRule = new TagMatchRuleTest(BlockTags.NETHER_CARVER_REPLACEABLES);
        var endStoneRule = new BlockMatchRuleTest(Blocks.END_STONE);

        context.register(MAGMITITE_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(List.of(OreFeatureConfig.createTarget(deepslateRule, FracturiaBlocks.DEEPSLATE_MAGMITITE_ORE.getDefaultState())), 4)));

        context.register(XYOPHITE_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(List.of(OreFeatureConfig.createTarget(deepslateRule, FracturiaBlocks.DEEPSLATE_XYOPHITE_ORE.getDefaultState())), 4)));

        context.register(BELITE_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(List.of(OreFeatureConfig.createTarget(deepslateRule, FracturiaBlocks.DEEPSLATE_BELITE_ORE.getDefaultState())), 4)));

        context.register(AVIDITE_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(List.of(OreFeatureConfig.createTarget(deepslateRule, FracturiaBlocks.DEEPSLATE_AVIDITE_ORE.getDefaultState())), 4)));

        context.register(ONYX_ORE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(List.of(OreFeatureConfig.createTarget(netherRule, FracturiaBlocks.NETHER_ONYX_ORE.getDefaultState())), 6)));

        context.register(ENDERLITE_KEY, new ConfiguredFeature<>(Feature.ORE,
                new OreFeatureConfig(List.of(OreFeatureConfig.createTarget(endStoneRule, FracturiaBlocks.ENDERLITE.getDefaultState())), 3)));
    }

    private static RegistryKey<ConfiguredFeature<? ,?>> key(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(Fracturia.MOD_ID, name));
    }
}
