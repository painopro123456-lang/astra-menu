package dev.crystallmoon.dfufast.mixin;

import dev.crystallmoon.dfufast.DfuFast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records when the main menu is first installed as the active screen.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("TAIL"), require = 1)
    private void dfuFast$recordTitleScreen(Screen screen, CallbackInfo ci) {
        if (screen != null && screen.getClass().getSimpleName().equals("TitleScreen")) {
            DfuFast.onTitleScreenDisplayed();
        }
    }
}
