package org.fentanylsolutions.vintagepickupnotifier.network;

import org.fentanylsolutions.vintagepickupnotifier.VintagePickUpNotifier;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class ClientboundTakeExperienceMessage implements IMessage {

    private int entityId;
    private int experienceValue;
    private int tickCount;

    public ClientboundTakeExperienceMessage() {}

    public ClientboundTakeExperienceMessage(int entityId, int experienceValue, int tickCount) {
        this.entityId = entityId;
        this.experienceValue = experienceValue;
        this.tickCount = tickCount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.experienceValue = buf.readInt();
        this.tickCount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.experienceValue);
        buf.writeInt(this.tickCount);
    }

    public static class Handler implements IMessageHandler<ClientboundTakeExperienceMessage, IMessage> {

        @Override
        public IMessage onMessage(ClientboundTakeExperienceMessage message, MessageContext ctx) {
            VintagePickUpNotifier.proxy
                .handleTakeExperienceMessage(message.entityId, message.experienceValue, message.tickCount);
            return null;
        }
    }
}
