package com.norako.fracturia.entity.custom.overworld.illagers;

import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.NoPenaltyTargeting;
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
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.compress.utils.Lists;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

public class SquallGolemEntity extends RaiderEntity implements GeoEntity
{
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // TrackedData pour sync client/serveur
    private static final TrackedData<Boolean> ACTIVATED =
            DataTracker.registerData(SquallGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ACTIVATION_ANIM_PLAYED =
            DataTracker.registerData(SquallGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private boolean activationAnimPlayed = false;

    public SquallGolemEntity(EntityType<? extends RaiderEntity> entityType, World world)
    {
        super(entityType, world);
    }

    // --- TrackedData init ---

    @Override
    protected void initDataTracker(DataTracker.Builder builder)
    {
        super.initDataTracker(builder);
        builder.add(ACTIVATED, false);
        builder.add(ACTIVATION_ANIM_PLAYED, false);
    }

    public boolean isActivated()
    {
        return this.dataTracker.get(ACTIVATED);
    }

    public void setActivated(boolean activated)
    {
        this.dataTracker.set(ACTIVATED, activated);
    }

    public boolean isActivationAnimPlayed()
    {
        return this.dataTracker.get(ACTIVATION_ANIM_PLAYED);
    }

    public void setActivationAnimPlayed(boolean played)
    {
        this.dataTracker.set(ACTIVATION_ANIM_PLAYED, played);
    }

    // --- NBT ---

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt)
    {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("isActivated", this.isActivated());
        nbt.putBoolean("isActivationAnimPlayed", this.isActivationAnimPlayed());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt)
    {
        super.readCustomDataFromNbt(nbt);
        this.setActivated(nbt.getBoolean("isActivated"));
        this.setActivationAnimPlayed(nbt.getBoolean("isActivationAnimPlayed")); // manquait ça
        if (this.isActivated())
        {
            this.activationAnimPlayed = true;
            this.setActivationAnimPlayed(true);
        }
    }

    // --- Attributes ---

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return RaiderEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 0.1f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12.0);
    }

    // --- Goals ---

    @Override
    protected void initGoals()
    {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new SquallGolemEntity.AttackGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.goalSelector.add(8, new WanderAroundGoal(this, 0.4, 1));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.goalSelector.add(3, new MoveToRaidCenterGoal<>(this));
        this.goalSelector.add(4, new AttackHomeGoal(this, 0.6, 1));
        this.goalSelector.add(5, new CelebrateGoal(this));
    }

    // --- Bloque le mouvement tant que désactivé ---

    @Override
    public void tickMovement()
    {
        if (!this.isActivated() || !this.isActivationAnimPlayed())
        {
            this.getNavigation().stop();
            return;
        }
        super.tickMovement();
    }

    private int activationTickTimer = 0;

    @Override
    public void tick()
    {
        super.tick();

        if (!this.getWorld().isClient())
        {
            if (!this.isActivated())
            {
                PlayerEntity player = this.getWorld().getClosestPlayer(
                        this.getX(), this.getY(), this.getZ(),
                        8.0, true
                );
                if (player != null)
                {
                    this.setActivated(true);
                    this.playSound(FracturiaSounds.SQUALL_GOLEM_POWER_ON);
                }
            }
            else if (!this.isActivationAnimPlayed())
            {
                activationTickTimer++;
                // mets ici la durée en ticks de ton anim activate
                if (activationTickTimer >= 51) // 40 ticks = 2 secondes
                {
                    this.setActivationAnimPlayed(true);
                    activationTickTimer = 0;
                }
            }
        }
    }

    class AttackGoal extends MeleeAttackGoal
    {
        private int attackDelayTimer = 0;
        private int attackCooldown = 0;
        private net.minecraft.entity.LivingEntity pendingTarget = null;

        public AttackGoal(SquallGolemEntity squallGolem)
        {
            super(squallGolem, 0.6, true);
        }

        @Override
        public boolean canStart()
        {
            return SquallGolemEntity.this.isActivated() && attackCooldown <= 0 && super.canStart();
        }

        @Override
        public boolean shouldContinue()
        {
            return super.shouldContinue();
        }

        @Override
        public void tick()
        {
            super.tick();
            if (attackCooldown > 0) attackCooldown--;
            if (attackDelayTimer > 0)
            {
                attackDelayTimer--;
                if (attackDelayTimer == 0 && pendingTarget != null)
                {
                    SquallGolemEntity.this.tryAttack(pendingTarget);
                    pendingTarget = null;
                }
            }
        }

        @Override
        protected void attack(net.minecraft.entity.LivingEntity target)
        {
            if (attackDelayTimer <= 0 && attackCooldown <= 0 && SquallGolemEntity.this.squaredDistanceTo(target) <= 9.0)
            {
                SquallGolemEntity.this.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                SquallGolemEntity.this.playSound(FracturiaSounds.SQUALL_GOLEM_ATTACK);
                attackDelayTimer = 18;
                attackCooldown = 60; // 3 secondes, ajuste si besoin
                pendingTarget = target;
            }
        }
    }

    static class AttackHomeGoal extends Goal
    {
        private final SquallGolemEntity raider;
        private final double speed;
        private BlockPos home;
        private final List<BlockPos> lastHomes = Lists.newArrayList();
        private final int distance;
        private boolean finished;

        public AttackHomeGoal(SquallGolemEntity raider, double speed, int distance)
        {
            this.raider = raider;
            this.speed = speed;
            this.distance = distance;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart()
        {
            if (!this.raider.isActivated()) return false;
            this.purgeMemory();
            return this.isRaiding() && this.tryFindHome() && this.raider.getTarget() == null;
        }

        private boolean isRaiding()
        {
            return this.raider.hasActiveRaid() && !this.raider.getRaid().isFinished();
        }

        private boolean tryFindHome()
        {
            ServerWorld serverWorld = (ServerWorld) this.raider.getWorld();
            BlockPos blockPos = this.raider.getBlockPos();
            Optional<BlockPos> optional = serverWorld.getPointOfInterestStorage().getPosition(registryEntry -> {
                return registryEntry.matchesKey(PointOfInterestTypes.HOME);
            }, this::canLootHome, PointOfInterestStorage.OccupationStatus.ANY, blockPos, 48, this.raider.random);
            if (!optional.isPresent())
            {
                return false;
            } else
            {
                this.home = ((BlockPos) optional.get()).toImmutable();
                return true;
            }
        }

        public boolean shouldContinue()
        {
            if (this.raider.getNavigation().isIdle()) return false;
            return this.raider.getTarget() == null &&
                    !this.home.isWithinDistance(this.raider.getPos(), (double)(this.raider.getWidth() + (float)this.distance)) &&
                    !this.finished;
        }

        public void stop()
        {
            if (this.home.isWithinDistance(this.raider.getPos(), (double)this.distance))
            {
                this.lastHomes.add(this.home);
            }
        }

        public void start()
        {
            super.start();
            this.raider.setDespawnCounter(0);
            this.raider.getNavigation().startMovingTo(
                    (double)this.home.getX(), (double)this.home.getY(), (double)this.home.getZ(), this.speed
            );
            this.finished = false;
        }

        public void tick()
        {
            if (this.raider.getNavigation().isIdle())
            {
                Vec3d vec3d = Vec3d.ofBottomCenter(this.home);
                Vec3d vec3d2 = NoPenaltyTargeting.findTo(this.raider, 16, 7, vec3d, 0.3141592741012573);
                if (vec3d2 == null)
                {
                    vec3d2 = NoPenaltyTargeting.findTo(this.raider, 8, 7, vec3d, 1.5707963705062866);
                }
                if (vec3d2 == null)
                {
                    this.finished = true;
                    return;
                }
                this.raider.getNavigation().startMovingTo(vec3d2.x, vec3d2.y, vec3d2.z, this.speed);
            }
        }

        private boolean canLootHome(BlockPos pos)
        {
            for (BlockPos blockPos : this.lastHomes)
            {
                if (Objects.equals(pos, blockPos)) return false;
            }
            return true;
        }

        private void purgeMemory()
        {
            if (this.lastHomes.size() > 2) this.lastHomes.remove(0);
        }
    }

    public class CelebrateGoal extends Goal
    {
        private final SquallGolemEntity raider;

        CelebrateGoal(SquallGolemEntity raider)
        {
            this.raider = raider;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart()
        {
            if (!this.raider.isActivated()) return false;
            Raid raid = this.raider.getRaid();
            return this.raider.isAlive() && this.raider.getTarget() == null && raid != null && raid.hasLost();
        }

        public void start()
        {
            this.raider.setCelebrating(true);
            super.start();
        }

        public void stop()
        {
            this.raider.setCelebrating(false);
            super.stop();
        }

        public void tick()
        {
            if (!this.raider.isSilent() && this.raider.random.nextInt(this.getTickCount(100)) == 0)
            {
                SquallGolemEntity.this.playSound(
                        SquallGolemEntity.this.getCelebratingSound(),
                        SquallGolemEntity.this.getSoundVolume(),
                        SquallGolemEntity.this.getSoundPitch()
                );
            }
            if (!this.raider.hasVehicle() && this.raider.random.nextInt(this.getTickCount(50)) == 0)
            {
                this.raider.getJumpControl().setActive();
            }
            super.tick();
        }
    }

    // --- Misc ---

    @Override
    public float getStepHeight()
    {
        return 1.0f;
    }

    @Override
    public boolean damage(DamageSource source, float amount)
    {
        if (!this.isActivationAnimPlayed()) return false;
        return super.damage(source, amount);
    }

    @Override
    public void addBonusForWave(ServerWorld world, int wave, boolean unused) {}

    @Override
    public SoundEvent getCelebratingSound()
    {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        if (!this.isActivated() ||!this.isActivationAnimPlayed()) return null;
        return FracturiaSounds.SQUALL_GOLEM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        if (!this.isActivated() ||!this.isActivationAnimPlayed()) return null;
        return FracturiaSounds.SQUALL_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        if (!this.isActivated() ||!this.isActivationAnimPlayed()) return null;
        return FracturiaSounds.SQUALL_GOLEM_DEATH;
    }

    // --- GeckoLib ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate)
                .setSoundKeyframeHandler(event -> {
                    if (event.getKeyframeData().getSound().equals("left_step"))
                        this.getWorld().playSound(this.getX(), this.getY(), this.getZ(),
                                FracturiaSounds.SQUALL_GOLEM_LEFT_STEP, this.getSoundCategory(), 1.0f, 1.0f, false);
                    else if (event.getKeyframeData().getSound().equals("right_step"))
                        this.getWorld().playSound(this.getX(), this.getY(), this.getZ(),
                                FracturiaSounds.SQUALL_GOLEM_RIGHT_STEP, this.getSoundCategory(), 1.0f, 1.0f, false);
                }));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }

    private PlayState attackPredicate(AnimationState<SquallGolemEntity> state)
    {
        AnimationController<SquallGolemEntity> controller = state.getController();

        if (this.handSwinging && (controller.getAnimationState() == AnimationController.State.STOPPED ||
                controller.getAnimationState() == AnimationController.State.RUNNING))
        {
            controller.forceAnimationReset();
            controller.setAnimation(RawAnimation.begin().then("animation.squall_golem.attack", Animation.LoopType.PLAY_ONCE));
            this.handSwinging = false;
        }

        return PlayState.CONTINUE;
    }

    private int activationTimer = 0;

    private PlayState predicate(AnimationState<SquallGolemEntity> state)
    {
        AnimationController<SquallGolemEntity> controller = state.getController();

        if (!this.isActivated())
        {
            controller.setAnimation(RawAnimation.begin().then("animation.squall_golem.deactivated", Animation.LoopType.LOOP));
            activationAnimPlayed = false;
            return PlayState.CONTINUE;
        }

        if (!activationAnimPlayed)
        {
            if (activationTimer == 0)
            {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin()
                        .then("animation.squall_golem.activate", Animation.LoopType.PLAY_ONCE));
                activationTimer = 1;
                return PlayState.CONTINUE;
            }

            if (controller.getAnimationState() == AnimationController.State.STOPPED)
            {
                activationAnimPlayed = true;
                this.setActivationAnimPlayed(true); // sync vers le serveur via TrackedData
                activationTimer = 0;
            }

            return PlayState.CONTINUE;
        }

        if (state.isMoving())
        {
            controller.setAnimation(RawAnimation.begin().then("animation.squall_golem.walk", Animation.LoopType.LOOP));
        } else
        {
            controller.setAnimation(RawAnimation.begin().then("animation.squall_golem.idle", Animation.LoopType.LOOP));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return cache;
    }
}