package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.FracturiaDifficulty;
import com.norako.fracturia.difficulty.FracturiaDifficultyState;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreFeature.class)
public class OreFeatureMixin {

    // Randomly skips ore vein placement based on difficulty ore multiplier:
    // DEMENTED (x0.5): 50% of veins skipped
    // INCONCEIVABLE (x0.25): 75% of veins skipped
    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void fracturia_reduceOreFrequency(
        FeatureContext<OreFeatureConfig> context, CallbackInfoReturnable<Boolean> cir
    ) {
        FracturiaDifficulty diff = FracturiaDifficultyState.activeServerDifficulty;
        if (!diff.isActive()) return;

        float skipChance = 1.0f - diff.getOreMultiplier();
        if (context.getRandom().nextFloat() < skipChance) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
