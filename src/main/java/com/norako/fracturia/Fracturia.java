package com.norako.fracturia;

import com.norako.fracturia.block.FracturiaBlocks;
import com.norako.fracturia.difficulty.FracturiaDifficulty;
import com.norako.fracturia.difficulty.FracturiaDifficultyState;
import com.norako.fracturia.effect.FracturiaEffects;
import com.norako.fracturia.entity.FracturiaEntities;
import com.norako.fracturia.entity.custom.end.endersent.*;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerCloneEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.MountaineerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.SquallGolemEntity;
import com.norako.fracturia.entity.custom.overworld.whisperer.PoisonVineEntity;
import com.norako.fracturia.entity.custom.overworld.whisperer.WhispererEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.WindcallerEntity;
import com.norako.fracturia.item.FracturiaItemGroups;
import com.norako.fracturia.item.FracturiaItems;
import com.norako.fracturia.network.ChangeDifficultyPayload;
import com.norako.fracturia.network.SyncDifficultyPayload;
import com.norako.fracturia.sound.FracturiaSounds;
import com.norako.fracturia.world.EndersentWorldState;
import com.norako.fracturia.world.FracturiaOreGeneration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.item.Item;
import net.minecraft.world.Heightmap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.norako.fracturia.advancement.FracturiaCriteria;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        FracturiaOreGeneration.generateOres();
        FracturiaEffects.registerEffects();
        FracturiaCriteria.register();
        FracturiaAttachments.register();

        FabricDefaultAttributeRegistry.register(FracturiaEntities.MOUNTAINEER_ENTITY, MountaineerEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.WINDCALLER_ENTITY, WindcallerEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.SQUALL_GOLEM_ENTITY, SquallGolemEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.ILLUSIONER_ENTITY, IllusionerEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.ILLUSIONER_CLONE_ENTITY, IllusionerCloneEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.WHISPERER_ENTITY, WhispererEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.POISON_VINE_ENTITY, PoisonVineEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.ENDERSENT, CommonEndersentEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.BINDING_ENDERSENT, BindingEndersentEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.BLIGHT_ENDERSENT, BlightEndersentEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.RAVENOUS_ENDERSENT, RavenousEndersentEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.REAPING_ENDERSENT, ReapingEndersentEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.SAVAGE_ENDERSENT, SavageEndersentEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.SPIKED_ENDERSENT, SpikedEndersentEntity.setAttributes());

        // Register network payload types
        PayloadTypeRegistry.playC2S().register(ChangeDifficultyPayload.ID, ChangeDifficultyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncDifficultyPayload.ID, SyncDifficultyPayload.CODEC);

        // Apply difficulty on first join only — locked forever after
        ServerPlayNetworking.registerGlobalReceiver(ChangeDifficultyPayload.ID, (payload, context) -> {
            FracturiaDifficultyState state = FracturiaDifficultyState.get(context.player().getServerWorld());
            if (state.isInitialized()) return;
            state.initialize(payload.difficulty());
            if (payload.difficulty().isActive()) {
                context.server().setDifficulty(net.minecraft.world.Difficulty.HARD, true);
            }
            SyncDifficultyPayload sync = new SyncDifficultyPayload(payload.difficulty(), true);
            context.server().getPlayerManager().getPlayerList().forEach(p ->
                ServerPlayNetworking.send(p, sync)
            );
        });

        // Load difficulty state on server start (updates static cache)
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            FracturiaDifficultyState state = FracturiaDifficultyState.get(server.getOverworld());
            if (state.getDifficulty().isActive()) {
                server.setDifficulty(net.minecraft.world.Difficulty.HARD, true);
            }
        });

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

        Map<UUID, Integer> voidedMaxTicks = new HashMap<>();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (net.minecraft.server.network.ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                StatusEffectInstance voided = player.getStatusEffect(FracturiaEffects.VOIDED);
                if (voided == null) {
                    Integer level = player.getAttached(FracturiaAttachments.VOIDED_LEVEL);
                    if (level != null && level > 0) {
                        player.getWorld().playSound(null, player.getBlockPos(),
                                FracturiaSounds.VOID_EFFECT_STOP, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                    }
                    player.setAttached(FracturiaAttachments.VOIDED_LEVEL, 0);
                    voidedMaxTicks.remove(player.getUuid());
                } else {
                    Integer level = player.getAttached(FracturiaAttachments.VOIDED_LEVEL);
                    if (level != null && level >= 99) {
                        int ticks = voidedMaxTicks.getOrDefault(player.getUuid(), 0) + 1;
                        voidedMaxTicks.put(player.getUuid(), ticks);
                        if (ticks >= 600) {
                            FracturiaCriteria.SURVIVED_VOIDED.trigger(player);
                            voidedMaxTicks.remove(player.getUuid());
                        }
                    } else {
                        voidedMaxTicks.remove(player.getUuid());
                    }
                }
            }
        });

        Set<Item> endersentEyes = Set.of(
                FracturiaItems.BINDING_EYE,
                FracturiaItems.BLIGHT_EYE,
                FracturiaItems.RAVENOUS_EYE,
                FracturiaItems.REAPING_EYE,
                FracturiaItems.SAVAGE_EYE,
                FracturiaItems.SPIKED_EYE
        );

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (!state.isOf(Blocks.END_PORTAL_FRAME)) return ActionResult.PASS;
            if (state.get(EndPortalFrameBlock.EYE)) return ActionResult.PASS;
            ItemStack stack = player.getStackInHand(hand);
            if (!endersentEyes.contains(stack.getItem())) return ActionResult.PASS;
            if (!world.isClient()) {
                world.setBlockState(pos, state.with(EndPortalFrameBlock.EYE, true), 3);
                world.updateComparators(pos, Blocks.END_PORTAL_FRAME);
                if (!player.isCreative()) stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        });

        // Binding - Désert
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.DESERT),
                SpawnGroup.MONSTER, FracturiaEntities.BINDING_ENDERSENT, 1, 1, 1);
        SpawnRestriction.register(FracturiaEntities.BINDING_ENDERSENT,
                SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> true);

        // Blight - Marais
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP),
                SpawnGroup.MONSTER, FracturiaEntities.BLIGHT_ENDERSENT, 1, 1, 1);
        SpawnRestriction.register(FracturiaEntities.BLIGHT_ENDERSENT,
                SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> true);

        // Ravenous - Grottes (tous biomes, filtré dans canSpawn)
        BiomeModifications.addSpawn(BiomeSelectors.all(),
                SpawnGroup.MONSTER, FracturiaEntities.RAVENOUS_ENDERSENT, 1, 1, 1);

        // Reaping - Stronghold (tous biomes, filtré dans canSpawn)
        BiomeModifications.addSpawn(BiomeSelectors.all(),
                SpawnGroup.MONSTER, FracturiaEntities.REAPING_ENDERSENT, 1, 1, 1);

        // Savage - Savane
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SAVANNA, BiomeKeys.SAVANNA_PLATEAU, BiomeKeys.WINDSWEPT_SAVANNA),
                SpawnGroup.MONSTER, FracturiaEntities.SAVAGE_ENDERSENT, 1, 1, 1);
        SpawnRestriction.register(FracturiaEntities.SAVAGE_ENDERSENT,
                SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> true);

        // Spiked - Forêts
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(
                        BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST,
                        BiomeKeys.TAIGA, BiomeKeys.OLD_GROWTH_PINE_TAIGA, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA,
                        BiomeKeys.DARK_FOREST),
                SpawnGroup.MONSTER, FracturiaEntities.SPIKED_ENDERSENT, 1, 1, 1);
        SpawnRestriction.register(FracturiaEntities.SPIKED_ENDERSENT,
                SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (type, world, reason, pos, random) -> true);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 40 != 0) return;
            EndersentWorldState state = EndersentWorldState.get(server);
            if (state.isPortalRoomVisited()) return;
            for (net.minecraft.server.network.ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                net.minecraft.util.math.BlockPos pos = player.getBlockPos();
                for (net.minecraft.util.math.BlockPos check : net.minecraft.util.math.BlockPos.iterateOutwards(pos, 8, 4, 8)) {
                    if (player.getServerWorld().getBlockState(check).isOf(net.minecraft.block.Blocks.END_PORTAL_FRAME)) {
                        state.setPortalRoomVisited();
                        FracturiaCriteria.ENTERED_PORTAL_ROOM.trigger(player);
                        break;
                    }
                }
            }
        });
    }
}
