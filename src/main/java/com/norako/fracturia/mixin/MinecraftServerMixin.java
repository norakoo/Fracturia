package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.FracturiaDifficultyState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "setDifficulty", at = @At("HEAD"), cancellable = true)
    private void fracturia_lockDifficulty(Difficulty difficulty, boolean forced, CallbackInfo ci) {
        if (difficulty == Difficulty.HARD) return;
        MinecraftServer server = (MinecraftServer)(Object)this;
        FracturiaDifficultyState state = FracturiaDifficultyState.get(server.getOverworld());
        if (state.isInitialized() && state.getDifficulty().isActive()) {
            ci.cancel();
        }
    }
}