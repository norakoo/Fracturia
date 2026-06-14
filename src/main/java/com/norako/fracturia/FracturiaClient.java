package com.norako.fracturia;

import com.norako.fracturia.difficulty.FracturiaDifficultyManager;
import com.norako.fracturia.network.ChangeDifficultyPayload;
import com.norako.fracturia.network.SyncDifficultyPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FracturiaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient()
    {
        ClientPlayNetworking.registerGlobalReceiver(SyncDifficultyPayload.ID, (payload, context) -> {
            FracturiaDifficultyManager.setCurrent(payload.difficulty());
            // World not yet initialized — send the difficulty chosen at creation
            if (!payload.initialized()) {
                ClientPlayNetworking.send(new ChangeDifficultyPayload(FracturiaDifficultyManager.getPending()));
            }
        });
    }
}
