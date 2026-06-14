package com.norako.fracturia.entity.custom.overworld.illagers;

import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.UUID;

public class IllusionerCloneEntity extends RaiderEntity implements GeoEntity
{
    private static final TrackedData<Integer> ATTACK_STATE =
            DataTracker.registerData(IllusionerCloneEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private UUID parentUuid;
    private int checkParentTimer = 0;
    private int attackAnimTimer = 0;
    private int lastAttackState = 0;

    public IllusionerCloneEntity(EntityType<? extends RaiderEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder)
    {
        super.initDataTracker(builder);
        builder.add(ATTACK_STATE, 0);
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return RaiderEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D);
    }

    public void setParentUuid(UUID uuid)
    {
        this.parentUuid = uuid;
    }

    @Override
    protected void initGoals()
    {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new ShootArrowGoal(this));
        this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.targetSelector.add(1, new RevengeGoal(this, RaiderEntity.class).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public void tick()
    {
        super.tick();
        if (attackAnimTimer > 0) {
            attackAnimTimer--;
            if (attackAnimTimer == 0) {
                this.dataTracker.set(ATTACK_STATE, 0);
            }
        }
        checkParentTimer++;
        if (checkParentTimer >= 40) {
            checkParentTimer = 0;
            if (parentUuid != null && this.getWorld() instanceof ServerWorld serverWorld) {
                var parent = serverWorld.getEntity(parentUuid);
                if (parent == null || !parent.isAlive()) {
                    this.discard();
                }
            }
        }
    }

    // --- Shoot arrow goal (left arm) ---

    class ShootArrowGoal extends Goal
    {
        // Adjust to the tick in the animation where the arrow releases
        private static final int SHOOT_TICK = 15;

        private final IllusionerCloneEntity clone;
        private int timer = 0;
        private int cooldown = 0;
        private boolean shot = false;

        ShootArrowGoal(IllusionerCloneEntity entity)
        {
            this.clone = entity;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart()
        {
            if (cooldown > 0) { cooldown--; return false; }
            LivingEntity target = clone.getTarget();
            return target != null && clone.squaredDistanceTo(target) <= 15.0 * 15.0;
        }

        @Override
        public boolean shouldContinue()
        {
            return !shot && clone.getTarget() != null;
        }

        @Override
        public void start()
        {
            timer = 0;
            shot = false;
            clone.dataTracker.set(ATTACK_STATE, 1);
            clone.attackAnimTimer = 5;
        }

        @Override
        public void stop()
        {
            cooldown = 20;
        }

        @Override
        public void tick()
        {
            LivingEntity target = clone.getTarget();
            if (target != null) clone.getLookControl().lookAt(target, 30.0F, 30.0F);
            timer++;
            if (timer >= SHOOT_TICK && !shot && target != null) {
                shot = true;
                if (clone.getWorld() instanceof ServerWorld) {
                    ItemStack bow = new ItemStack(Items.BOW);
                    ArrowEntity arrow = new ArrowEntity(clone.getWorld(), clone, bow, null);
                    double dx = target.getX() - clone.getX();
                    double dy = target.getEyeY() - arrow.getY() - 0.1;
                    double dz = target.getZ() - clone.getZ();
                    double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                    arrow.setVelocity(dx, dy + horizontalDist * 0.2, dz, 1.6F, 14 - clone.getWorld().getDifficulty().getId() * 4);
                    clone.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (clone.getRandom().nextFloat() * 0.4F + 0.8F));
                    clone.getWorld().spawnEntity(arrow);
                }
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount)
    {
        net.minecraft.entity.Entity attacker = source.getAttacker();
        if (attacker instanceof IllusionerEntity || attacker instanceof IllusionerCloneEntity) return false;
        return super.damage(source, amount);
    }

    @Override
    public void addBonusForWave(ServerWorld world, int wave, boolean unused) {}

    @Override
    public SoundEvent getCelebratingSound() { return SoundEvents.ENTITY_EVOKER_CELEBRATE; }

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
        return FracturiaSounds.ILLUSIONER_CLONE_DEATH;
    }

    // --- GeckoLib ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }

    private PlayState attackPredicate(AnimationState<IllusionerCloneEntity> state)
    {
        int attackState = this.dataTracker.get(ATTACK_STATE);
        if (attackState != lastAttackState) {
            lastAttackState = attackState;
            if (attackState == 1) {
                AnimationController<IllusionerCloneEntity> controller = state.getController();
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.illusioner.attack_left", Animation.LoopType.PLAY_ONCE));
            }
        }
        return PlayState.CONTINUE;
    }

    private PlayState predicate(AnimationState<IllusionerCloneEntity> state)
    {
        AnimationController<IllusionerCloneEntity> controller = state.getController();
        if (state.isMoving()) {
            controller.setAnimation(RawAnimation.begin().then("animation.illusioner.walk", Animation.LoopType.LOOP));
        } else {
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
