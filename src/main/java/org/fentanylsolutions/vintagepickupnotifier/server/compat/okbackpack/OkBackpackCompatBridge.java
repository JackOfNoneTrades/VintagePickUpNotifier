package org.fentanylsolutions.vintagepickupnotifier.server.compat.okbackpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;

import baubles.api.BaublesApi;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers.BackpackContext;

final class OkBackpackCompatBridge {

    private OkBackpackCompatBridge() {}

    static int getPickupAmount(EntityPlayer player, ItemStack stack) {
        ItemStack remaining = stack.copy();
        remaining = simulatePickup(player, BaublesApi.getBaubles(player), remaining, InventoryTypes.BAUBLES);
        if (remaining != null) {
            remaining = simulatePickup(player, player.inventory, remaining, InventoryTypes.PLAYER);
        }

        int remainingAmount = remaining == null ? 0 : remaining.stackSize;
        return Math.max(0, stack.stackSize - remainingAmount);
    }

    private static ItemStack simulatePickup(EntityPlayer player, IInventory inventory, ItemStack stack,
        InventoryType type) {
        if (inventory == null || stack == null || stack.stackSize <= 0) {
            return stack;
        }

        ItemStack remaining = stack;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            BackpackContext context = BackpackEntityHelpers.getBackpack(player, type, slot);
            if (context == null || !context.getWrapper()
                .canPickupItem(remaining)) {
                continue;
            }

            remaining = context.getWrapper()
                .insertItem(remaining, true);
            if (remaining == null || remaining.stackSize <= 0) {
                return null;
            }
        }

        return remaining;
    }
}
