package org.fentanylsolutions.vintagepickupnotifier.mixins.early.minecraft;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S0DPacketCollectItem;

import org.fentanylsolutions.vintagepickupnotifier.client.handler.AddEntriesHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow
    private WorldClient clientWorldController;

    @Inject(method = "handleCollectItem", at = @At("HEAD"))
    private void vintagepickupnotifier$handleCollectItem(S0DPacketCollectItem packet, CallbackInfo ci) {
        AddEntriesHandler.onEntityPickup(this.clientWorldController, packet.func_149354_c(), packet.func_149353_d());
    }
}
