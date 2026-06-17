package com.norako.fracturia.sound;

import com.norako.fracturia.effect.FracturiaEffects;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

public class VoidedLoopSound extends MovingSoundInstance {
    private final PlayerEntity player;

    public VoidedLoopSound(PlayerEntity player) {
        super(FracturiaSounds.VOID_EFFECT_LOOP, SoundCategory.PLAYERS, SoundInstance.createRandom());
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.8f;
    }

    @Override
    public void tick() {
        if (!player.isAlive() || player.getStatusEffect(FracturiaEffects.VOIDED) == null) {
            this.setDone();
            return;
        }
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }
}