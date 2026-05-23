package org.fentanylsolutions.vintagepickupnotifier.client.gui.entry;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.client.util.DisplayEntryRenderHelper;
import org.fentanylsolutions.vintagepickupnotifier.config.CombineEntries;
import org.lwjgl.opengl.GL11;

public final class ItemDisplayEntry extends DisplayEntry<ItemStack> {

    private static final int ITEM_STACK_POP_TIME = 5;

    public ItemDisplayEntry(ItemStack itemStack, int displayAmount) {
        super(itemStack.copy(), displayAmount, itemStack.getRarity());
        this.item.animationsToGo = ITEM_STACK_POP_TIME;
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
        if (Config.inventoryCount) {
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
            if (stack != null && stack.isItemEqual(this.item) && ItemStack.areItemStackTagsEqual(stack, this.item)) {
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
        if (!this.item.isItemEqual(other.item) || !ItemStack.areItemStackTagsEqual(this.item, other.item)
            || this.item.getRarity() != other.item.getRarity()
            || this.item.isItemEnchanted() != other.item.isItemEnchanted()) {
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
}
