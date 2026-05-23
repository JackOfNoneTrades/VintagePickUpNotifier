package org.fentanylsolutions.vintagepickupnotifier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.fentanylsolutions.vintagepickupnotifier.network.NetworkHandler;
import org.fentanylsolutions.vintagepickupnotifier.server.handler.ItemPickupHandler;
import org.fentanylsolutions.vintagepickupnotifier.server.handler.OkBackpackPickupHandler;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.loadConfig(event.getSuggestedConfigurationFile());
        NetworkHandler.registerMessages();
        VintagePickUpNotifier.LOG.info("Loaded config from {}", Config.getConfigFile());
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(ItemPickupHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(OkBackpackPickupHandler.INSTANCE);
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {}

    public void handleTakeItemStackMessage(int entityId, ItemStack itemStack) {}
}
