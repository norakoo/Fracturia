package com.norako.fracturia.entity.custom.overworld.illagers;

import com.norako.fracturia.raid.FracturiaEnchantmentProviders;
import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
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
    boolean polnareff;
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public WindcallerEntity(EntityType<? extends RaiderEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return RaiderEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1.0f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 12.0);
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
        this.goalSelector.add(4, new WindcallerEntity.AttackGoal(this));
        this.goalSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.goalSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, true));
        this.goalSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.goalSelector.add(8, new WanderAroundGoal(this, 0.6));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.goalSelector.add(1, new PickupBannerAsLeaderGoal<>(this));
        this.goalSelector.add(3, new MoveToRaidCenterGoal<>(this));
        this.goalSelector.add(4, new WindcallerEntity.AttackHomeGoal(this, 1.0499999523162842, 1));
        this.goalSelector.add(5, new CelebrateGoal(this));
    }

    class AttackGoal extends MeleeAttackGoal {
        public AttackGoal(WindcallerEntity windcallerEntity) {
            super(windcallerEntity, 1.0, false);
        }

        protected double getSquaredMaxAttackDistance(LivingEntity target) {
            if (this.mob.getVehicle() instanceof RavagerEntity) {
                float f = this.mob.getVehicle().getWidth() - 0.1F;
                return (f * 2.0F * f * 2.0F + target.getWidth());
            } else {
                return 4.0D + target.getWidth();
            }
        }
    }

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


    @Override
    public SoundEvent getCelebratingSound()
    {
        return SoundEvents.ENTITY_EVOKER_CELEBRATE;
    }

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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
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
