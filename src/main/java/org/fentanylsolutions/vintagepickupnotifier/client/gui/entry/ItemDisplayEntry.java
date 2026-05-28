package org.fentanylsolutions.vintagepickupnotifier.client.gui.entry;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.VintagePickUpNotifier;
import org.fentanylsolutions.vintagepickupnotifier.client.util.DisplayEntryRenderHelper;
import org.fentanylsolutions.vintagepickupnotifier.config.CombineEntries;
import org.lwjgl.opengl.GL11;

public final class ItemDisplayEntry extends DisplayEntry<ItemStack> {

    private static final int ITEM_STACK_POP_TIME = 5;

    public ItemDisplayEntry(ItemStack itemStack, int displayAmount) {
        super(copyForDisplay(itemStack), displayAmount, itemStack.getRarity());
        this.item.animationsToGo = ITEM_STACK_POP_TIME;
    }

    private static ItemStack copyForDisplay(ItemStack itemStack) {
        ItemStack copy = itemStack.copy();
        copy.stackSize = 1;
        return copy;
    }

    @Override
    protected String getEntryName(ItemStack itemStack) {
        if (Config.combineEntries == CombineEntries.ALWAYS) {
            return itemStack.getItem()
                .getItemStackDisplayName(itemStack);
        }
        return itemStack.getDisplayName();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.item.animationsToGo > 0) {
            this.item.animationsToGo--;
        }
    }

    @Override
    public DisplayEntry<?> mergeWith(DisplayEntry<?> otherEntry) {
        return new ItemDisplayEntry(this.item, this.getDisplayAmount() + otherEntry.getDisplayAmount());
    }

    @Override
    protected void appendTextComponents(List<String> components, boolean reverse) {
        if (Config.inventoryCount && shouldDisplayAmount()) {
            components.add("(" + getInventoryCount() + ")");
        }

        super.appendTextComponents(components, reverse);
    }

    @Override
    protected void renderSprite(Minecraft minecraft, FontRenderer fontRenderer, int posX, int posY, float alpha,
        float partialTicks) {
        float popTime = this.item.animationsToGo - partialTicks;
        boolean animated = popTime > 0.0F;
        if (animated) {
            float popTimeScale = 1.0F + popTime / ITEM_STACK_POP_TIME;
            GL11.glPushMatrix();
            GL11.glTranslatef(posX + 8.0F, posY + 12.0F, 0.0F);
            GL11.glScalef(1.0F / popTimeScale, (popTimeScale + 1.0F) / 2.0F, 1.0F);
            GL11.glTranslatef(-(posX + 8.0F), -(posY + 12.0F), 0.0F);
        }

        try {
            DisplayEntryRenderHelper.renderItem(minecraft, this.item, posX, posY, alpha);
        } finally {
            if (animated) {
                GL11.glPopMatrix();
            }
        }

        if (Config.displayAmount.isSprite()) {
            DisplayEntryRenderHelper.renderGuiItemCount(fontRenderer, this.getDisplayAmount(), posX, posY, alpha);
        }
    }

    private int getInventoryCount() {
        if (Minecraft.getMinecraft().thePlayer == null) {
            return this.getDisplayAmount();
        }

        int count = 0;
        InventoryPlayer inventory = Minecraft.getMinecraft().thePlayer.inventory;
        for (ItemStack stack : inventory.mainInventory) {
            if (stack != null && stack.isItemEqual(this.item) && itemTagsMatch(stack, this.item)) {
                count += stack.stackSize;
            }
        }
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (Config.combineEntries == CombineEntries.NEVER || !(obj instanceof ItemDisplayEntry)) {
            return false;
        }

        ItemDisplayEntry other = (ItemDisplayEntry) obj;
        if (!this.item.isItemEqual(other.item)) {
            debugMergeMismatch("item or damage differs", this.item, other.item);
            return false;
        }
        if (!itemTagsMatch(this.item, other.item, true)) {
            return false;
        }
        if (this.item.getRarity() != other.item.getRarity()) {
            debugMergeMismatch("rarity differs", this.item, other.item);
            return false;
        }
        if (this.item.isItemEnchanted() != other.item.isItemEnchanted()) {
            debugMergeMismatch("enchantment glint differs", this.item, other.item);
            return false;
        }

        return Config.combineEntries == CombineEntries.ALWAYS || this.item.getDisplayName()
            .equals(other.item.getDisplayName());
    }

    @Override
    public int hashCode() {
        int result = this.item.getItem()
            .hashCode();
        result = 31 * result + this.item.getItemDamage();
        return result;
    }

    private static boolean itemTagsMatch(ItemStack first, ItemStack second) {
        return itemTagsMatch(first, second, false);
    }

    private static boolean itemTagsMatch(ItemStack first, ItemStack second, boolean logMismatch) {
        if (Config.combineEntries != CombineEntries.ALWAYS) {
            return ItemStack.areItemStackTagsEqual(first, second);
        }

        NBTTagCompound firstTag = normalizeAlwaysTag(first.stackTagCompound);
        NBTTagCompound secondTag = normalizeAlwaysTag(second.stackTagCompound);
        boolean matches = firstTag == null && secondTag == null || firstTag != null && firstTag.equals(secondTag);
        if (!matches && logMismatch) {
            debugMergeMismatch("normalized tags differ", first, second);
        }
        return matches;
    }

    private static NBTTagCompound normalizeAlwaysTag(NBTTagCompound tag) {
        if (tag == null) {
            return null;
        }

        NBTTagCompound copy = (NBTTagCompound) tag.copy();
        copy.removeTag("RepairCost");
        if (copy.hasKey("display", 10)) {
            NBTTagCompound display = copy.getCompoundTag("display");
            display.removeTag("Name");
            if (display.hasNoTags()) {
                copy.removeTag("display");
            }
        }
        return copy.hasNoTags() ? null : copy;
    }

    private static void debugMergeMismatch(String reason, ItemStack first, ItemStack second) {
        if (Config.combineEntries != CombineEntries.ALWAYS) {
            return;
        }

        VintagePickUpNotifier.debug(
            "Not combining item entries because " + reason
                + ": "
                + describeStack(first)
                + " vs "
                + describeStack(second));
    }

    private static String describeStack(ItemStack stack) {
        if (stack == null) {
            return "null";
        }

        return stack.stackSize + "x "
            + Item.itemRegistry.getNameForObject(stack.getItem())
            + "@"
            + stack.getItemDamage()
            + " display='"
            + stack.getDisplayName()
            + "' base='"
            + stack.getItem()
                .getItemStackDisplayName(stack)
            + "' tag="
            + stack.stackTagCompound
            + " normalizedTag="
            + normalizeAlwaysTag(stack.stackTagCompound);
    }
}
