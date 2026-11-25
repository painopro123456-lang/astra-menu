package dev.crystallmoon.dfufast.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Defers the expensive eager baking of DFU migration rules.
 *
 * DFU itself remains enabled. If an old world actually needs conversion,
 * Minecraft can still build and use the required rules at that point.
 */
@Mixin(SharedConstants.class)
public abstract class SharedConstantsMixin {
    /**
     * @author DFU Fast
     * @reason Avoid eagerly baking every historical migration rule at startup.
     */
    @Overwrite
    public static void enableDataFixerOptimizations() {
        // Intentionally deferred; do not eagerly bake all DFU rules on launch.
    }
}
