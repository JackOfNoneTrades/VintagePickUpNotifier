package org.fentanylsolutions.vintagepickupnotifier.client.util;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.client.compat.AngelicaFontBatcher;
import org.fentanylsolutions.vintagepickupnotifier.config.EntryBackground;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class DisplayEntryRenderHelper {

    private static final NavigableMap<Integer, String> COUNT_SUFFIXES = new TreeMap<>();

    static {
        COUNT_SUFFIXES.put(1_000, "K");
        COUNT_SUFFIXES.put(1_000_000, "M");
        COUNT_SUFFIXES.put(1_000_000_000, "B");
    }

    public static void renderBackground(Minecraft minecraft, int posX, int posY, int width, float alpha) {
        if (Config.entryBackground == EntryBackground.NONE || width <= 0) {
            return;
        }

        int backgroundAlpha = MathHelper
            .clamp_int((int) ((minecraft.gameSettings.chatOpacity * 0.9F + 0.1F) * alpha * 255.0F), 0, 255);
        int color = backgroundAlpha / 2 << 24;
        Gui.drawRect(posX - 3, posY, posX + width + 5, posY + 17, color);
    }

    public static void renderItem(Minecraft minecraft, ItemStack stack, int posX, int posY, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        RenderItem.getInstance()
            .renderItemAndEffectIntoGUI(minecraft.fontRenderer, minecraft.getTextureManager(), stack, posX, posY);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    public static void renderGuiItemCount(FontRenderer fontRenderer, int count, int posX, int posY, float alpha) {
        if (count <= 1 && !Config.displaySingleCount) {
            return;
        }

        String text = shortenValue(count);
        float scale = Math.min(1.0F, 16.0F / Math.max(1, fontRenderer.getStringWidth(text)));
        int color = MathHelper.clamp_int((int) (alpha * 255.0F), 0, 255) << 24 | 0xFFFFFF;

        AngelicaFontBatcher.flush(fontRenderer);
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);
        int textX = (int) ((posX + 17) / scale) - fontRenderer.getStringWidth(text);
        int textY = (int) ((posY + fontRenderer.FONT_HEIGHT * 2) / scale) - fontRenderer.FONT_HEIGHT;
        fontRenderer.drawStringWithShadow(text, textX, textY, color);
        AngelicaFontBatcher.flush(fontRenderer);
        GL11.glPopMatrix();
    }

    private static String shortenValue(int value) {
        Map.Entry<Integer, String> entry = COUNT_SUFFIXES.floorEntry(value);
        if (entry == null) {
            return String.valueOf(value);
        }
        return value / entry.getKey() + entry.getValue();
    }
}
