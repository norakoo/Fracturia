package com.norako.fracturia.entity.custom.end.endersent;

import com.norako.fracturia.advancement.FracturiaCriteria;
import com.norako.fracturia.entity.custom.end.endersent.goal.DeadlyEscapeGoal;
import com.norako.fracturia.entity.custom.end.endersent.goal.EndersentSweepAttackGoal;
import com.norako.fracturia.entity.custom.end.endersent.goal.TeleportSmashGoal;
import com.norako.fracturia.sound.FracturiaSounds;
import com.norako.fracturia.world.EndersentWorldState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class EndersentEntity extends HostileEntity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ServerBossBar bossBar;

    public boolean isDoingAbility = false;
    private boolean playingDeathAnim = false;
    private int deathAnimTick = 0;
    private boolean eyeDropped = false;

    protected EndersentEntity(EntityType<? extends EndersentEntity> type, World world) {
        super(type, world);
        if (!world.isClient() && getVariant() != null) {
            bossBar = new ServerBossBar(
                    Text.literal(getBossBarName()),
                    getBossBarColor(),
                    BossBar.Style.PROGRESS
            );
        }
    }

    public abstract EndersentWorldState.Variant getVariant();
    protected abstract String getBossBarName();
    protected abstract BossBar.Color getBossBarColor();

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(2, new EndersentSweepAttackGoal(this));
        goalSelector.add(3, new TeleportSmashGoal(this, 12.0, 200));
        goalSelector.add(4, new DeadlyEscapeGoal(this, 400));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(7, new LookAroundGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        if (!super.canSpawn(world, reason)) return false;
        if (getVariant() == null) return true;
        if (world instanceof ServerWorld serverWorld) {
            EndersentWorldState state = EndersentWorldState.get(serverWorld.getServer());
            if (!state.isPortalRoomVisited()) return false;
            return !state.isKilled(getVariant()) && !state.isSpawned(getVariant());
        }
        return false;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason reason, EntityData entityData) {
        markSpawned();
        return super.initialize(world, difficulty, reason, entityData);
    }

    private void markSpawned() {
        if (getVariant() == null) return;
        if (getWorld() instanceof ServerWorld serverWorld) {
            EndersentWorldState.get(serverWorld.getServer()).setSpawned(getVariant());
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        this.triggerAnim("action_controller", "death");
        playingDeathAnim = true;
        deathAnimTick = 0;
        super.onDeath(source);
        if (getVariant() != null && getWorld() instanceof ServerWorld serverWorld) {
            EndersentWorldState state = EndersentWorldState.get(serverWorld.getServer());
            state.setKilled(getVariant());
            state.clearSpawned(getVariant());

            // Après le setKilled :
            boolean allKilled = true;
            for (EndersentWorldState.Variant v : EndersentWorldState.Variant.values()) {
                if (!state.isKilled(v)) { allKilled = false; break; }
            }
            if (allKilled) {
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    FracturiaCriteria.KILLED_ALL_ENDERSENT.trigger(player);
                }
            }

            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                FracturiaCriteria.KILLED_ENDERSENT_VARIANT.trigger(player);
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED && playingDeathAnim) return;
        super.remove(reason);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        if (bossBar != null) bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        if (bossBar != null) bossBar.removePlayer(player);
    }

    @Override
    public void tick() {
        super.tick();
        if (playingDeathAnim) {
            deathAnimTick++;
            this.deathTime = 0;
            if (deathAnimTick == 50 && !getWorld().isClient() && getVariant() != null && !eyeDropped) {
                eyeDropped = true;
                net.minecraft.util.math.Vec3d look = this.getRotationVec(1.0f);
                double dropX = this.getX() + look.x * 3.5;
                double dropY = this.getEyeY() + 0.5;
                double dropZ = this.getZ() + look.z * 3.5;
                net.minecraft.entity.ItemEntity item = new net.minecraft.entity.ItemEntity(
                        getWorld(), dropX, dropY, dropZ,
                        new net.minecraft.item.ItemStack(getVariantEye())
                );
                item.setVelocity(look.x * 0.2, 0.15, look.z * 0.2);
                getWorld().spawnEntity(item);
            }
            if (deathAnimTick >= 100) {
                playingDeathAnim = false;
                super.remove(Entity.RemovalReason.KILLED);
                return;
            }
        }

        if (bossBar != null) bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if (getWorld().isClient()) {
            for (int i = 0; i < 2; i++) {
                getWorld().addParticle(net.minecraft.particle.ParticleTypes.PORTAL,
                        getParticleX(0.5), getRandomBodyY() - 0.25, getParticleZ(0.5),
                        (random.nextDouble() - 0.5) * 2.0,
                        -random.nextDouble(),
                        (random.nextDouble() - 0.5) * 2.0);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            if (state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("endersent_walk"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("endersent_idle"));
        }).setSoundKeyframeHandler(event -> {
            if (event.getKeyframeData().getSound().equals("idle_smash")) {
                getWorld().playSound(getX(), getY(), getZ(),
                        FracturiaSounds.ENDERSENT_AMBIENT_SMASH,
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f, false);
            }
        }));
        controllers.add(new AnimationController<>(this, "action_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("endersent_attack"))
                .triggerableAnim("teleport", RawAnimation.begin().thenPlay("endersent_teleport"))
                .triggerableAnim("death", RawAnimation.begin().thenPlay("endersent_death")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean cannotDespawn() {
        return getVariant() != null;
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (getVariant() == null) return;
        if (getWorld() instanceof ServerWorld serverWorld) {
            EndersentWorldState state = EndersentWorldState.get(serverWorld.getServer());
            if (!state.isKilled(getVariant())) {
                state.clearSpawned(getVariant());
            }
        }
    }

    @Override
    public boolean isAffectedByDaylight() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FracturiaSounds.ENDERSENT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return FracturiaSounds.ENDERSENT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FracturiaSounds.ENDERSENT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(FracturiaSounds.ENDERSENT_STEP, 0.15f, 1.0f);
    }

    private net.minecraft.item.Item getVariantEye() {
        return switch (getVariant()) {
            case BINDING -> com.norako.fracturia.item.FracturiaItems.BINDING_EYE;
            case BLIGHT -> com.norako.fracturia.item.FracturiaItems.BLIGHT_EYE;
            case RAVENOUS -> com.norako.fracturia.item.FracturiaItems.RAVENOUS_EYE;
            case REAPING -> com.norako.fracturia.item.FracturiaItems.REAPING_EYE;
            case SAVAGE -> com.norako.fracturia.item.FracturiaItems.SAVAGE_EYE;
            case SPIKED -> com.norako.fracturia.item.FracturiaItems.SPIKED_EYE;
        };
    }
}
