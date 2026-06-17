package com.norako.fracturia;

import com.norako.fracturia.difficulty.FracturiaDifficultyManager;
import com.norako.fracturia.entity.FracturiaEntities;
import com.norako.fracturia.entity.client.end.endersent.EndersentRenderer;
import com.norako.fracturia.entity.client.end.endersent.EndersentVariantRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.IllusionerCloneRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.IllusionerRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.MountaineerRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.SquallGolemRenderer;
import com.norako.fracturia.entity.client.overworld.whisperer.PoisonVineRenderer;
import com.norako.fracturia.entity.client.overworld.whisperer.WhispererRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.WindcallerRenderer;
import com.norako.fracturia.network.ChangeDifficultyPayload;
import com.norako.fracturia.network.SyncDifficultyPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.norako.fracturia.effect.FracturiaEffects;
import com.norako.fracturia.sound.VoidedLoopSound;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FracturiaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient()
    {
        EntityRendererRegistry.register(FracturiaEntities.MOUNTAINEER_ENTITY, MountaineerRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.WINDCALLER_ENTITY, WindcallerRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.SQUALL_GOLEM_ENTITY, SquallGolemRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.ILLUSIONER_ENTITY, IllusionerRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.ILLUSIONER_CLONE_ENTITY, IllusionerCloneRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.WHISPERER_ENTITY, WhispererRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.POISON_VINE_ENTITY, PoisonVineRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.ENDERSENT, EndersentRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.BINDING_ENDERSENT, EndersentVariantRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.BLIGHT_ENDERSENT, EndersentVariantRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.RAVENOUS_ENDERSENT, EndersentVariantRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.REAPING_ENDERSENT, EndersentVariantRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.SAVAGE_ENDERSENT, EndersentVariantRenderer::new);
        EntityRendererRegistry.register(FracturiaEntities.SPIKED_ENDERSENT, EndersentVariantRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(SyncDifficultyPayload.ID, (payload, context) -> {
            FracturiaDifficultyManager.setCurrent(payload.difficulty());
            // World not yet initialized — send the difficulty chosen at creation
            if (!payload.initialized()) {
                ClientPlayNetworking.send(new ChangeDifficultyPayload(FracturiaDifficultyManager.getPending()));
            }
        });

        VoidedLoopSound[] loopSound = { null };
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            boolean hasVoided = client.player.getStatusEffect(FracturiaEffects.VOIDED) != null;
            if (hasVoided && (loopSound[0] == null || loopSound[0].isDone())) {
                loopSound[0] = new VoidedLoopSound(client.player);
                client.getSoundManager().play(loopSound[0]);
            }
        });
    }
}
