package dev.crystallmoon.astra.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface AstraGameModeAccessor {
    @Accessor("destroyProgress")
    float astra$getDestroyProgress();

    @Accessor("isDestroying")
    boolean astra$isDestroying();

    @Accessor("destroyBlockPos")
    BlockPos astra$getDestroyBlockPos();
}
