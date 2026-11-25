package dev.crystallmoon.dfufast.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Defers the eager baking of DFU migration rules.
 *
 * DFU itself remains enabled. If an old world needs conversion, Minecraft
 * can still build and use the required rules when they are needed.
 */
@Mixin(SharedConstants.class)
public abstract class SharedConstantsMixin {
    /**
     * @author DFU Fast
     * @reason Avoid eagerly baking every historical migration rule at startup.
     */
    @Overwrite
    public static void enableDataFixerOptimization() {
        // Intentionally defer the optimization work until it is needed.
    }
}
