package com.norako.fracturia;

import com.norako.fracturia.block.FracturiaBlocks;
import com.norako.fracturia.difficulty.FracturiaDifficulty;
import com.norako.fracturia.difficulty.FracturiaDifficultyState;
import com.norako.fracturia.item.FracturiaItemGroups;
import com.norako.fracturia.item.FracturiaItems;
import com.norako.fracturia.network.ChangeDifficultyPayload;
import com.norako.fracturia.network.SyncDifficultyPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fracturia implements ModInitializer {
	public static final String MOD_ID = "fracturia";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
    {
        FracturiaItems.registerFracturiaItems();
        FracturiaBlocks.registerModBlocks();
        FracturiaItemGroups.registerItemGroups();

        // Register network payload types
        PayloadTypeRegistry.playC2S().register(ChangeDifficultyPayload.ID, ChangeDifficultyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncDifficultyPayload.ID, SyncDifficultyPayload.CODEC);

        // Apply difficulty on first join only — locked forever after
        ServerPlayNetworking.registerGlobalReceiver(ChangeDifficultyPayload.ID, (payload, context) -> {
            FracturiaDifficultyState state = FracturiaDifficultyState.get(context.player().getServerWorld());
            if (state.isInitialized()) return;
            state.initialize(payload.difficulty());
            SyncDifficultyPayload sync = new SyncDifficultyPayload(payload.difficulty(), true);
            context.server().getPlayerManager().getPlayerList().forEach(p ->
                ServerPlayNetworking.send(p, sync)
            );
        });

        // Load difficulty state on server start (updates static cache)
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
            FracturiaDifficultyState.get(server.getOverworld())
        );

        // Sync difficulty (+ lock state) to player on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            FracturiaDifficultyState state = FracturiaDifficultyState.get(server.getOverworld());
            ServerPlayNetworking.send(handler.player, new SyncDifficultyPayload(state.getDifficulty(), state.isInitialized()));
        });

        // Permadeath: switch to spectator on death if Dément or Inconcevable
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return;
            FracturiaDifficulty diff = FracturiaDifficultyState.get(newPlayer.getServerWorld()).getDifficulty();
            if (!diff.isActive()) return;
            newPlayer.changeGameMode(GameMode.SPECTATOR);
        });
    }
}