package com.norako.fracturia.effect;

import com.norako.fracturia.Fracturia;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class FracturiaEffects {

    public static final RegistryEntry<StatusEffect> VOIDED = register("voided", new VoidedEffect());

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT,
                Identifier.of(Fracturia.MOD_ID, name), effect);
    }

    public static void registerEffects() {
        Fracturia.LOGGER.info("Registering Facturia Effects for " + Fracturia.MOD_ID);
    }
}
