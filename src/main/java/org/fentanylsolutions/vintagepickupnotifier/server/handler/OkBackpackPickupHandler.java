package org.fentanylsolutions.vintagepickupnotifier.server.handler;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.network.ClientboundTakeItemStackMessage;
import org.fentanylsolutions.vintagepickupnotifier.network.NetworkHandler;
import org.fentanylsolutions.vintagepickupnotifier.server.compat.okbackpack.OkBackpackCompat;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class OkBackpackPickupHandler {

    public static final OkBackpackPickupHandler INSTANCE = new OkBackpackPickupHandler();

    private final Map<Integer, ItemStack> pendingPickups = new HashMap<>();

    private OkBackpackPickupHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void capturePickup(EntityItemPickupEvent event) {
        if (!isEnabled() || !isServerPlayerPickup(event) || event.isCanceled()) {
            return;
        }

        EntityItem entityItem = event.item;
        ItemStack itemStack = entityItem.getEntityItem();
        if (itemStack == null || itemStack.getItem() == null || itemStack.stackSize <= 0) {
            return;
        }

        int pickedAmount = OkBackpackCompat.getPickupAmount(event.entityPlayer, itemStack);
        if (pickedAmount <= 0 || !Config.partialPickUps && pickedAmount < itemStack.stackSize) {
            return;
        }

        ItemStack pickedStack = itemStack.copy();
        pickedStack.stackSize = pickedAmount;
        this.pendingPickups.put(entityItem.getEntityId(), pickedStack);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void sendPickup(EntityItemPickupEvent event) {
        if (!isEnabled() || !isServerPlayerPickup(event)) {
            return;
        }

        ItemStack pickedStack = this.pendingPickups.remove(event.item.getEntityId());
        if (pickedStack == null || !event.isCanceled() || !event.item.isDead) {
            return;
        }

        NetworkHandler.CHANNEL.sendTo(
            new ClientboundTakeItemStackMessage(event.item.getEntityId(), pickedStack),
            (EntityPlayerMP) event.entityPlayer);
    }

    private static boolean isEnabled() {
        return !Config.clientOnly && Config.backpackIntegration && OkBackpackCompat.isLoaded();
    }

    private static boolean isServerPlayerPickup(EntityItemPickupEvent event) {
        return event != null && event.item != null
            && !event.item.worldObj.isRemote
            && event.entityPlayer instanceof EntityPlayerMP;
    }
}
