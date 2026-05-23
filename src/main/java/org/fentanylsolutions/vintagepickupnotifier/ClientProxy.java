package org.fentanylsolutions.vintagepickupnotifier;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import org.fentanylsolutions.vintagepickupnotifier.client.command.CommandTestEntries;
import org.fentanylsolutions.vintagepickupnotifier.client.handler.AddEntriesHandler;
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

    @Override
    public void handleTakeItemStackMessage(final int entityId, ItemStack itemStack) {
        final ItemStack copiedStack = itemStack == null ? null : itemStack.copy();
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    AddEntriesHandler.addItemStackEntry(entityId, copiedStack);
                }
            });
    }
}
