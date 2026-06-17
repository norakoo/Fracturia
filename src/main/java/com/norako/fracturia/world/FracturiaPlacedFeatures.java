package com.norako.fracturia.world;

import com.norako.fracturia.Fracturia;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.List;

public class FracturiaPlacedFeatures {

    public static final RegistryKey<PlacedFeature> MAGMITITE_ORE_PLACED_KEY = key("magmitite_ore");
    public static final RegistryKey<PlacedFeature> XYOPHITE_ORE_PLACED_KEY = key("xyophite_ore");
    public static final RegistryKey<PlacedFeature> BELITE_ORE_PLACED_KEY = key("belite_ore");
    public static final RegistryKey<PlacedFeature> AVIDITE_ORE_PLACED_KEY = key("avidite_ore");
    public static final RegistryKey<PlacedFeature> ONYX_ORE_PLACED_KEY = key("onyx_ore");
    public static final RegistryKey<PlacedFeature> ENDERLITE_PLACED_KEY = key("enderlite");

    public static void bootstrap(Registerable<PlacedFeature> context) {
        var configured = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        // Overworld — deep only (y -64 à -16)
        register(context, MAGMITITE_ORE_PLACED_KEY,
                configured.getOrThrow(FracturiaConfiguredFeatures.MAGMITITE_ORE_KEY),
                modifiers(3, YOffset.fixed(-64), YOffset.fixed(-16)));

        register(context, XYOPHITE_ORE_PLACED_KEY,
                configured.getOrThrow(FracturiaConfiguredFeatures.XYOPHITE_ORE_KEY),
                modifiers(3, YOffset.fixed(-64), YOffset.fixed(-16)));

        register(context, BELITE_ORE_PLACED_KEY,
                configured.getOrThrow(FracturiaConfiguredFeatures.BELITE_ORE_KEY),
                modifiers(2, YOffset.fixed(-64), YOffset.fixed(-32)));

        register(context, AVIDITE_ORE_PLACED_KEY,
                configured.getOrThrow(FracturiaConfiguredFeatures.AVIDITE_ORE_KEY),
                modifiers(2, YOffset.fixed(-64), YOffset.fixed(-32)));

        // Nether
        register(context, ONYX_ORE_PLACED_KEY,
                configured.getOrThrow(FracturiaConfiguredFeatures.ONYX_ORE_KEY),
                modifiers(6, YOffset.fixed(10), YOffset.fixed(120)));

        // End
        register(context, ENDERLITE_PLACED_KEY,
                configured.getOrThrow(FracturiaConfiguredFeatures.ENDERLITE_KEY),
                modifiers(4, YOffset.fixed(0), YOffset.fixed(60)));
    }

    private static List<PlacementModifier> modifiers(int count, YOffset min, YOffset max) {
        return List.of(
                CountPlacementModifier.of(count),
                SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(min, max),
                BiomePlacementModifier.of()
        );
    }

    private static void register(Registerable<PlacedFeature> context,
                                 RegistryKey<PlacedFeature> key,
                                 RegistryEntry<ConfiguredFeature<?, ?>> feature,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(feature, modifiers));
    }

    private static RegistryKey<PlacedFeature> key(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Fracturia.MOD_ID, name));
    }
}
