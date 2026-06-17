package com.norako.fracturia.mixin;

import com.norako.fracturia.effect.FracturiaEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.effect.StatusEffectInstance")
public class StatusEffectInstanceMixin {

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void fracturia_voidedCustomName(CallbackInfoReturnable<Text> cir) {
        StatusEffectInstance self = (StatusEffectInstance)(Object)this;
        if (self.getEffectType().equals(FracturiaEffects.VOIDED)) {
            cir.setReturnValue(Text.literal("Voided x" + (self.getAmplifier() + 1)));
        }
    }
}
