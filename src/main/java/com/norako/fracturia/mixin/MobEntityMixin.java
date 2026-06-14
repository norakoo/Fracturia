package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.Difficulty;
import com.norako.fracturia.difficulty.DifficultyState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {

    @Inject(method = "initialize", at = @At("RETURN"))
    private void fracturia_scaleMaxHealth(
            ServerWorldAccess world, LocalDifficulty localDifficulty,
            SpawnReason spawnReason, EntityData entityData,
            CallbackInfoReturnable<EntityData> cir
    ) {
        Difficulty diff = DifficultyState.activeServerDifficulty;
        if (!diff.isActive()) return;

        MobEntity mob = (MobEntity)(Object)this;
        EntityAttributeInstance maxHealth = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            double newBase = maxHealth.getBaseValue() * diff.getHpMultiplier();
            maxHealth.setBaseValue(newBase);
            mob.setHealth((float) newBase);
        }
    }
}
