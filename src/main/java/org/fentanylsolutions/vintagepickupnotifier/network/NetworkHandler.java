package org.fentanylsolutions.vintagepickupnotifier.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("vpun");

    private static int discriminator = 0;

    public static void registerMessages() {
        CHANNEL.registerMessage(
            ClientboundTakeItemStackMessage.Handler.class,
            ClientboundTakeItemStackMessage.class,
            discriminator++,
            Side.CLIENT);
    }
}
