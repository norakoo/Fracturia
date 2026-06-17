package com.norako.fracturia.entity.custom.end.endersent.goal;

import com.norako.fracturia.entity.custom.end.endersent.EndersentEntity;
import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumSet;
import java.util.List;

public class EndersentSweepAttackGoal extends Goal {

    private final EndersentEntity endersent;
    private int cooldown = 0;
    private int tick = 0;
    private boolean sweep = false;
    private boolean active = false;

    public EndersentSweepAttackGoal(EndersentEntity endersent) {
        this.endersent = endersent;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (endersent.isDoingAbility) return false;
        if (--cooldown > 0) return false;
        LivingEntity target = endersent.getTarget();
        if (target == null || !target.isAlive()) return false;
        return endersent.squaredDistanceTo(target) <= 9.0;
    }

    @Override
    public boolean shouldContinue() { return active; }

    @Override
    public void start() {
        active = true;
        tick = 0;
        sweep = endersent.getRandom().nextBoolean();
        endersent.triggerAnim("action_controller", "attack");
        endersent.getNavigation().stop();
    }

    @Override
    public void tick() {
        tick++;
        LivingEntity target = endersent.getTarget();
        if (target != null) endersent.getLookControl().lookAt(target);

        if (tick == 35) {
            if (!endersent.getWorld().isClient()) {
                endersent.getWorld().playSound(null, endersent.getBlockPos(),
                        FracturiaSounds.ENDERSENT_ATTACK,
                        net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 1.0f);
            }// 1.25s = impact dans l'anim
            if (endersent.getWorld() instanceof ServerWorld sw) {
                if (sweep) {
                    List<LivingEntity> targets = sw.getEntitiesByClass(LivingEntity.class,
                            endersent.getBoundingBox().expand(4),
                            e -> e != endersent && e instanceof PlayerEntity);
                    for (LivingEntity t : targets) endersent.tryAttack(t);
                } else {
                    if (target != null) endersent.tryAttack(target);
                }
            }
        }

        if (tick >= 50) active = false;
    }

    @Override
    public void stop() {
        active = false;
        cooldown = 20;
    }
}