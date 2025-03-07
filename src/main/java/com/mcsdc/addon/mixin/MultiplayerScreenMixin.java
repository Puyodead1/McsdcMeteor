package com.mcsdc.addon.mixin;

import com.mcsdc.addon.gui.McsdcScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {

    protected MultiplayerScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    private void onInit(CallbackInfo info) {
        this.addDrawableChild(
            new ButtonWidget.Builder(
                Text.literal("Mcsdc"),
                onPress -> {
                    if (this.client == null) return;
                    this.client.setScreen(new McsdcScreen((MultiplayerScreen) (Object) this));
                }
            )
                .position(150, 3)
                .width(80)
                .build()
        );
    }
}
