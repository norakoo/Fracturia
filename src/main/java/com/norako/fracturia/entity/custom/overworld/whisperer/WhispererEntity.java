package com.norako.fracturia.entity.custom.overworld.whisperer;

import com.norako.fracturia.entity.FracturiaEntities;
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
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class WhispererEntity extends HostileEntity implements GeoEntity
{
    private static final TrackedData<Integer> ATTACK_STATE =
            DataTracker.registerData(WhispererEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int attackAnimTimer = 0;
    private int lastAttackState = 0;
    int summonCooldown = 0;

    public WhispererEntity(EntityType<? extends HostileEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder)
    {
        super.initDataTracker(builder);
        builder.add(ATTACK_STATE, 0);
    }

    // --- Attributes ---

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D);
    }

    // --- Goals ---

    @Override
    protected void initGoals()
    {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new SummonVineGoal(this));
        this.goalSelector.add(3, new MeleeGoal(this));
        this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    // --- Tick ---

    @Override
    public void tick()
    {
        super.tick();
        if (summonCooldown > 0) summonCooldown--;
        if (attackAnimTimer > 0) {
            attackAnimTimer--;
            if (attackAnimTimer == 0) {
                this.dataTracker.set(ATTACK_STATE, 0);
            }
        }
    }

    // --- Melee goal ---

    class MeleeGoal extends Goal
    {
        // Adjust to the frame in the animation where the hit lands
        private static final int HIT_TICK = 10;
        private static final int REACH = 2;

        private final WhispererEntity whisperer;
        private int timer = 0;
        private int cooldown = 0;
        private boolean hit = false;

        MeleeGoal(WhispererEntity entity)
        {
            this.whisperer = entity;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart()
        {
            if (cooldown > 0) { cooldown--; return false; }
            LivingEntity target = whisperer.getTarget();
            return target != null && whisperer.squaredDistanceTo(target) <= REACH * REACH * 4;
        }

        @Override
        public boolean shouldContinue()
        {
            return !hit && whisperer.getTarget() != null;
        }

        @Override
        public void start()
        {
            timer = 0;
            hit = false;
            whisperer.dataTracker.set(ATTACK_STATE, 1);
            whisperer.attackAnimTimer = 5;
        }

        @Override
        public void stop()
        {
            cooldown = 20;
        }

        @Override
        public void tick()
        {
            LivingEntity target = whisperer.getTarget();
            if (target != null) {
                whisperer.getLookControl().lookAt(target, 30.0F, 30.0F);
                whisperer.getNavigation().startMovingTo(target, 1.0);
            }
            timer++;
            if (timer >= HIT_TICK && !hit && target != null
                    && whisperer.squaredDistanceTo(target) <= REACH * REACH * 4) {
                hit = true;
                if (whisperer.getWorld() instanceof ServerWorld serverWorld) {
                    target.damage(
                        serverWorld.getDamageSources().mobAttack(whisperer),
                        (float) whisperer.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    );
                    whisperer.playSound(FracturiaSounds.WHISPERER_ATTACK);
                }
            }
        }
    }

    // --- Summon vine goal ---

    class SummonVineGoal extends Goal
    {
        // Adjust to the frame in the animation where vines appear
        private static final int SUMMON_TICK = 20;
        private static final int VINE_COUNT = 1;

        private final WhispererEntity whisperer;
        private int timer = 0;
        private boolean summoned = false;

        SummonVineGoal(WhispererEntity entity)
        {
            this.whisperer = entity;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart()
        {
            LivingEntity target = whisperer.getTarget();
            return target != null
                && whisperer.summonCooldown <= 0
                && whisperer.squaredDistanceTo(target) <= 12.0 * 12.0
                && whisperer.getWorld() instanceof ServerWorld;
        }

        @Override
        public boolean shouldContinue() { return !summoned; }

        @Override
        public void start()
        {
            timer = 0;
            summoned = false;
            whisperer.dataTracker.set(ATTACK_STATE, 2);
            whisperer.attackAnimTimer = 5;
            whisperer.playSound(FracturiaSounds.WHISPERER_SUMMON);
        }

        @Override
        public void stop()
        {
            whisperer.summonCooldown = 120;
        }

        @Override
        public void tick()
        {
            LivingEntity target = whisperer.getTarget();
            if (target != null) whisperer.getLookControl().lookAt(target, 30.0F, 30.0F);
            timer++;
            if (timer >= SUMMON_TICK && !summoned && target != null) {
                summoned = true;
                if (!(whisperer.getWorld() instanceof ServerWorld world)) return;

                for (int i = 0; i < VINE_COUNT; i++) {
                    float angle = whisperer.random.nextFloat() * (float) (Math.PI * 2);
                    float dist = 1.0f + whisperer.random.nextFloat() * 4.0f; // 1-5 blocks from whisperer
                    double x = whisperer.getX() + Math.cos(angle) * dist;
                    double z = whisperer.getZ() + Math.sin(angle) * dist;

                    PoisonVineEntity vine = new PoisonVineEntity(FracturiaEntities.POISON_VINE_ENTITY, world);
                    vine.refreshPositionAndAngles(x, whisperer.getY(), z, 0, 0);
                    world.spawnEntity(vine);
                }
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount)
    {
        if (source.getAttacker() instanceof PoisonVineEntity) return false;
        return super.damage(source, amount);
    }

    // --- Sounds ---

    @Override
    protected SoundEvent getAmbientSound() { return FracturiaSounds.WHISPERER_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return FracturiaSounds.WHISPERER_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return FracturiaSounds.WHISPERER_DEATH; }

    // --- GeckoLib ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }

    private PlayState attackPredicate(AnimationState<WhispererEntity> state)
    {
        int attackState = this.dataTracker.get(ATTACK_STATE);
        if (attackState != lastAttackState) {
            lastAttackState = attackState;
            AnimationController<WhispererEntity> controller = state.getController();
            if (attackState == 1) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.whisperer.attack", Animation.LoopType.PLAY_ONCE));
            } else if (attackState == 2) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.whisperer.summon", Animation.LoopType.PLAY_ONCE));
            }
        }
        return PlayState.CONTINUE;
    }

    private PlayState predicate(AnimationState<WhispererEntity> state)
    {
        AnimationController<WhispererEntity> controller = state.getController();
        if (state.isMoving()) {
            controller.setAnimation(RawAnimation.begin().then("animation.whisperer.walk", Animation.LoopType.LOOP));
        } else {
            controller.setAnimation(RawAnimation.begin().then("animation.whisperer.idle", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
