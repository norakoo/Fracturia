package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.FracturiaDifficulty;
import com.norako.fracturia.difficulty.FracturiaDifficultyManager;
import com.norako.fracturia.network.ChangeDifficultyPayload;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void fracturia_addDifficultyButton(CallbackInfo ci) {
        ClientPlayNetworkHandler handler = this.client != null ? this.client.getNetworkHandler() : null;
        if (handler == null) return;

        FracturiaDifficulty current = FracturiaDifficultyManager.getCurrent();
        ButtonWidget[] holder = new ButtonWidget[1];

        holder[0] = ButtonWidget.builder(buildText(current), btn -> {
            FracturiaDifficulty next = FracturiaDifficultyManager.getCurrent().next();
            FracturiaDifficultyManager.setCurrent(next);
            ClientPlayNetworking.send(new ChangeDifficultyPayload(next));
            btn.setMessage(buildText(next));
        }).dimensions(this.width / 2 - 102, this.height / 4 + 168, 204, 20).build();

        this.addDrawableChild(holder[0]);
    }

    private static Text buildText(FracturiaDifficulty diff) {
        return Text.translatable("fracturia.difficulty.button",
            Text.translatable("fracturia.difficulty." + diff.getId()));
    }
}
