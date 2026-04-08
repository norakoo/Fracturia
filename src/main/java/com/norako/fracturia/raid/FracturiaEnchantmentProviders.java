package com.norako.fracturia.raid;

import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class FracturiaEnchantmentProviders
{
    public static final RegistryKey<EnchantmentProvider> MOUNTAINEER = RegistryKey.of(RegistryKeys.ENCHANTMENT_PROVIDER, Identifier.of("fracturia", "mountaineer"));
    public static final RegistryKey<EnchantmentProvider> MOUNTAINEER_POST_WAVE_5 = RegistryKey.of(RegistryKeys.ENCHANTMENT_PROVIDER, Identifier.of("fracturia", "mountaineer_post_wave_5"));
    public static final RegistryKey<EnchantmentProvider> WINDCALLER = RegistryKey.of(RegistryKeys.ENCHANTMENT_PROVIDER, Identifier.of("fracturia", "windcaller"));
    public static final RegistryKey<EnchantmentProvider> WINDCALLER_POST_WAVE_5 = RegistryKey.of(RegistryKeys.ENCHANTMENT_PROVIDER, Identifier.of("fracturia", "windcaller_post_wave_5"));
}
