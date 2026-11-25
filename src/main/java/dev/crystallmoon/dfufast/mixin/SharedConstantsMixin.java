package dev.crystallmoon.dfufast.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Defers the eager baking of DFU migration rules.
 *
 * DFU itself remains enabled. Only the startup optimization pass is skipped;
 * Minecraft retains its normal data conversion code for world loading.
 */
@Mixin(SharedConstants.class)
public abstract class SharedConstantsMixin {
    @Inject(
            method = "enableDataFixerOptimization",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private static void dfuFast$deferOptimization(CallbackInfo ci) {
        ci.cancel();
    }
}
