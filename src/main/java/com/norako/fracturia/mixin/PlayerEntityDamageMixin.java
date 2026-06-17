package com.norako.fracturia.mixin;

import com.norako.fracturia.FracturiaAttachments;
import com.norako.fracturia.difficulty.FracturiaDifficulty;
import com.norako.fracturia.difficulty.FracturiaDifficultyState;
import com.norako.fracturia.effect.FracturiaEffects;
import com.norako.fracturia.enchantment.FracturiaEnchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityDamageMixin {

    @Unique
    private boolean fracturia_scalingDamage = false;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void fracturia_scaleMobDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        if (player.getWorld().isClient()) return;
        if (fracturia_scalingDamage) return;

        Entity attacker = source.getAttacker();
        if (!(attacker instanceof MobEntity) || attacker instanceof PlayerEntity) return;

        Integer voidedLevel = ((LivingEntity)(Object)this).getAttached(FracturiaAttachments.VOIDED_LEVEL);
        int level = (voidedLevel != null) ? voidedLevel : 0;

        if (level > 0) {
            boolean isEndMob = attacker instanceof EndermanEntity
                    || attacker instanceof EndermiteEntity
                    || attacker instanceof ShulkerEntity
                    || attacker instanceof EnderDragonEntity;
            if (isEndMob) {
                fracturia_scalingDamage = true;
                boolean result = player.damage(source, amount * level);
                fracturia_scalingDamage = false;
                cir.setReturnValue(result);
                cir.cancel();
                return;
            }
        }

        var enchantEntry = player.getWorld().getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(FracturiaEnchantments.VOID_TEMPERING);
        int enchantLevel = enchantEntry
                .map(e -> net.minecraft.enchantment.EnchantmentHelper.getEquipmentLevel(e, player))
                .orElse(0);
        if (enchantLevel > 0) {
            level = Math.max(1, (int)(Math.sqrt(level) / enchantLevel));
        }

        // Fracturia difficulty — all mob damage multiplier
        FracturiaDifficulty diff = FracturiaDifficultyState.activeServerDifficulty;
        if (!diff.isActive()) return;

        fracturia_scalingDamage = true;
        boolean result = player.damage(source, amount * diff.getDamageMultiplier());
        fracturia_scalingDamage = false;

        cir.setReturnValue(result);
        cir.cancel();
    }
}