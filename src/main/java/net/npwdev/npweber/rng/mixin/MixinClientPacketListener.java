package net.npwdev.npweber.rng.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.npwdev.npweber.rng.RNG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Inject(method = "handleSetTime", at = @At("HEAD"))
    public void handleSetTime(ClientboundSetTimePacket setTimePacket, CallbackInfo info) {
        RNG.getDaytime(setTimePacket.getDayTime());
    }

    @Inject(method = "handleDisconnect", at = @At("HEAD"))
    public void handleDisconnect(ClientboundDisconnectPacket disconnectPacket, CallbackInfo info) {
        if(disconnectPacket.getReason().getString().equals("Server closed"))
            RNG.disableDaytimeStore();
    }

    @Inject(method = "handleKeepAlive", at = @At("HEAD"))
    public void handleKeepAlive(ClientboundKeepAlivePacket keepAlivePacket, CallbackInfo info) {
        if(!RNG.hasCalibratedNanoTime())
            RNG.calibrateNanoTime(keepAlivePacket.getId());
    }
}
