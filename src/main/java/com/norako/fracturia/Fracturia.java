package com.norako.fracturia;

import com.norako.fracturia.block.FracturiaBlocks;
import com.norako.fracturia.entity.FracturiaEntities;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerCloneEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.MountaineerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.SquallGolemEntity;
import com.norako.fracturia.entity.custom.overworld.whisperer.PoisonVineEntity;
import com.norako.fracturia.entity.custom.overworld.whisperer.WhispererEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.WindcallerEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import com.norako.fracturia.difficulty.Difficulty;
import com.norako.fracturia.difficulty.DifficultyState;
import com.norako.fracturia.network.ChangeDifficultyPayload;
import com.norako.fracturia.network.SyncDifficultyPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import com.norako.fracturia.item.FracturiaItemGroups;
import com.norako.fracturia.item.FracturiaItems;
import com.norako.fracturia.sound.FracturiaSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
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
        FracturiaBlocks.registerFracturiaBlocks();
        FracturiaItemGroups.registerItemGroups();
        FracturiaEntities.registerFracturiaEntities();
        FracturiaSounds.registerSounds();

        FabricDefaultAttributeRegistry.register(FracturiaEntities.MOUNTAINEER_ENTITY, MountaineerEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.WINDCALLER_ENTITY, WindcallerEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.SQUALL_GOLEM_ENTITY, SquallGolemEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.ILLUSIONER_ENTITY, IllusionerEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.ILLUSIONER_CLONE_ENTITY, IllusionerCloneEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.WHISPERER_ENTITY, WhispererEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.POISON_VINE_ENTITY, PoisonVineEntity.setAttributes());

        PayloadTypeRegistry.playC2S().register(ChangeDifficultyPayload.ID, ChangeDifficultyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncDifficultyPayload.ID, SyncDifficultyPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ChangeDifficultyPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!player.hasPermissionLevel(2)) return;

            DifficultyState state = DifficultyState.get(player.getServerWorld());
            state.setDifficulty(payload.difficulty());

            SyncDifficultyPayload sync = new SyncDifficultyPayload(payload.difficulty());
            context.server().getPlayerManager().getPlayerList().forEach(p ->
                    ServerPlayNetworking.send(p, sync)
            );
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                DifficultyState.get(server.getOverworld())
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Difficulty diff = DifficultyState.get(server.getOverworld()).getDifficulty();
            ServerPlayNetworking.send(handler.player, new SyncDifficultyPayload(diff));
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return;
            Difficulty diff = DifficultyState.get(newPlayer.getServerWorld()).getDifficulty();
            if (!diff.isActive()) return;
            newPlayer.changeGameMode(GameMode.SPECTATOR);
        });
    }
}