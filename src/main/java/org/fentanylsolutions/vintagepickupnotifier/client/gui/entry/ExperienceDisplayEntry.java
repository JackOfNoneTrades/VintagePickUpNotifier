package org.fentanylsolutions.vintagepickupnotifier.client.gui.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.config.CombineEntries;
import org.lwjgl.opengl.GL11;

public final class ExperienceDisplayEntry extends DisplayEntry<String> {

    private static final ResourceLocation EXPERIENCE_ORB_TEXTURE = new ResourceLocation(
        "textures/entity/experience_orb.png");

    private int tickCount;

    public ExperienceDisplayEntry(String name, int displayAmount, int tickCount) {
        super(name, displayAmount, EnumRarity.uncommon);
        this.tickCount = tickCount;
    }

    @Override
    protected String getEntryName(String item) {
        return item;
    }

    @Override
    public void tick() {
        super.tick();
        this.tickCount++;
    }

    @Override
    public DisplayEntry<?> mergeWith(DisplayEntry<?> otherEntry) {
        return new ExperienceDisplayEntry(
            this.item,
            this.getDisplayAmount() + otherEntry.getDisplayAmount(),
            this.tickCount);
    }

    @Override
    protected void renderSprite(Minecraft minecraft, FontRenderer fontRenderer, int posX, int posY, float alpha) {
        int textureOffset = getXpTexture(this.getDisplayAmount());
        int textureX = textureOffset % 4 * 16;
        int textureY = textureOffset / 4 * 16;
        float age = this.tickCount / 2.0F;
        float red = (MathHelper.sin(age) + 1.0F) * 0.5F;
        float green = 1.0F;
        float blue = (MathHelper.sin(age + 4.1887903F) + 1.0F) * 0.1F;

        minecraft.getTextureManager()
            .bindTexture(EXPERIENCE_ORB_TEXTURE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(red, green, blue, alpha);
        Gui.func_146110_a(posX, posY, textureX, textureY, 16, 16, 64, 64);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return Config.combineEntries != CombineEntries.NEVER && obj instanceof ExperienceDisplayEntry;
    }

    @Override
    public int hashCode() {
        return 31;
    }

    private static int getXpTexture(int displayCount) {
        if (displayCount >= 2477) {
            return 10;
        } else if (displayCount >= 1237) {
            return 9;
        } else if (displayCount >= 617) {
            return 8;
        } else if (displayCount >= 307) {
            return 7;
        } else if (displayCount >= 149) {
            return 6;
        } else if (displayCount >= 73) {
            return 5;
        } else if (displayCount >= 37) {
            return 4;
        } else if (displayCount >= 17) {
            return 3;
        } else if (displayCount >= 7) {
            return 2;
        } else if (displayCount >= 3) {
            return 1;
        } else {
            return 0;
        }
    }
}
