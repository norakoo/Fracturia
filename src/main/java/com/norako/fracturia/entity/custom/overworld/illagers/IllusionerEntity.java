package com.norako.fracturia.entity.custom.overworld.illagers;

import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class IllusionerEntity extends net.minecraft.entity.mob.IllusionerEntity implements GeoEntity
{
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Flag client-side pour forcer la visibilité lors du rendu des copies fantômes
    public transient boolean renderingAsPhantom = false;

    @Override
    public boolean isInvisible()
    {
        if (renderingAsPhantom) return false;
        return super.isInvisible();
    }

    public IllusionerEntity(EntityType<? extends net.minecraft.entity.mob.IllusionerEntity> entityType, World world)
    {
        super(entityType, world);
    }

    // --- Attributes ---

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return RaiderEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 32.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D);
    }

    // --- Sounds ---

    @Override
    protected SoundEvent getAmbientSound()
    {
        return FracturiaSounds.ILLUSIONER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return FracturiaSounds.ILLUSIONER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return FracturiaSounds.ILLUSIONER_DEATH;
    }

    // --- Raids ---

    @Override
    public void addBonusForWave(ServerWorld world, int wave, boolean unused)
    {
        super.addBonusForWave(world, wave, unused);
    }

    // --- GeckoLib ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }

    private PlayState attackPredicate(AnimationState<IllusionerEntity> state)
    {
        AnimationController<IllusionerEntity> controller = state.getController();

        if (this.handSwinging && (controller.getAnimationState() == AnimationController.State.STOPPED ||
                controller.getAnimationState() == AnimationController.State.RUNNING))
        {
            controller.forceAnimationReset();
            controller.setAnimation(RawAnimation.begin().then("animation.illusioner.attack", Animation.LoopType.PLAY_ONCE));
            this.handSwinging = false;
        }

        return PlayState.CONTINUE;
    }

    private PlayState predicate(AnimationState<IllusionerEntity> state)
    {
        AnimationController<IllusionerEntity> controller = state.getController();

        if (this.isSpellcasting())
        {
            controller.setAnimation(RawAnimation.begin().then("animation.illusioner.spellcasting", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        if (this.isCelebrating())
        {
            controller.setAnimation(RawAnimation.begin().then("animation.illusioner.celebrate", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        if (state.isMoving())
        {
            controller.setAnimation(RawAnimation.begin().then("animation.illusioner.walk", Animation.LoopType.LOOP));
        }
        else
        {
            controller.setAnimation(RawAnimation.begin().then("animation.illusioner.idle", Animation.LoopType.LOOP));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return cache;
    }
}
