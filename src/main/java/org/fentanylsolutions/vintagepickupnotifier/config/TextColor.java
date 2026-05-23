package org.fentanylsolutions.vintagepickupnotifier.config;

import net.minecraft.util.EnumChatFormatting;

public enum TextColor {

    BLACK(EnumChatFormatting.BLACK),
    DARK_BLUE(EnumChatFormatting.DARK_BLUE),
    DARK_GREEN(EnumChatFormatting.DARK_GREEN),
    DARK_AQUA(EnumChatFormatting.DARK_AQUA),
    DARK_RED(EnumChatFormatting.DARK_RED),
    DARK_PURPLE(EnumChatFormatting.DARK_PURPLE),
    GOLD(EnumChatFormatting.GOLD),
    GRAY(EnumChatFormatting.GRAY),
    DARK_GRAY(EnumChatFormatting.DARK_GRAY),
    BLUE(EnumChatFormatting.BLUE),
    GREEN(EnumChatFormatting.GREEN),
    AQUA(EnumChatFormatting.AQUA),
    RED(EnumChatFormatting.RED),
    LIGHT_PURPLE(EnumChatFormatting.LIGHT_PURPLE),
    YELLOW(EnumChatFormatting.YELLOW),
    WHITE(EnumChatFormatting.WHITE);

    private final EnumChatFormatting formatting;

    TextColor(EnumChatFormatting formatting) {
        this.formatting = formatting;
    }

    public EnumChatFormatting getFormatting() {
        return this.formatting;
    }
}
