package org.fentanylsolutions.vintagepickupnotifier.client.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.client.compat.AngelicaFontBatcher;
import org.fentanylsolutions.vintagepickupnotifier.client.gui.entry.DisplayEntry;
import org.fentanylsolutions.vintagepickupnotifier.client.gui.entry.ExperienceDisplayEntry;
import org.fentanylsolutions.vintagepickupnotifier.client.gui.entry.ItemDisplayEntry;
import org.fentanylsolutions.vintagepickupnotifier.config.AnchorPoint;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class DrawEntriesHandler {

    public static final DrawEntriesHandler INSTANCE = new DrawEntriesHandler();
    private static final int HANDLED_ENTITY_TICKS = 40;

    private final Map<Object, DisplayEntry<?>> collector = new LinkedHashMap<>();
    private final Map<Integer, Integer> handledEntities = new HashMap<>();

    private DrawEntriesHandler() {}

    public void addEntry(DisplayEntry<?> displayEntry) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer != null && minecraft.thePlayer.capabilities.isCreativeMode
            && Config.disableInCreative) {
            return;
        }

        int maxSize = getMaxEntryCount(minecraft);
        DisplayEntry<?> oldEntry = this.collector.remove(displayEntry.getKey());
        if (oldEntry != null) {
            displayEntry = displayEntry.mergeWith(oldEntry);
        }

        while (!this.collector.isEmpty() && this.collector.size() >= maxSize) {
            Object firstKey = this.collector.keySet()
                .iterator()
                .next();
            this.collector.remove(firstKey);
        }

        this.collector.put(displayEntry.getKey(), displayEntry);
    }

    public boolean isEntityHandled(int entityId) {
        return this.handledEntities.containsKey(entityId);
    }

    public void addHandledEntity(int entityId) {
        this.handledEntities.put(entityId, HANDLED_ENTITY_TICKS);
    }

    public void addDebugEntries() {
        Minecraft minecraft = Minecraft.getMinecraft();
        ItemStack stack = minecraft.thePlayer != null ? minecraft.thePlayer.getCurrentEquippedItem() : null;
        if (stack == null) {
            stack = new ItemStack(Items.diamond, 7);
        }
        addEntry(new ItemDisplayEntry(stack, Math.max(1, stack.stackSize)));
        addEntry(new ExperienceDisplayEntry("Experience", 42, 0));
    }

    public void clear() {
        this.collector.clear();
        this.handledEntities.clear();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft()
            .isGamePaused()) {
            return;
        }

        if (!this.collector.isEmpty()) {
            for (DisplayEntry<?> entry : this.collector.values()) {
                entry.tick();
            }

            if (Config.displayTime != 0) {
                this.collector.values()
                    .removeIf(DisplayEntry::mayDiscard);
            }
        }

        if (!this.handledEntities.isEmpty()) {
            Iterator<Entry<Integer, Integer>> iterator = this.handledEntities.entrySet()
                .iterator();
            while (iterator.hasNext()) {
                Entry<Integer, Integer> entry = iterator.next();
                int remainingTicks = entry.getValue() - 1;
                if (remainingTicks <= 0) {
                    iterator.remove();
                } else {
                    entry.setValue(remainingTicks);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (this.collector.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer fontRenderer = minecraft.fontRenderer;
        float partialTicks = event.partialTicks;
        float scale = Config.getDisplayScale();
        int screenWidth = (int) (event.resolution.getScaledWidth() / scale);
        int screenHeight = (int) (event.resolution.getScaledHeight() / scale);

        List<DisplayEntry<?>> entries = new ArrayList<>(this.collector.values());
        AnchorPoint anchorPoint = Config.position;
        if (!anchorPoint.isTop()) {
            Collections.reverse(entries);
        }

        int maxWidth = 0;
        for (DisplayEntry<?> entry : entries) {
            maxWidth = Math.max(maxWidth, entry.getEntryWidth(fontRenderer));
        }

        AnchorPoint.Positioner positioner = anchorPoint
            .createPositioner(screenWidth, screenHeight, maxWidth, DisplayEntry.ELEMENT_HEIGHT * entries.size());
        int offsetX = (int) (Config.offsetX / scale);
        int offsetY = (int) (Config.offsetY / scale);
        int posX = positioner.getPosX(offsetX);
        int posY = positioner.getPosY(offsetY - getMoveOffset(entries, partialTicks));
        int elementY = posY;

        AngelicaFontBatcher.flush(fontRenderer);
        org.lwjgl.opengl.GL11.glPushMatrix();
        org.lwjgl.opengl.GL11.glScalef(scale, scale, 1.0F);
        for (DisplayEntry<?> entry : entries) {
            int elementWidth = entry.getEntryWidth(fontRenderer);
            int elementX = posX + anchorPoint.createPositioner(maxWidth, -1, elementWidth, -1)
                .getPosX(0);

            if (Config.moveOut.moveHorizontally(anchorPoint)) {
                elementX += (int) (maxWidth * (1.0F - entry.getRelativeRemainingTicks(partialTicks))
                    * anchorPoint.getNormalX());
            }

            float alpha = Config.fadeOut ? entry.getRelativeRemainingTicks(partialTicks) : 1.0F;
            entry.render(minecraft, fontRenderer, elementX, elementY, alpha);
            elementY += DisplayEntry.ELEMENT_HEIGHT;
        }
        AngelicaFontBatcher.flush(fontRenderer);
        org.lwjgl.opengl.GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        clear();
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        clear();
    }

    private static int getMoveOffset(List<DisplayEntry<?>> entries, float partialTicks) {
        if (!Config.moveOut.moveVertically(Config.position)) {
            return 0;
        }
        return (int) (DisplayEntry.getExpiredRows(entries, partialTicks) * DisplayEntry.ELEMENT_HEIGHT);
    }

    private static int getMaxEntryCount(Minecraft minecraft) {
        ScaledResolution resolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int scaledHeight = (int) (resolution.getScaledHeight() / Config.getDisplayScale());
        return Math.max(1, (int) (scaledHeight * Config.maxHeight / DisplayEntry.ELEMENT_HEIGHT) - 1);
    }
}
