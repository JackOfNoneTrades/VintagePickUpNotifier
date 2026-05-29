package org.fentanylsolutions.vintagepickupnotifier.client.util;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.client.compat.AngelicaFontBatcher;
import org.fentanylsolutions.vintagepickupnotifier.config.EntryBackground;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class DisplayEntryRenderHelper {

    private static final int ENTRY_HEIGHT = 18;
    private static final int CHAT_BACKGROUND_TOP_OVERHANG = 0;
    private static final int CHAT_BACKGROUND_BOTTOM_OFFSET = 17;
    private static final int TOOLTIP_BACKGROUND_TOP_OVERHANG = 2;
    private static final int TOOLTIP_BACKGROUND_BOTTOM_OFFSET = 18;
    private static final int ITEM_FRAMEBUFFER_LOGICAL_SIZE = 32;
    private static final int ITEM_FRAMEBUFFER_PADDING = 8;
    private static final NavigableMap<Integer, String> COUNT_SUFFIXES = new TreeMap<>();
    private static Framebuffer itemFramebuffer;

    static {
        COUNT_SUFFIXES.put(1_000, "K");
        COUNT_SUFFIXES.put(1_000_000, "M");
        COUNT_SUFFIXES.put(1_000_000_000, "B");
    }

    public static void renderBackground(Minecraft minecraft, int posX, int posY, int width, float alpha) {
        if (Config.entryBackground == EntryBackground.NONE || width <= 0) {
            return;
        }

        if (Config.entryBackground == EntryBackground.TOOLTIP) {
            renderTooltipBackground(
                posX - 4,
                posY - getEntryTopOverhang(),
                posX + width + 5,
                posY + getEntryBottomOffset(),
                alpha);
            return;
        }

        int backgroundAlpha = MathHelper
            .clamp_int((int) ((minecraft.gameSettings.chatOpacity * 0.9F + 0.1F) * alpha * 255.0F), 0, 255);
        int color = backgroundAlpha / 2 << 24;
        Gui.drawRect(posX - 3, posY - getEntryTopOverhang(), posX + width + 5, posY + getEntryBottomOffset(), color);
    }

    public static int getEntryTopOverhang() {
        if (Config.entryBackground == EntryBackground.TOOLTIP) {
            return TOOLTIP_BACKGROUND_TOP_OVERHANG;
        }
        return CHAT_BACKGROUND_TOP_OVERHANG;
    }

    public static int getEntryBottomOffset() {
        if (Config.entryBackground == EntryBackground.NONE) {
            return ENTRY_HEIGHT;
        }
        if (Config.entryBackground == EntryBackground.TOOLTIP) {
            return TOOLTIP_BACKGROUND_BOTTOM_OFFSET;
        }
        return CHAT_BACKGROUND_BOTTOM_OFFSET;
    }

    private static void renderTooltipBackground(int left, int top, int right, int bottom, float alpha) {
        int backgroundColor = withAlpha(0xF0100010, alpha);
        int borderStart = withAlpha(0x505000FF, alpha);
        int borderEnd = withAlpha(0x5028007F, alpha);

        drawGradientRect(left + 1, top, right - 1, top + 1, backgroundColor, backgroundColor);
        drawGradientRect(left + 1, bottom - 1, right - 1, bottom, backgroundColor, backgroundColor);
        drawGradientRect(left + 1, top + 1, right - 1, bottom - 1, backgroundColor, backgroundColor);
        drawGradientRect(left, top + 1, left + 1, bottom - 1, backgroundColor, backgroundColor);
        drawGradientRect(right - 1, top + 1, right, bottom - 1, backgroundColor, backgroundColor);
        drawGradientRect(left + 1, top + 1, left + 2, bottom - 1, borderStart, borderEnd);
        drawGradientRect(right - 2, top + 1, right - 1, bottom - 1, borderStart, borderEnd);
        drawGradientRect(left + 1, top + 1, right - 1, top + 2, borderStart, borderStart);
        drawGradientRect(left + 1, bottom - 2, right - 1, bottom - 1, borderEnd, borderEnd);
    }

    private static int withAlpha(int color, float alpha) {
        int colorAlpha = color >>> 24;
        int scaledAlpha = MathHelper.clamp_int((int) (colorAlpha * alpha), 0, 255);
        return scaledAlpha << 24 | color & 0xFFFFFF;
    }

    private static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(startRed, startGreen, startBlue, startAlpha);
        tessellator.addVertex(right, top, 0.0D);
        tessellator.addVertex(left, top, 0.0D);
        tessellator.setColorRGBA_F(endRed, endGreen, endBlue, endAlpha);
        tessellator.addVertex(left, bottom, 0.0D);
        tessellator.addVertex(right, bottom, 0.0D);
        tessellator.draw();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void renderItem(Minecraft minecraft, ItemStack stack, int posX, int posY, float alpha) {
        if (alpha >= 0.99F || !OpenGlHelper.isFramebufferEnabled()) {
            renderItemDirect(minecraft, stack, posX, posY);
            return;
        }

        Framebuffer framebuffer = renderItemToFramebuffer(minecraft, stack);
        minecraft.getFramebuffer()
            .bindFramebuffer(true);
        renderItemFramebuffer(framebuffer, posX - ITEM_FRAMEBUFFER_PADDING, posY - ITEM_FRAMEBUFFER_PADDING, alpha);
    }

    private static void renderItemDirect(Minecraft minecraft, ItemStack stack, int posX, int posY) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
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

    private static Framebuffer renderItemToFramebuffer(Minecraft minecraft, ItemStack stack) {
        Framebuffer framebuffer = getItemFramebuffer(minecraft);
        framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, ITEM_FRAMEBUFFER_LOGICAL_SIZE, ITEM_FRAMEBUFFER_LOGICAL_SIZE, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);

        renderItemDirect(minecraft, stack, ITEM_FRAMEBUFFER_PADDING, ITEM_FRAMEBUFFER_PADDING);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopAttrib();

        return framebuffer;
    }

    private static void renderItemFramebuffer(Framebuffer framebuffer, int posX, int posY, float alpha) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
        framebuffer.bindFramebufferTexture();

        Tessellator tessellator = Tessellator.instance;
        double maxU = (double) framebuffer.framebufferWidth / framebuffer.framebufferTextureWidth;
        double maxV = (double) framebuffer.framebufferHeight / framebuffer.framebufferTextureHeight;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, alpha);
        tessellator.addVertexWithUV(posX, posY + ITEM_FRAMEBUFFER_LOGICAL_SIZE, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV(
            posX + ITEM_FRAMEBUFFER_LOGICAL_SIZE,
            posY + ITEM_FRAMEBUFFER_LOGICAL_SIZE,
            0.0D,
            maxU,
            0.0D);
        tessellator.addVertexWithUV(posX + ITEM_FRAMEBUFFER_LOGICAL_SIZE, posY, 0.0D, maxU, maxV);
        tessellator.addVertexWithUV(posX, posY, 0.0D, 0.0D, maxV);
        tessellator.draw();

        framebuffer.unbindFramebufferTexture();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthMask(true);
        GL11.glPopAttrib();
    }

    private static Framebuffer getItemFramebuffer(Minecraft minecraft) {
        int size = getItemFramebufferSize(minecraft);
        if (itemFramebuffer == null || itemFramebuffer.framebufferWidth != size
            || itemFramebuffer.framebufferHeight != size) {
            if (itemFramebuffer != null) {
                itemFramebuffer.deleteFramebuffer();
            }

            itemFramebuffer = new Framebuffer(size, size, true);
            itemFramebuffer.setFramebufferFilter(GL11.GL_NEAREST);
        }
        return itemFramebuffer;
    }

    private static int getItemFramebufferSize(Minecraft minecraft) {
        ScaledResolution resolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        float screenScale = Config.getDisplayScale() * resolution.getScaleFactor();
        return Math.max(
            ITEM_FRAMEBUFFER_LOGICAL_SIZE,
            MathHelper.ceiling_float_int(ITEM_FRAMEBUFFER_LOGICAL_SIZE * screenScale));
    }

    public static void renderGuiItemCount(FontRenderer fontRenderer, int count, int posX, int posY, float alpha) {
        if (!Config.shouldDisplayCount(count)) {
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
