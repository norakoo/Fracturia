package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.Difficulty;
import com.norako.fracturia.difficulty.DifficultyState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityRespawnMixin {

    @Inject(method = "requestRespawn", at = @At("HEAD"), cancellable = true)
    private void fracturia_preventRespawn(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        ServerWorld world = player.getServerWorld();

        Difficulty diff = DifficultyState.get(world).getDifficulty();
        if (!diff.isActive()) return;

        player.setHealth(player.getMaxHealth());
        player.changeGameMode(GameMode.SPECTATOR);
        ci.cancel();
    }
}
