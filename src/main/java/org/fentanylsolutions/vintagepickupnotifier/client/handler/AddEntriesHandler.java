package org.fentanylsolutions.vintagepickupnotifier.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.client.gui.entry.ExperienceDisplayEntry;
import org.fentanylsolutions.vintagepickupnotifier.client.gui.entry.ItemDisplayEntry;

public class AddEntriesHandler {

    public static void onEntityPickup(World world, int entityId, int playerId) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (world == null || minecraft.thePlayer == null || minecraft.thePlayer.getEntityId() != playerId) {
            return;
        }

        Entity entity = world.getEntityByID(entityId);
        if (entity instanceof EntityItem) {
            addItemEntityEntry((EntityItem) entity);
        } else if (entity instanceof EntityArrow) {
            addArrowEntry();
        } else if (entity instanceof EntityXPOrb) {
            addExperienceEntry((EntityXPOrb) entity);
        }
    }

    public static void addItemEntry(ItemStack itemStack) {
        if (Config.includeItems && itemStack != null) {
            addItemEntry(itemStack, itemStack.stackSize);
        }
    }

    private static void addItemEntityEntry(EntityItem entityItem) {
        if (!Config.includeItems) {
            return;
        }

        ItemStack itemStack = entityItem.getEntityItem();
        if (itemStack == null || itemStack.getItem() == null || itemStack.stackSize <= 0) {
            return;
        }

        int amount = getPickupAmount(itemStack);
        if (!Config.partialPickUps && amount < itemStack.stackSize) {
            return;
        }

        addItemEntry(itemStack, amount);
    }

    private static void addArrowEntry() {
        if (Config.includeArrows) {
            addItemEntry(new ItemStack(Items.arrow), 1);
        }
    }

    private static void addExperienceEntry(EntityXPOrb orb) {
        if (!Config.includeExperience || orb.xpValue <= 0) {
            return;
        }

        int amount = Config.experienceValue ? orb.xpValue : 1;
        DrawEntriesHandler.INSTANCE
            .addEntry(new ExperienceDisplayEntry(orb.getCommandSenderName(), amount, orb.xpColor));
    }

    private static void addItemEntry(ItemStack itemStack, int amount) {
        if (itemStack == null || itemStack.getItem() == null
            || itemStack.stackSize <= 0
            || amount <= 0
            || Config.isItemHidden(itemStack)) {
            return;
        }

        DrawEntriesHandler.INSTANCE.addEntry(new ItemDisplayEntry(itemStack, amount));
    }

    private static int getPickupAmount(ItemStack itemStack) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (itemStack == null || itemStack.getItem() == null
            || itemStack.stackSize <= 0
            || minecraft.thePlayer == null) {
            return 0;
        }
        if (minecraft.thePlayer.capabilities.isCreativeMode) {
            return itemStack.stackSize;
        }

        return Math.min(itemStack.stackSize, getAvailableInventorySpace(minecraft.thePlayer.inventory, itemStack));
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
