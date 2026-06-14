package com.norako.fracturia;

import com.norako.fracturia.entity.FracturiaEntities;
import com.norako.fracturia.entity.client.overworld.illagers.IllusionerCloneRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.IllusionerRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.MountaineerRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.SquallGolemRenderer;
import com.norako.fracturia.entity.client.overworld.whisperer.PoisonVineRenderer;
import com.norako.fracturia.entity.client.overworld.whisperer.WhispererRenderer;
import com.norako.fracturia.entity.client.overworld.illagers.WindcallerRenderer;
import com.norako.fracturia.difficulty.DifficultyManager;
import com.norako.fracturia.network.SyncDifficultyPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

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

        ClientPlayNetworking.registerGlobalReceiver(SyncDifficultyPayload.ID, (payload, context) ->
                DifficultyManager.setCurrent(payload.difficulty())
        );
    }
}
