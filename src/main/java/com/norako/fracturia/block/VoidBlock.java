package com.norako.fracturia.block;

import com.norako.fracturia.FracturiaAttachments;
import com.norako.fracturia.effect.FracturiaEffects;
import com.norako.fracturia.sound.FracturiaSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VoidBlock extends Block {

    public VoidBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient() || !(entity instanceof LivingEntity living)) return;

        StatusEffectInstance current = living.getStatusEffect(FracturiaEffects.VOIDED);
        Integer attachedLevel = living.getAttached(FracturiaAttachments.VOIDED_LEVEL);
        int currentLevel = (current != null && attachedLevel != null) ? attachedLevel : 0;

        int newLevel = currentLevel;
        if (current == null || world.getTime() % 20 == 0) {
            newLevel = Math.min(currentLevel + 1, 99);
        }

        if (current == null) {
            world.playSound(null, pos, FracturiaSounds.VOID_BLOCK_ENTER, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
        }

        living.setAttached(FracturiaAttachments.VOIDED_LEVEL, newLevel);
        living.addStatusEffect(new StatusEffectInstance(FracturiaEffects.VOIDED, 600, 0, false, true));

        if (entity instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(net.minecraft.text.Text.literal("§5Voided x" + newLevel), true);
        }
    }
}
