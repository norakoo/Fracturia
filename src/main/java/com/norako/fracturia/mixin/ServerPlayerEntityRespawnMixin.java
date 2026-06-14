package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.FracturiaDifficulty;
import com.norako.fracturia.difficulty.FracturiaDifficultyState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityRespawnMixin {

    // On "Respawn" click from death screen: switch to spectator instead of respawning
    @Inject(method = "requestRespawn", at = @At("HEAD"), cancellable = true)
    private void fracturia_preventRespawn(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        ServerWorld world = player.getServerWorld();

        FracturiaDifficulty diff = FracturiaDifficultyState.get(world).getDifficulty();
        if (!diff.isActive()) return;

        player.setHealth(player.getMaxHealth());
        player.changeGameMode(GameMode.SPECTATOR);
        ci.cancel();
    }
}
