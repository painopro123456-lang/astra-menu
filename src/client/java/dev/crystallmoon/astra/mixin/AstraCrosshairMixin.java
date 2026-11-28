package dev.crystallmoon.astra.mixin;

import dev.crystallmoon.astra.AstraModules;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class AstraCrosshairMixin {
    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void astra$hideVanillaCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
            CallbackInfo ci) {
        if (AstraModules.isEnabled("Crosshair")) ci.cancel();
    }
}
