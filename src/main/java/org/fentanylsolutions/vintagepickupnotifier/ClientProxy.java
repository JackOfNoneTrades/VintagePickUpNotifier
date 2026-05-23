package org.fentanylsolutions.vintagepickupnotifier;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import org.fentanylsolutions.vintagepickupnotifier.client.command.CommandTestEntries;
import org.fentanylsolutions.vintagepickupnotifier.client.handler.DrawEntriesHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(DrawEntriesHandler.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(DrawEntriesHandler.INSTANCE);
        ClientCommandHandler.instance.registerCommand(new CommandTestEntries());
    }
}
