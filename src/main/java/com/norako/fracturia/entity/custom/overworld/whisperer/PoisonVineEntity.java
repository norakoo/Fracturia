package com.norako.fracturia.entity.custom.overworld.whisperer;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PoisonVineEntity extends MobEntity implements GeoEntity
{
    private static final TrackedData<Boolean> IS_OPEN =
            DataTracker.registerData(PoisonVineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SHOOT_STATE =
            DataTracker.registerData(PoisonVineEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final int DETECTION_RADIUS = 8;
    private static final int LIFETIME = 400; // 20 seconds
    private static final int SHOOT_COOLDOWN = 40; // fire every 2 seconds

    private int ticksAlive = 0;
    private int shootCooldown = 0;
    private int shootAnimTimer = 0;
    private int lastShootState = 0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PoisonVineEntity(EntityType<? extends MobEntity> entityType, World world)
    {
        super(entityType, world);
        this.setNoGravity(false);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder)
    {
        super.initDataTracker(builder);
        builder.add(IS_OPEN, false);
        builder.add(SHOOT_STATE, 0);
    }

    public static DefaultAttributeContainer.Builder setAttributes()
    {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0D);
    }

    // No AI goals — stationary entity
    @Override
    protected void initGoals() {}

    // --- Tick ---

    @Override
    public void tick()
    {
        super.tick();
        ticksAlive++;

        // Despawn after LIFETIME ticks
        if (ticksAlive >= LIFETIME) {
            this.discard();
            return;
        }

        if (shootCooldown > 0) shootCooldown--;
        if (shootAnimTimer > 0) {
            shootAnimTimer--;
            if (shootAnimTimer == 0) this.dataTracker.set(SHOOT_STATE, 0);
        }

        // Detect nearby survival players
        PlayerEntity nearest = this.getWorld().getClosestPlayer(this, DETECTION_RADIUS);
        boolean open = nearest != null && !nearest.isCreative() && !nearest.isSpectator();
        this.dataTracker.set(IS_OPEN, open);

        // Shoot poisoned arrow when open (server-side only)
        if (open && shootCooldown <= 0 && this.getWorld() instanceof ServerWorld) {
            shootCooldown = SHOOT_COOLDOWN;
            this.dataTracker.set(SHOOT_STATE, 1);
            this.shootAnimTimer = 5;
            shootAt(nearest);
        }
    }

    private void shootAt(LivingEntity target)
    {
        ItemStack bow = new ItemStack(Items.BOW);
        ArrowEntity arrow = new ArrowEntity(this.getWorld(), this, bow, null);
        arrow.addEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0));

        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        arrow.setVelocity(dx, dy + horizontalDist * 0.2, dz, 1.2F, 6.0F);

        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 0.8F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getWorld().spawnEntity(arrow);
    }

    // --- NBT ---

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt)
    {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("TicksAlive", ticksAlive);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt)
    {
        super.readCustomDataFromNbt(nbt);
        ticksAlive = nbt.getInt("TicksAlive");
    }

    // --- Misc ---

    @Override
    public boolean damage(DamageSource source, float amount)
    {
        if (source.getAttacker() instanceof WhispererEntity) return false;
        return super.damage(source, amount);
    }

    @Override
    public boolean isCollidable() { return false; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.BLOCK_VINE_BREAK; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.BLOCK_VINE_BREAK; }

    @Override
    protected SoundEvent getAmbientSound() { return null; }

    // --- GeckoLib ---

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 2, this::predicate));
        controllers.add(new AnimationController<>(this, "shootController", 0, this::shootPredicate));
    }

    private PlayState predicate(AnimationState<PoisonVineEntity> state)
    {
        AnimationController<PoisonVineEntity> controller = state.getController();
        if (this.dataTracker.get(IS_OPEN)) {
            controller.setAnimation(RawAnimation.begin().then("animation.poison_vine.idle.open", Animation.LoopType.LOOP));
        } else {
            controller.setAnimation(RawAnimation.begin().then("animation.poison_vine.idle.closed", Animation.LoopType.LOOP));
        }
        return PlayState.CONTINUE;
    }

    private PlayState shootPredicate(AnimationState<PoisonVineEntity> state)
    {
        int shootState = this.dataTracker.get(SHOOT_STATE);
        if (shootState != lastShootState) {
            lastShootState = shootState;
            if (shootState == 1) {
                AnimationController<PoisonVineEntity> controller = state.getController();
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.poison_vine.attack", Animation.LoopType.PLAY_ONCE));
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
