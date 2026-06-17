package com.norako.fracturia.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class VoidedEffect extends StatusEffect {

    public VoidedEffect() {
        super(StatusEffectCategory.HARMFUL, 0x4B0082);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
