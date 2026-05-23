package org.fentanylsolutions.vintagepickupnotifier.server.handler;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.network.ClientboundTakeItemStackMessage;
import org.fentanylsolutions.vintagepickupnotifier.network.NetworkHandler;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ItemPickupHandler {

    public static final ItemPickupHandler INSTANCE = new ItemPickupHandler();

    private ItemPickupHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityItemPickup(EntityItemPickupEvent event) {
        if (Config.clientOnly || event.item.worldObj.isRemote
            || event.getResult() != Event.Result.DEFAULT
            || !(event.entityPlayer instanceof EntityPlayerMP)
            || !mayPlayerPickUp(event.item, event.entityPlayer)) {
            return;
        }

        ItemStack itemStack = event.item.getEntityItem();
        if (itemStack == null || itemStack.getItem() == null || itemStack.stackSize <= 0) {
            return;
        }

        int pickedAmount = Math
            .min(itemStack.stackSize, getAvailableInventorySpace(event.entityPlayer.inventory, itemStack));
        if (pickedAmount <= 0) {
            return;
        }

        ItemStack pickedStack = itemStack.copy();
        pickedStack.stackSize = pickedAmount;
        NetworkHandler.CHANNEL.sendTo(
            new ClientboundTakeItemStackMessage(event.item.getEntityId(), pickedStack),
            (EntityPlayerMP) event.entityPlayer);
    }

    private static boolean mayPlayerPickUp(EntityItem entityItem, EntityPlayer player) {
        String owner = entityItem.func_145798_i();
        return owner == null || entityItem.lifespan - entityItem.age <= 200
            || owner.equals(player.getCommandSenderName());
    }

    private static int getAvailableInventorySpace(InventoryPlayer inventory, ItemStack itemStack) {
        if (itemStack.getMaxStackSize() == 1 || itemStack.isItemDamaged()) {
            return inventory.getFirstEmptyStack() >= 0 ? itemStack.stackSize : 0;
        }

        int accepted = 0;
        int remaining = itemStack.stackSize;

        for (ItemStack inventoryStack : inventory.mainInventory) {
            if (remaining <= 0) {
                return accepted;
            }
            if (inventoryStack == null || !canMergeStacks(inventory, inventoryStack, itemStack)) {
                continue;
            }

            int moved = Math.min(remaining, getStackSpace(inventory, inventoryStack));
            accepted += moved;
            remaining -= moved;
        }

        int emptySlotLimit = Math.min(itemStack.getMaxStackSize(), inventory.getInventoryStackLimit());
        for (ItemStack inventoryStack : inventory.mainInventory) {
            if (remaining <= 0) {
                return accepted;
            }
            if (inventoryStack != null) {
                continue;
            }

            int moved = Math.min(remaining, emptySlotLimit);
            accepted += moved;
            remaining -= moved;
        }

        return accepted;
    }

    private static boolean canMergeStacks(InventoryPlayer inventory, ItemStack inventoryStack, ItemStack itemStack) {
        return inventoryStack.getItem() == itemStack.getItem() && inventoryStack.isStackable()
            && inventoryStack.stackSize < inventoryStack.getMaxStackSize()
            && inventoryStack.stackSize < inventory.getInventoryStackLimit()
            && (!inventoryStack.getHasSubtypes() || inventoryStack.getItemDamage() == itemStack.getItemDamage())
            && ItemStack.areItemStackTagsEqual(inventoryStack, itemStack);
    }

    private static int getStackSpace(InventoryPlayer inventory, ItemStack inventoryStack) {
        int stackLimit = Math.min(inventoryStack.getMaxStackSize(), inventory.getInventoryStackLimit());
        return Math.max(0, stackLimit - inventoryStack.stackSize);
    }
}
