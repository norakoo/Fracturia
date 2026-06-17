package com.norako.fracturia.mixin;

import com.norako.fracturia.difficulty.FracturiaDifficulty;
import com.norako.fracturia.difficulty.FracturiaDifficultyManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    @Unique
    private FracturiaDifficulty fracturia_selected = FracturiaDifficulty.NONE;

    protected CreateWorldScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void fracturia_addDifficultyButton(CallbackInfo ci) {
        // Keep pending in sync on every re-init (screen resize, tab switch)
        FracturiaDifficultyManager.setPending(fracturia_selected);
        this.addDrawableChild(ButtonWidget.builder(fracturia_buildLabel(fracturia_selected), btn -> {
            fracturia_selected = fracturia_selected.next();
            FracturiaDifficultyManager.setPending(fracturia_selected);
            btn.setMessage(fracturia_buildLabel(fracturia_selected));
        }).dimensions(this.width / 2 - 102, this.height / 4 + 116, 204, 20).build());
    }

    @Unique
    private static Text fracturia_buildLabel(FracturiaDifficulty diff) {
        return Text.translatable("fracturia.difficulty.button",
            Text.translatable("fracturia.difficulty." + diff.getId()));
    }
}
