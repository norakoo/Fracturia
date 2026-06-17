package com.norako.fracturia.entity.custom.end.endersent.goal;

import com.norako.fracturia.entity.custom.end.endersent.EndersentEntity;
import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;

public class DeadlyEscapeGoal extends Goal {

    private final EndersentEntity endersent;
    private final int cooldownMax;
    private int cooldown = 0;
    private int phase = 0;
    private int phaseTick = 0;
    private Vec3d savedPos;
    private boolean triggered = false;

    public DeadlyEscapeGoal(EndersentEntity endersent, int cooldownMax) {
        this.endersent = endersent;
        this.cooldownMax = cooldownMax;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (--cooldown > 0) return false;
        if (triggered) return false;
        return endersent.getHealth() < endersent.getMaxHealth() * 0.7f
                && endersent.getTarget() != null;
    }

    @Override
    public boolean shouldContinue() {
        return phase > 0;
    }

    @Override
    public void start() {
        phase = 1;
        phaseTick = 0;
        triggered = true;
        savedPos = endersent.getPos();
        endersent.getNavigation().stop();
        endersent.triggerAnim("action_controller", "attack");
    }

    @Override
    public void tick() {
        phaseTick++;

        if (phase == 1 & phaseTick >= 10) {
            if (endersent.getWorld() instanceof ServerWorld sw) {
                List<PlayerEntity> nearby = sw.getEntitiesByClass(PlayerEntity.class,
                        endersent.getBoundingBox().expand(5), e -> true);
                for (PlayerEntity p : nearby) {
                    p.damage(sw.getDamageSources().mobAttack(endersent), 20.0f);
                    p.takeKnockback(3.0, endersent.getX() - p.getX(), endersent.getZ() - p.getZ());
                }
            }
            phase = 2;
            phaseTick = 0;
        } else if (phase == 2 && phaseTick >= 10) {
            endersent.setInvisible(true);
            endersent.setInvulnerable(true);
            endersent.triggerAnim("action_controller", "teleport");
            endersent.requestTeleport(endersent.getX(), endersent.getY() + 200, endersent.getZ());
            if (endersent.getWorld() instanceof ServerWorld sw) {
                sw.playSound(null, endersent.getBlockPos(),
                        FracturiaSounds.ENDERSENT_DEADLY_ESCAPE,
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
            }
            ServerWorld sw = (ServerWorld) endersent.getWorld();
            for (int i = 0; i < 3 + sw.getRandom().nextInt(4); i++) {
                EndermanEntity watchling = EntityType.ENDERMAN.create(sw);
                if (watchling != null) {
                    watchling.refreshPositionAndAngles(savedPos.x, savedPos.y, savedPos.z, 0, 0);
                    sw.spawnEntity(watchling);
                }
            }
            phase = 3;
            phaseTick = 0;
        } else if (phase == 3 && phaseTick >= 160) {
            endersent.requestTeleport(savedPos.x, savedPos.y, savedPos.z);
            endersent.setInvisible(false);
            endersent.setInvulnerable(false);
            endersent.triggerAnim("action_controller", "teleport");
            phase = 0;
        }
    }

    @Override
    public void stop() {
        phase = 0;
        cooldown = cooldownMax;
    }
}
