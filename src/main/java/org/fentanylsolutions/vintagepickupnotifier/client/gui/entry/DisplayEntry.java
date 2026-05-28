package org.fentanylsolutions.vintagepickupnotifier.client.gui.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.client.util.DisplayEntryRenderHelper;
import org.lwjgl.opengl.GL11;

public abstract class DisplayEntry<T> {

    public static final int ELEMENT_HEIGHT = 18;
    private static final int TEXT_ITEM_MARGIN = 4;

    protected final T item;
    private final int displayAmount;
    private final EnumRarity rarity;
    private int remainingTicks = Config.displayTime;

    protected DisplayEntry(T item, int displayAmount, EnumRarity rarity) {
        this.item = item;
        this.displayAmount = displayAmount;
        this.rarity = rarity;
    }

    public Object getKey() {
        return this;
    }

    public int getDisplayAmount() {
        return this.displayAmount;
    }

    public boolean mayDiscard() {
        return Config.displayTime != 0 && this.remainingTicks <= -getTransitionTime();
    }

    public void tick() {
        if (Config.displayTime != 0 && this.remainingTicks > -getTransitionTime()) {
            this.remainingTicks--;
        }
    }

    public float getRelativeRemainingTicks(float partialTicks) {
        int transitionTime = getTransitionTime();
        if (Config.displayTime == 0 || transitionTime <= 0 || this.remainingTicks > 0) {
            return 1.0F;
        }

        return MathHelper
            .clamp_float((transitionTime + this.remainingTicks - partialTicks) / transitionTime, 0.0F, 1.0F);
    }

    public static float getExpiredRows(Collection<DisplayEntry<?>> entries, float partialTicks) {
        float expiredRows = 0.0F;
        for (DisplayEntry<?> entry : entries) {
            expiredRows += 1.0F - entry.getRelativeRemainingTicks(partialTicks);
        }
        return expiredRows;
    }

    public int getEntryWidth(FontRenderer fontRenderer) {
        int textWidth = getTextWidth(fontRenderer);
        if (!Config.drawSprite) {
            return textWidth;
        }
        return textWidth + (textWidth == 0 ? 0 : TEXT_ITEM_MARGIN) + 16;
    }

    public void render(Minecraft minecraft, FontRenderer fontRenderer, int posX, int posY, float alpha,
        float partialTicks) {
        if (alpha <= 0.02F) {
            return;
        }

        int textWidth = getTextWidth(fontRenderer);
        boolean mirrorPosition = Config.position.isRight();
        int textStartX = mirrorPosition || !Config.drawSprite ? posX : posX + 16 + TEXT_ITEM_MARGIN;

        DisplayEntryRenderHelper.renderBackground(minecraft, posX, posY, getEntryWidth(fontRenderer), alpha);
        GL11.glEnable(GL11.GL_BLEND);
        fontRenderer.drawStringWithShadow(getFormattedText(), textStartX, posY + 4, toAlphaColor(alpha));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (Config.drawSprite) {
            int spriteX = mirrorPosition ? posX + textWidth + (textWidth == 0 ? 0 : TEXT_ITEM_MARGIN) : posX;
            renderSprite(minecraft, fontRenderer, spriteX, posY, alpha, partialTicks);
        }
    }

    protected void appendTextComponents(List<String> components, boolean reverse) {
        if (Config.displayItemName) {
            components.add(this.getEntryName(this.item));
        }

        if (Config.displayAmount.isText() && shouldDisplayAmount()) {
            components.add(reverse ? this.displayAmount + "x" : "x" + this.displayAmount);
        }
    }

    protected boolean shouldDisplayAmount() {
        return this.displayAmount > 1 || this.displayAmount == 1 && Config.displaySingleCount;
    }

    protected String getFormattedText() {
        String text = createText();
        if (text.isEmpty()) {
            return text;
        }
        return getTextColor() + text;
    }

    private String createText() {
        boolean reverse = Config.position.isRight();
        List<String> components = new ArrayList<>();
        appendTextComponents(components, reverse);

        if (reverse) {
            Collections.reverse(components);
        }

        StringBuilder builder = new StringBuilder();
        for (String component : components) {
            if (component == null || component.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(component);
        }
        return builder.toString();
    }

    private int getTextWidth(FontRenderer fontRenderer) {
        return fontRenderer.getStringWidth(EnumChatFormatting.getTextWithoutFormattingCodes(getFormattedText()));
    }

    private EnumChatFormatting getTextColor() {
        if (!Config.ignoreRarity && this.rarity != EnumRarity.common) {
            return this.rarity.rarityColor;
        }
        return Config.textColor.getFormatting();
    }

    private static int toAlphaColor(float alpha) {
        int alphaBits = MathHelper.clamp_int((int) (alpha * 255.0F), 0, 255);
        return alphaBits << 24 | 0xFFFFFF;
    }

    private static int getTransitionTime() {
        if (Config.getMoveTime() <= 0 || !isTransitionEnabled()) {
            return 0;
        }
        return Config.getMoveTime();
    }

    private static boolean isTransitionEnabled() {
        return Config.fadeOut || Config.moveOut.moveHorizontally(Config.position)
            || Config.moveOut.moveVertically(Config.position);
    }

    protected abstract String getEntryName(T item);

    protected abstract void renderSprite(Minecraft minecraft, FontRenderer fontRenderer, int posX, int posY,
        float alpha, float partialTicks);

    public abstract DisplayEntry<?> mergeWith(DisplayEntry<?> otherEntry);

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();
}
