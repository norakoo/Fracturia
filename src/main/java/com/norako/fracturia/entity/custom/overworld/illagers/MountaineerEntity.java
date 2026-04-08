package com.norako.fracturia.entity.custom.overworld.illagers;

import com.norako.fracturia.raid.FracturiaEnchantmentProviders;
import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.function.Predicate;

public class MountaineerEntity extends RaiderEntity implements GeoEntity
{
    static final Predicate<Difficulty> DIFFICULTY_ALLOWS_DOOR_BREAKING_PREDICATE = (difficulty) -> {
        return difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    int attackCooldown = 0;

    public MountaineerEntity(EntityType<? extends RaiderEntity> entityType, World world)
    {
        super(entityType, world);
        this.setCanPickUpLoot(true);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData)
    {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        if (this.random.nextInt(5) == 0)
        {
            this.setPatrolLeader(true);
            Item ominousBanner = Registries.ITEM.get(Identifier.of("minecraft", "ominous_banner"));
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(ominousBanner));
        }
        return data;
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return RaiderEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3499999940395355f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12.0);
    }

    @Override
    public void addBonusForWave(ServerWorld world, int wave, boolean unused) {
        ItemStack itemStack = new ItemStack(Items.IRON_PICKAXE);

        Raid raid = this.getRaid();

        boolean applyEnchant = this.random.nextFloat() <= raid.getEnchantmentChance();
        if (applyEnchant) {
            RegistryKey<EnchantmentProvider> registryKey = wave > raid.getMaxWaves(world.getDifficulty())
                    ? FracturiaEnchantmentProviders.MOUNTAINEER
                    : FracturiaEnchantmentProviders.MOUNTAINEER_POST_WAVE_5;

            EnchantmentHelper.applyEnchantmentProvider(
                    itemStack,
                    world.getRegistryManager(),
                    registryKey,
                    world.getLocalDifficulty(this.getBlockPos()),
                    this.random
            );
        }

        this.equipStack(EquipmentSlot.MAINHAND, itemStack);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (attackCooldown > 0) attackCooldown--;
    }

    @Override
    protected void initGoals()
    {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MountaineerEntity.BreakDoorGoal(this));
        this.goalSelector.add(4, new MountaineerEntity.AttackGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.goalSelector.add(1, new PickupBannerAsLeaderGoal<>(this));
        this.goalSelector.add(3, new MoveToRaidCenterGoal<>(this));
        this.goalSelector.add(4, new AttackHomeGoal(this, 1.0499999523162842, 1));
        this.goalSelector.add(5, new CelebrateGoal(this));
    }

    class AttackGoal extends MeleeAttackGoal {
        private int attackDelayTimer = 0;
        private LivingEntity pendingTarget = null;

        public AttackGoal(MountaineerEntity mountaineer) {
            super(mountaineer, 1.0, false);
        }

        protected double getSquaredMaxAttackDistance(LivingEntity target) {
            if (this.mob.getVehicle() instanceof RavagerEntity) {
                float f = this.mob.getVehicle().getWidth() - 0.1F;
                return (f * 2.0F * f * 2.0F + target.getWidth());
            } else {
                return 4.0D + target.getWidth();
            }
        }

        @Override
        public boolean canStart() {
            return MountaineerEntity.this.attackCooldown <= 0 && super.canStart();
        }

        @Override
        public void tick() {
            super.tick();
            if (attackDelayTimer > 0) {
                attackDelayTimer--;
                if (attackDelayTimer == 0 && pendingTarget != null) {
                    if (MountaineerEntity.this.squaredDistanceTo(pendingTarget) <= getSquaredMaxAttackDistance(pendingTarget))
                        MountaineerEntity.this.tryAttack(pendingTarget);
                    pendingTarget = null;
                }
            }
        }

        @Override
        protected void attack(LivingEntity target) {
            if (attackDelayTimer <= 0 && MountaineerEntity.this.attackCooldown <= 0
                    && MountaineerEntity.this.squaredDistanceTo(target) <= getSquaredMaxAttackDistance(target)) {
                MountaineerEntity.this.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                attackDelayTimer = 7;
                MountaineerEntity.this.attackCooldown = 30;
                pendingTarget = target;
            }
        }
    }

    static class AttackHomeGoal extends Goal
    {
        private final MountaineerEntity raider;
        private final double speed;
        private BlockPos home;
        private final List<BlockPos> lastHomes = Lists.newArrayList();
        private final int distance;
        private boolean finished;

        public AttackHomeGoal(MountaineerEntity raider, double speed, int distance)
        {
            this.raider = raider;
            this.speed = speed;
            this.distance = distance;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart()
        {
            this.purgeMemory();
            return this.isRaiding() && this.tryFindHome() && this.raider.getTarget() == null;
        }

        private boolean isRaiding()
        {
            return this.raider.hasActiveRaid() && !this.raider.getRaid().isFinished();
        }

        private boolean tryFindHome()
        {
            ServerWorld serverWorld = (ServerWorld)this.raider.getWorld();
            BlockPos blockPos = this.raider.getBlockPos();
            Optional<BlockPos> optional = serverWorld.getPointOfInterestStorage().getPosition(registryEntry -> {
                return registryEntry.matchesKey(PointOfInterestTypes.HOME);
            }, this::canLootHome, PointOfInterestStorage.OccupationStatus.ANY, blockPos, 48, this.raider.random);
            if (!optional.isPresent())
            {
                return false;
            } else
            {
                this.home = ((BlockPos)optional.get()).toImmutable();
                return true;
            }
        }

        public boolean shouldContinue()
        {
            if (this.raider.getNavigation().isIdle())
            {
                return false;
            } else
            {
                return this.raider.getTarget() == null && !this.home.isWithinDistance(this.raider.getPos(), (double)(this.raider.getWidth() + (float)this.distance)) && ! this.finished;
            }
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
            this.raider.getNavigation().startMovingTo((double)this.home.getX(), (double)this.home.getY(), (double)this.home.getZ(), this.speed);
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
            Iterator var2 = this.lastHomes.iterator();

            BlockPos blockPos;
            do
            {
                if (!var2.hasNext())
                {
                    return true;
                }

                blockPos = (BlockPos)var2.next();
            } while(!Objects.equals(pos, blockPos));

            return false;
        }

        private void purgeMemory()
        {
            if (this.lastHomes.size() > 2)
            {
                this.lastHomes.remove(0);
            }
        }
    }

    static class BreakDoorGoal extends net.minecraft.entity.ai.goal.BreakDoorGoal
    {
        public BreakDoorGoal(MobEntity mobEntity)
        {
            super(mobEntity, 6, MountaineerEntity.DIFFICULTY_ALLOWS_DOOR_BREAKING_PREDICATE);
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean shouldContinue()
        {
            MountaineerEntity mountaineerEntity = (MountaineerEntity)this.mob;
            return mountaineerEntity.hasActiveRaid() && super.shouldContinue();
        }

        public boolean canStart()
        {
            MountaineerEntity mountaineerEntity = (MountaineerEntity)this.mob;
            return mountaineerEntity.hasActiveRaid() && mountaineerEntity.random.nextInt(toGoalTicks(10)) == 0 && super.canStart();
        }

        public void start()
        {
            super.start();
            this.mob.setDespawnCounter(0);
        }
    }

    public class CelebrateGoal extends Goal
    {
        private final MountaineerEntity raider;

        CelebrateGoal(MountaineerEntity raider)
        {
            this.raider = raider;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart()
        {
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
                MountaineerEntity.this.playSound(MountaineerEntity.this.getCelebratingSound(), MountaineerEntity.this.getSoundVolume(), MountaineerEntity.this.getSoundPitch());
            }

            if (!this.raider.hasVehicle() && this.raider.random.nextInt(this.getTickCount(50)) == 0)
            {
                this.raider.getJumpControl().setActive();
            }

            super.tick();
        }
    }

    @Override
    public SoundEvent getCelebratingSound()
    {
        return SoundEvents.ENTITY_VINDICATOR_CELEBRATE;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return FracturiaSounds.MOUNTAINEER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return FracturiaSounds.MOUNTAINEER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return FracturiaSounds.MOUNTAINEER_DEATH;
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }

    private PlayState attackPredicate(AnimationState<MountaineerEntity> state) {
        AnimationController<MountaineerEntity> controller = state.getController();

        if (this.handSwinging && (controller.getAnimationState() == AnimationController.State.STOPPED ||
                controller.getAnimationState() == AnimationController.State.RUNNING)) {

            controller.forceAnimationReset();
            controller.setAnimation(RawAnimation.begin().then("animation.mountaineer.right.attack_fast", Animation.LoopType.PLAY_ONCE));
            this.handSwinging = false;
        }

        return PlayState.CONTINUE;
    }

    private PlayState predicate(AnimationState<MountaineerEntity> mountaineerEntityAnimationState)
    {
        if(mountaineerEntityAnimationState.isMoving())
        {
            mountaineerEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.mountaineer.right.walk", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        } else
        {
            if (this.isCelebrating())
            {
                mountaineerEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.mountaineer.celebrate", Animation.LoopType.LOOP));
                return PlayState.CONTINUE;
            } else
            {
                mountaineerEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.mountaineer.idle", Animation.LoopType.LOOP));
            }
        }

        mountaineerEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.mountaineer.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return cache;
    }
}
