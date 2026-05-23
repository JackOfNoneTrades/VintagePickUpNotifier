package org.fentanylsolutions.vintagepickupnotifier.client.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import org.fentanylsolutions.vintagepickupnotifier.client.handler.DrawEntriesHandler;

public class CommandTestEntries extends CommandBase {

    @Override
    public String getCommandName() {
        return "vpun_test";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/vpun_test";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        DrawEntriesHandler.INSTANCE.addDebugEntries();
        sender.addChatMessage(new ChatComponentText("Added test pickup entries."));
    }
}
