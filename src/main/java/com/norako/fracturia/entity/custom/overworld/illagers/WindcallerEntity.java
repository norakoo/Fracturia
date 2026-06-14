package com.norako.fracturia.entity.custom.overworld.illagers;

import com.norako.fracturia.raid.FracturiaEnchantmentProviders;
import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.function.Predicate;

public class WindcallerEntity extends RaiderEntity implements GeoEntity
{
    private static final String POLNAREFF = "Polnareff";
    static final Predicate<Difficulty> DIFFICULTY_ALLOWS_DOOR_BREAKING_PREDICATE = (difficulty) -> {
        return difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;
    };

    private static final TrackedData<Integer> ATTACK_STATE =
            DataTracker.registerData(WindcallerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    boolean polnareff;
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    int blastCooldown = 0;
    int liftCooldown = 0;
    private int attackAnimTimer = 0;
    private int lastAttackState = 0;

    public WindcallerEntity(EntityType<? extends RaiderEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ATTACK_STATE, 0);
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return RaiderEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    public void tick()
    {
        super.tick();
        if (blastCooldown > 0) blastCooldown--;
        if (liftCooldown > 0) liftCooldown--;
        if (attackAnimTimer > 0) {
            attackAnimTimer--;
            if (attackAnimTimer == 0) {
                this.dataTracker.set(ATTACK_STATE, 0);
            }
        }
    }

    public void writeCustomDataToNbt(NbtCompound nbt)
    {
        super.writeCustomDataToNbt(nbt);
        if (this.polnareff)
        {
            nbt.putBoolean("Polnareff", true);
        }
    }

    public void readCustomDataFromNbt(NbtCompound nbt)
    {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Polnareff", 99))
        {
            this.polnareff = nbt.getBoolean("Polnareff");
        }
    }

    public void setCustomName(@Nullable Text name)
    {
        super.setCustomName(name);
        if (!this.polnareff && name != null && name.getString().equals("Polnareff"))
        {
            this.polnareff = true;
        }
    }

    @Override
    public void addBonusForWave(ServerWorld world, int wave, boolean unused) {
        ItemStack itemStack = new ItemStack(Items.IRON_PICKAXE);

        Raid raid = this.getRaid();

        boolean applyEnchant = this.random.nextFloat() <= raid.getEnchantmentChance();
        if (applyEnchant) {
            RegistryKey<EnchantmentProvider> registryKey = wave > raid.getMaxWaves(world.getDifficulty())
                    ? FracturiaEnchantmentProviders.WINDCALLER
                    : FracturiaEnchantmentProviders.WINDCALLER_POST_WAVE_5;

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
    protected void initGoals()
    {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new WindcallerEntity.BreakDoorGoal(this));
        this.goalSelector.add(4, new WindcallerEntity.WindBlastGoal(this));
        this.goalSelector.add(5, new WindcallerEntity.WindLiftGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.goalSelector.add(1, new PickupBannerAsLeaderGoal<>(this));
        this.goalSelector.add(3, new MoveToRaidCenterGoal<>(this));
        this.goalSelector.add(4, new WindcallerEntity.AttackHomeGoal(this, 1.0499999523162842, 1));
        this.goalSelector.add(5, new CelebrateGoal(this));
    }

    // --- Wind Blast Goal (ranged, fires wind charge) ---

    class WindBlastGoal extends Goal {
        // Adjust this to the tick in the animation where the hand releases the blast
        private static final int BLAST_HIT_TICK = 15;

        private final WindcallerEntity windcaller;
        private int timer = 0;
        private boolean fired = false;

        WindBlastGoal(WindcallerEntity entity) {
            this.windcaller = entity;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = windcaller.getTarget();
            return target != null
                && windcaller.blastCooldown <= 0
                && windcaller.squaredDistanceTo(target) <= 16.0 * 16.0;
        }

        @Override
        public boolean shouldContinue() {
            return !fired && windcaller.getTarget() != null;
        }

        @Override
        public void start() {
            timer = 0;
            fired = false;
            // Trigger animation immediately so it starts at frame 0
            windcaller.dataTracker.set(ATTACK_STATE, 1);
            windcaller.attackAnimTimer = 5;
            windcaller.playSound(FracturiaSounds.WINDCALLER_BLAST);
        }

        @Override
        public void stop() {
            timer = 0;
            fired = false;
        }

        @Override
        public void tick() {
            LivingEntity target = windcaller.getTarget();
            if (target != null) {
                windcaller.getLookControl().lookAt(target, 30.0F, 30.0F);
            }
            timer++;
            if (timer >= BLAST_HIT_TICK && !fired && target != null) {
                fired = true;
                if (windcaller.getWorld() instanceof ServerWorld serverWorld) {
                    Vec3d eyePos = windcaller.getEyePos();
                    Vec3d direction = target.getEyePos().subtract(eyePos).normalize();

                    WindChargeEntity windCharge = new WindChargeEntity(EntityType.WIND_CHARGE, serverWorld);
                    windCharge.setOwner(windcaller);
                    windCharge.refreshPositionAfterTeleport(eyePos.x, eyePos.y, eyePos.z);
                    windCharge.setVelocity(direction.x, direction.y, direction.z, 1.5f, 0.0f);
                    serverWorld.spawnEntity(windCharge);
                    windcaller.playSound(FracturiaSounds.WINDCALLER_BLAST_ATTACK);
                }
                windcaller.blastCooldown = 60;
            }
        }
    }

    // --- Wind Lift Goal (applies levitation, player falls and takes fall damage) ---

    class WindLiftGoal extends Goal {
        // Adjust this to the tick in the animation where the lift effect triggers
        private static final int LIFT_HIT_TICK = 15;

        private final WindcallerEntity windcaller;
        private int timer = 0;
        private boolean cast = false;

        WindLiftGoal(WindcallerEntity entity) {
            this.windcaller = entity;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = windcaller.getTarget();
            return target != null
                && windcaller.liftCooldown <= 0
                && windcaller.squaredDistanceTo(target) <= 10.0 * 10.0;
        }

        @Override
        public boolean shouldContinue() {
            return !cast && windcaller.getTarget() != null;
        }

        @Override
        public void start() {
            timer = 0;
            cast = false;
            // Trigger animation immediately so it starts at frame 0
            windcaller.dataTracker.set(ATTACK_STATE, 2);
            windcaller.attackAnimTimer = 5;
            windcaller.playSound(FracturiaSounds.WINDCALLER_LIFT);
        }

        @Override
        public void stop() {
            timer = 0;
            cast = false;
        }

        @Override
        public void tick() {
            LivingEntity target = windcaller.getTarget();
            if (target != null) {
                windcaller.getLookControl().lookAt(target, 30.0F, 30.0F);
            }
            timer++;
            if (timer >= LIFT_HIT_TICK && !cast && target != null
                    && windcaller.squaredDistanceTo(target) <= 10.0 * 10.0) {
                cast = true;
                // Levitation for 3 seconds (60 ticks), amplifier 1 for faster rise
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 2, false, true));
                windcaller.playSound(FracturiaSounds.WINDCALLER_TORNADO);
                windcaller.liftCooldown = 120;
            }
        }
    }

    // --- Supporting goals (unchanged) ---

    static class AttackHomeGoal extends Goal
    {
        private final WindcallerEntity raider;
        private final double speed;
        private BlockPos home;
        private final List<BlockPos> lastHomes = Lists.newArrayList();
        private final int distance;
        private boolean finished;

        public AttackHomeGoal(WindcallerEntity raider, double speed, int distance)
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
            super(mobEntity, 6, WindcallerEntity.DIFFICULTY_ALLOWS_DOOR_BREAKING_PREDICATE);
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean shouldContinue()
        {
            WindcallerEntity windcallerEntity = (WindcallerEntity) this.mob;
            return windcallerEntity.hasActiveRaid() && super.shouldContinue();
        }

        public boolean canStart()
        {
            WindcallerEntity windcallerEntity = (WindcallerEntity) this.mob;
            return windcallerEntity.hasActiveRaid() && windcallerEntity.random.nextInt(toGoalTicks(10)) == 0 && super.canStart();
        }

        public void start()
        {
            super.start();
            this.mob.setDespawnCounter(0);
        }
    }

    public class CelebrateGoal extends Goal
    {
        private final WindcallerEntity raider;

        CelebrateGoal(WindcallerEntity raider)
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
                WindcallerEntity.this.playSound(WindcallerEntity.this.getCelebratingSound(), WindcallerEntity.this.getSoundVolume(), WindcallerEntity.this.getSoundPitch());
            }

            if (!this.raider.hasVehicle() && this.raider.random.nextInt(this.getTickCount(50)) == 0)
            {
                this.raider.getJumpControl().setActive();
            }

            super.tick();
        }
    }

    // --- Sounds ---

    @Override
    public SoundEvent getCelebratingSound()
    {
        return SoundEvents.ENTITY_EVOKER_CELEBRATE;
    }

    @Override
    protected float getSoundVolume() { return 0.6F; } // portée ~10 blocs

    @Override
    protected SoundEvent getAmbientSound()
    {
        if(this.polnareff)
        {
            return FracturiaSounds.WINDCALLER_POLNAREFF;
        } else
        {
            return FracturiaSounds.WINDCALLER_AMBIENT;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return FracturiaSounds.WINDCALLER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return FracturiaSounds.WINDCALLER_DEATH;
    }

    // --- Animations ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }

    private PlayState attackPredicate(AnimationState<WindcallerEntity> state)
    {
        AnimationController<WindcallerEntity> controller = state.getController();
        int attackState = this.dataTracker.get(ATTACK_STATE);

        if (attackState != lastAttackState) {
            lastAttackState = attackState;
            if (attackState == 1) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("windcaller_blast", Animation.LoopType.PLAY_ONCE));
            } else if (attackState == 2) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("windcaller_lift", Animation.LoopType.PLAY_ONCE));
            }
        }

        return PlayState.CONTINUE;
    }

    private PlayState predicate(AnimationState<WindcallerEntity> windcallerEntityAnimationState)
    {
        if(windcallerEntityAnimationState.isMoving())
        {
            windcallerEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("windcaller_fly", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        } else
        {
            if (this.isCelebrating())
            {
                windcallerEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("windcaller_celebrate", Animation.LoopType.LOOP));
                return PlayState.CONTINUE;
            } else
            {
                windcallerEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("windcaller_idle", Animation.LoopType.LOOP));
            }
        }

        windcallerEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("windcaller_idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return cache;
    }
}
