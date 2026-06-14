package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.Difficulty;
import com.norako.fracturia.difficulty.DifficultyState;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreFeature.class)
public class OreFeatureMixin {

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void fracturia_reduceOreFrequency(
            FeatureContext<OreFeatureConfig> context, CallbackInfoReturnable<Boolean> cir
    ) {
        Difficulty diff = DifficultyState.activeServerDifficulty;
        if (!diff.isActive()) return;

        float skipChance = 1.0f - diff.getOreMultiplier();
        if (context.getRandom().nextFloat() < skipChance) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
