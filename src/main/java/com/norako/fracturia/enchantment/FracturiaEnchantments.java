package com.norako.fracturia.enchantment;

import com.norako.fracturia.Fracturia;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class FracturiaEnchantments {

    public static final RegistryKey<Enchantment> VOID_TEMPERING =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(Fracturia.MOD_ID, "void_tempering"));
}
