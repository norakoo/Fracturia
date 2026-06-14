package com.norako.fracturia.entity.custom.overworld.illagers;

import com.norako.fracturia.entity.FracturiaEntities;
import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.entity.Entity;
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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.compress.utils.Lists;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class IllusionerEntity extends net.minecraft.entity.mob.IllusionerEntity implements GeoEntity
{
    private static final TrackedData<Integer> ATTACK_STATE =
            DataTracker.registerData(IllusionerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    final List<UUID> cloneUuids = new ArrayList<>();
    int summonCooldown = 0;
    int blindCooldown = 0;
    private int attackAnimTimer = 0;
    private int lastAttackState = 0;

    public IllusionerEntity(EntityType<? extends net.minecraft.entity.mob.IllusionerEntity> entityType, World world)
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
        return RaiderEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 32.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D);
    }

    // --- Goals ---

    @Override
    protected void initGoals()
    {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new PickupBannerAsLeaderGoal<>(this));
        this.goalSelector.add(2, new SummonClonesGoal(this));
        this.goalSelector.add(3, new ShootArrowGoal(this));
        this.goalSelector.add(4, new BlindnessGoal(this));
        this.goalSelector.add(5, new MoveToRaidCenterGoal<>(this));
        this.goalSelector.add(6, new AttackHomeGoal(this, 1.05, 1));
        this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.targetSelector.add(1, new RevengeGoal(this, RaiderEntity.class).setGroupRevenge());
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
        if (blindCooldown > 0) blindCooldown--;
        if (attackAnimTimer > 0) {
            attackAnimTimer--;
            if (attackAnimTimer == 0) {
                this.dataTracker.set(ATTACK_STATE, 0);
            }
        }
    }

    // --- Shoot arrow goal (right arm) ---

    class ShootArrowGoal extends Goal
    {
        // Adjust to the tick in the animation where the arrow releases
        private static final int SHOOT_TICK = 15;

        private final IllusionerEntity illusioner;
        private int timer = 0;
        private int cooldown = 0;
        private boolean shot = false;

        ShootArrowGoal(IllusionerEntity entity)
        {
            this.illusioner = entity;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart()
        {
            if (cooldown > 0) { cooldown--; return false; }
            LivingEntity target = illusioner.getTarget();
            return target != null && illusioner.squaredDistanceTo(target) <= 15.0 * 15.0;
        }

        @Override
        public boolean shouldContinue()
        {
            return !shot && illusioner.getTarget() != null;
        }

        @Override
        public void start()
        {
            timer = 0;
            shot = false;
            illusioner.dataTracker.set(ATTACK_STATE, 1);
            illusioner.attackAnimTimer = 5;
        }

        @Override
        public void stop()
        {
            cooldown = 20;
        }

        @Override
        public void tick()
        {
            LivingEntity target = illusioner.getTarget();
            if (target != null) illusioner.getLookControl().lookAt(target, 30.0F, 30.0F);
            timer++;
            if (timer >= SHOOT_TICK && !shot && target != null) {
                shot = true;
                if (illusioner.getWorld() instanceof ServerWorld) {
                    ItemStack bow = new ItemStack(Items.BOW);
                    ArrowEntity arrow = new ArrowEntity(illusioner.getWorld(), illusioner, bow, null);
                    double dx = target.getX() - illusioner.getX();
                    double dy = target.getEyeY() - arrow.getY() - 0.1;
                    double dz = target.getZ() - illusioner.getZ();
                    double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                    arrow.setVelocity(dx, dy + horizontalDist * 0.2, dz, 1.6F, 14 - illusioner.getWorld().getDifficulty().getId() * 4);
                    illusioner.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (illusioner.getRandom().nextFloat() * 0.4F + 0.8F));
                    illusioner.getWorld().spawnEntity(arrow);
                }
            }
        }
    }

    // --- Blindness goal ---

    class BlindnessGoal extends Goal
    {
        // Adjust to the cast frame in the animation
        private static final int CAST_TICK = 20;
        private static final int RANGE = 12;

        private final IllusionerEntity illusioner;
        private int timer = 0;
        private boolean cast = false;

        BlindnessGoal(IllusionerEntity entity)
        {
            this.illusioner = entity;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart()
        {
            LivingEntity target = illusioner.getTarget();
            return target != null
                && illusioner.blindCooldown <= 0
                && illusioner.squaredDistanceTo(target) <= RANGE * RANGE;
        }

        @Override
        public boolean shouldContinue()
        {
            return !cast && illusioner.getTarget() != null;
        }

        @Override
        public void start()
        {
            timer = 0;
            cast = false;
            illusioner.dataTracker.set(ATTACK_STATE, 3);
            illusioner.attackAnimTimer = 5;
            illusioner.playSound(FracturiaSounds.ILLUSIONER_BLINDNESS);
        }

        @Override
        public void stop()
        {
            illusioner.blindCooldown = 200;
        }

        @Override
        public void tick()
        {
            LivingEntity target = illusioner.getTarget();
            if (target != null) illusioner.getLookControl().lookAt(target, 30.0F, 30.0F);
            timer++;
            if (timer >= CAST_TICK && !cast && target != null) {
                cast = true;
                // Blindness for 7 seconds (140 ticks)
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 140, 0, false, true));
            }
        }
    }

    // --- Clone management ---

    boolean areAllClonesDead()
    {
        if (cloneUuids.isEmpty()) return true;
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return false;
        for (UUID uuid : cloneUuids) {
            Entity entity = serverWorld.getEntity(uuid);
            if (entity != null && entity.isAlive()) return false;
        }
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount)
    {
        net.minecraft.entity.Entity attacker = source.getAttacker();
        if (attacker instanceof IllusionerEntity || attacker instanceof IllusionerCloneEntity) return false;
        return super.damage(source, amount);
    }

    @Override
    public void onDeath(DamageSource source)
    {
        super.onDeath(source);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            for (UUID uuid : cloneUuids) {
                Entity entity = serverWorld.getEntity(uuid);
                if (entity != null && entity.isAlive()) {
                    entity.discard();
                }
            }
            cloneUuids.clear();
            this.playSound(FracturiaSounds.ILLUSIONER_ALL_DEATH);
        }
    }

    // --- Summon clones goal ---

    class SummonClonesGoal extends Goal
    {
        // Adjust to the frame in the animation where clones appear
        private static final int SPAWN_TICK = 25;

        private final IllusionerEntity illusioner;
        private int timer = 0;
        private boolean spawned = false;

        SummonClonesGoal(IllusionerEntity entity)
        {
            this.illusioner = entity;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart()
        {
            return illusioner.getTarget() != null
                && illusioner.summonCooldown <= 0
                && illusioner.areAllClonesDead()
                && illusioner.getWorld() instanceof ServerWorld;
        }

        @Override
        public boolean shouldContinue() { return !spawned; }

        @Override
        public void start()
        {
            timer = 0;
            spawned = false;
            illusioner.dataTracker.set(ATTACK_STATE, 2);
            illusioner.attackAnimTimer = 5;
            illusioner.playSound(FracturiaSounds.ILLUSIONER_PREPARE_MIRROR);
        }

        @Override
        public void stop()
        {
            illusioner.summonCooldown = 100;
        }

        @Override
        public void tick()
        {
            LivingEntity target = illusioner.getTarget();
            if (target != null) illusioner.getLookControl().lookAt(target, 30.0F, 30.0F);
            timer++;
            if (timer >= SPAWN_TICK && !spawned) {
                spawned = true;
                if (!(illusioner.getWorld() instanceof ServerWorld world)) return;

                illusioner.cloneUuids.clear();
                int count = 5 + illusioner.random.nextInt(3); // 5, 6 or 7
                for (int i = 0; i < count; i++) {
                    float angle = illusioner.random.nextFloat() * (float) (Math.PI * 2);
                    float dist = 1.5f + illusioner.random.nextFloat() * 2.0f;
                    double x = illusioner.getX() + Math.cos(angle) * dist;
                    double z = illusioner.getZ() + Math.sin(angle) * dist;

                    IllusionerCloneEntity clone = new IllusionerCloneEntity(FracturiaEntities.ILLUSIONER_CLONE_ENTITY, world);
                    clone.setParentUuid(illusioner.getUuid());
                    clone.refreshPositionAndAngles(x, illusioner.getY(), z, illusioner.random.nextFloat() * 360, 0);
                    clone.setTarget(target);
                    world.spawnEntity(clone);
                    illusioner.cloneUuids.add(clone.getUuid());
                }
                illusioner.playSound(FracturiaSounds.ILLUSIONER_MIRROR);
            }
        }
    }

    // --- Attack home goal ---

    static class AttackHomeGoal extends Goal
    {
        private final IllusionerEntity raider;
        private final double speed;
        private BlockPos home;
        private final List<BlockPos> lastHomes = Lists.newArrayList();
        private final int distance;
        private boolean finished;

        AttackHomeGoal(IllusionerEntity raider, double speed, int distance)
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
            ServerWorld serverWorld = (ServerWorld) this.raider.getWorld();
            BlockPos blockPos = this.raider.getBlockPos();
            Optional<BlockPos> optional = serverWorld.getPointOfInterestStorage().getPosition(
                    entry -> entry.matchesKey(PointOfInterestTypes.HOME),
                    this::canLootHome,
                    PointOfInterestStorage.OccupationStatus.ANY,
                    blockPos, 48, this.raider.random);
            if (optional.isEmpty()) return false;
            this.home = optional.get().toImmutable();
            return true;
        }

        public boolean shouldContinue()
        {
            if (this.raider.getNavigation().isIdle()) return false;
            return this.raider.getTarget() == null
                && !this.home.isWithinDistance(this.raider.getPos(), this.raider.getWidth() + this.distance)
                && !this.finished;
        }

        public void start()
        {
            super.start();
            this.raider.setDespawnCounter(0);
            this.raider.getNavigation().startMovingTo(this.home.getX(), this.home.getY(), this.home.getZ(), this.speed);
            this.finished = false;
        }

        public void stop()
        {
            if (this.home.isWithinDistance(this.raider.getPos(), this.distance))
                this.lastHomes.add(this.home);
        }

        public void tick()
        {
            if (!this.raider.getNavigation().isIdle()) return;
            Vec3d target = Vec3d.ofBottomCenter(this.home);
            Vec3d next = NoPenaltyTargeting.findTo(this.raider, 16, 7, target, 0.3141592741012573);
            if (next == null) next = NoPenaltyTargeting.findTo(this.raider, 8, 7, target, 1.5707963705062866);
            if (next == null) { this.finished = true; return; }
            this.raider.getNavigation().startMovingTo(next.x, next.y, next.z, this.speed);
        }

        private boolean canLootHome(BlockPos pos)
        {
            Iterator<BlockPos> it = this.lastHomes.iterator();
            BlockPos blockPos;
            do {
                if (!it.hasNext()) return true;
                blockPos = it.next();
            } while (!Objects.equals(pos, blockPos));
            return false;
        }

        private void purgeMemory()
        {
            if (this.lastHomes.size() > 2) this.lastHomes.remove(0);
        }
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

    // --- GeckoLib ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }

    private PlayState attackPredicate(AnimationState<IllusionerEntity> state)
    {
        int attackState = this.dataTracker.get(ATTACK_STATE);
        if (attackState != lastAttackState) {
            lastAttackState = attackState;
            AnimationController<IllusionerEntity> controller = state.getController();
            if (attackState == 1) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.illusioner.attack", Animation.LoopType.PLAY_ONCE));
            } else if (attackState == 2) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.illusioner.summon", Animation.LoopType.PLAY_ONCE));
            } else if (attackState == 3) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.illusioner.blind", Animation.LoopType.PLAY_ONCE));
            }
        }
        return PlayState.CONTINUE;
    }

    private PlayState predicate(AnimationState<IllusionerEntity> state)
    {
        AnimationController<IllusionerEntity> controller = state.getController();

        if (this.isCelebrating()) {
            controller.setAnimation(RawAnimation.begin().then("animation.illusioner.celebrate", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

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
