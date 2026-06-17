package com.norako.fracturia.entity.custom.end.endersent.goal;

import com.norako.fracturia.entity.custom.end.endersent.EndersentEntity;
import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;

public class TeleportSmashGoal extends Goal {

    private final EndersentEntity endersent;
    private final double minRange;
    private final int cooldownMax;
    private int cooldown = 0;
    private int phase = 0;
    private int phaseTick = 0;

    public TeleportSmashGoal(EndersentEntity endersent, double minRange, int cooldownMax) {
        this.endersent = endersent;
        this.minRange = minRange;
        this.cooldownMax = cooldownMax;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (--cooldown > 0) return false;
        LivingEntity target = endersent.getTarget();
        if (target == null || !target.isAlive()) return false;
        return endersent.squaredDistanceTo(target) > minRange * minRange;
    }

    @Override
    public boolean shouldContinue() {
        return phase > 0;
    }

    @Override
    public void start() {
        phase = 1;
        phaseTick = 0;
        endersent.getNavigation().stop();
        endersent.triggerAnim("action_controller", "teleport");
        endersent.isDoingAbility = true;
    }

    @Override
    public void tick() {
        phaseTick++;
        LivingEntity target = endersent.getTarget();

        if (phase == 1 && phaseTick >= 20) {
            if (target != null) {
                Vec3d look = target.getRotationVec(1.0f);
                Vec3d behind = target.getPos().subtract(look.x * 2 , 0, look.z * 2);
                endersent.requestTeleport(behind.x, behind.y, behind.z);

            }
            phase = 2;
            phaseTick = 0;
            if (endersent.getWorld() instanceof ServerWorld sw) {
                sw.playSound(null, endersent.getBlockPos(),
                        FracturiaSounds.ENDERSENT_TELEPORT_SMASH,
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
            }
        } else if (phase == 2 && phaseTick >= 9) {
            endersent.triggerAnim("action_controller", "attack");
            phase = 3;
            phaseTick = 0;
            // Au début de phase 2, juste après la téléportation
            endersent.getWorld().playSound(null, endersent.getBlockPos(),
                    FracturiaSounds.ENDERSENT_TELEPORT_SMASH,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
        } else if (phase == 3 && phaseTick >= 12) {
            // Dégâts après que l'anim ait commencé
            if (endersent.getWorld() instanceof ServerWorld sw) {
                List<LivingEntity> nearby = sw.getEntitiesByClass(LivingEntity.class,
                        endersent.getBoundingBox().expand(4),
                        e -> e != endersent && e instanceof PlayerEntity);
                for (LivingEntity t : nearby) {
                    t.damage(sw.getDamageSources().mobAttack(endersent), 15.0f);
                    t.takeKnockback(4.0, endersent.getX() - t.getX(), endersent.getZ() - t.getZ());
                }
            }
            phase = 4;
            phaseTick = 0;
        } else if (phase == 4 && phaseTick >= 10) {
            phase = 0;
        }
    }

    @Override
    public void stop() {
        phase = 0;
        cooldown = cooldownMax;
        endersent.isDoingAbility = false;
    }
}
