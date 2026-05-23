package org.fentanylsolutions.vintagepickupnotifier.network;

import net.minecraft.item.ItemStack;

import org.fentanylsolutions.vintagepickupnotifier.VintagePickUpNotifier;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class ClientboundTakeItemStackMessage implements IMessage {

    private int entityId;
    private ItemStack itemStack;

    public ClientboundTakeItemStackMessage() {}

    public ClientboundTakeItemStackMessage(int entityId, ItemStack itemStack) {
        this.entityId = entityId;
        this.itemStack = itemStack == null ? null : itemStack.copy();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.itemStack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeItemStack(buf, this.itemStack);
    }

    public static class Handler implements IMessageHandler<ClientboundTakeItemStackMessage, IMessage> {

        @Override
        public IMessage onMessage(ClientboundTakeItemStackMessage message, MessageContext ctx) {
            VintagePickUpNotifier.proxy.handleTakeItemStackMessage(message.entityId, message.itemStack);
            return null;
        }
    }
}
