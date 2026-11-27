package dev.crystallmoon.astra.mixin;

import dev.crystallmoon.astra.AstraHud;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class AstraServerTimeMixin {
    @Inject(method = "handleSetTime", at = @At("HEAD"))
    private void astra$trackServerTick(ClientboundSetTimePacket packet, CallbackInfo ci) {
        AstraHud.noteServerTime(packet.gameTime());
    }
}
