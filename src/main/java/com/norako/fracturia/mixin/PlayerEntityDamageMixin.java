package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.FracturiaDifficulty;
import com.norako.fracturia.difficulty.FracturiaDifficultyState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityDamageMixin {

    @Unique
    private boolean fracturia_scalingDamage = false;

    // Scales all mob-sourced damage (melee + projectiles) before armor calculation
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void fracturia_scaleMobDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (player.getWorld().isClient()) return;
        if (fracturia_scalingDamage) return;

        Entity attacker = source.getAttacker();
        if (!(attacker instanceof MobEntity) || attacker instanceof PlayerEntity) return;

        FracturiaDifficulty diff = FracturiaDifficultyState.activeServerDifficulty;
        if (!diff.isActive()) return;

        fracturia_scalingDamage = true;
        boolean result = player.damage(source, amount * diff.getDamageMultiplier());
        fracturia_scalingDamage = false;

        cir.setReturnValue(result);
        cir.cancel();
    }
}
