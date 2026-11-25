package dev.crystallmoon.dfufast.mixin;

import dev.crystallmoon.dfufast.DfuFast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records when Minecraft 26.2 installs its title screen.
 * In 26.2 screen management moved from Minecraft to Gui.
 */
@Mixin(targets = "net.minecraft.client.gui.Gui")
public abstract class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("TAIL"), require = 1)
    private void dfuFast$recordTitleScreen(@Coerce Object screen, CallbackInfo ci) {
        if (screen != null && screen.getClass().getSimpleName().equals("TitleScreen")) {
            DfuFast.onTitleScreenDisplayed();
        }
    }
}
