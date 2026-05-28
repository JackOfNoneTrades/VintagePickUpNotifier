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
        if (DrawEntriesHandler.INSTANCE.isEntityHandled(entityId)) {
            return;
        }

        Entity entity = world.getEntityByID(entityId);
        boolean added = false;
        if (entity instanceof EntityItem) {
            added = addItemEntityEntry((EntityItem) entity);
        } else if (entity instanceof EntityArrow) {
            added = addArrowEntry();
        } else if (entity instanceof EntityXPOrb) {
            added = addExperienceEntry((EntityXPOrb) entity);
        }

        if (added) {
            DrawEntriesHandler.INSTANCE.addHandledEntity(entityId);
        }
    }

    public static void addItemStackEntry(int entityId, ItemStack itemStack) {
        if (Config.clientOnly) {
            return;
        }
        if (DrawEntriesHandler.INSTANCE.isEntityHandled(entityId)) {
            return;
        }

        DrawEntriesHandler.INSTANCE.addHandledEntity(entityId);
        addItemEntry(itemStack);
    }

    public static void addExperienceEntry(int entityId, int experienceValue, int tickCount) {
        if (Config.clientOnly) {
            return;
        }
        if (DrawEntriesHandler.INSTANCE.isEntityHandled(entityId)) {
            return;
        }

        if (addExperienceEntry("Experience", experienceValue, tickCount)) {
            DrawEntriesHandler.INSTANCE.addHandledEntity(entityId);
        }
    }

    public static void addItemEntry(ItemStack itemStack) {
        if (Config.includeItems && itemStack != null) {
            addItemEntry(itemStack, itemStack.stackSize);
        }
    }

    private static boolean addItemEntityEntry(EntityItem entityItem) {
        if (!Config.includeItems) {
            return false;
        }

        ItemStack itemStack = entityItem.getEntityItem();
        if (itemStack == null || itemStack.getItem() == null || itemStack.stackSize <= 0) {
            return false;
        }

        int amount = getPickupAmount(itemStack);
        if (!Config.partialPickUps && amount < itemStack.stackSize) {
            return false;
        }

        return addItemEntry(itemStack, amount);
    }

    private static boolean addArrowEntry() {
        return Config.includeArrows && addItemEntry(new ItemStack(Items.arrow), 1);
    }

    private static boolean addExperienceEntry(EntityXPOrb orb) {
        if (orb.xpValue <= 0) {
            return false;
        }

        return addExperienceEntry(orb.getCommandSenderName(), orb.xpValue, orb.xpColor);
    }

    private static boolean addExperienceEntry(String name, int experienceValue, int tickCount) {
        if (!Config.includeExperience || experienceValue <= 0) {
            return false;
        }

        int amount = Config.experienceValue ? experienceValue : 1;
        DrawEntriesHandler.INSTANCE.addEntry(new ExperienceDisplayEntry(name, amount, tickCount));
        return true;
    }

    private static boolean addItemEntry(ItemStack itemStack, int amount) {
        if (itemStack == null || itemStack.getItem() == null
            || itemStack.stackSize <= 0
            || amount <= 0
            || Config.isItemHidden(itemStack)) {
            return false;
        }

        DrawEntriesHandler.INSTANCE.addEntry(new ItemDisplayEntry(itemStack, amount));
        return true;
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
