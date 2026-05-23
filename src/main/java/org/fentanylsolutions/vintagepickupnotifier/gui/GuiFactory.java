package org.fentanylsolutions.vintagepickupnotifier.gui;

import java.util.Arrays;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;

import org.fentanylsolutions.vintagepickupnotifier.Config;
import org.fentanylsolutions.vintagepickupnotifier.VintagePickUpNotifier;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;

@SuppressWarnings("unused")
public class GuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class ConfigGui extends GuiConfig {

        public ConfigGui(GuiScreen parentScreen) {
            super(
                parentScreen,
                Arrays.asList(
                    new ConfigElement(
                        Config.getRawConfig()
                            .getCategory(Config.Categories.GENERAL)),
                    new ConfigElement(
                        Config.getRawConfig()
                            .getCategory(Config.Categories.BEHAVIOR)),
                    new ConfigElement(
                        Config.getRawConfig()
                            .getCategory(Config.Categories.DISPLAY)),
                    new ConfigElement(
                        Config.getRawConfig()
                            .getCategory(Config.Categories.SERVER))),
                VintagePickUpNotifier.MODID,
                VintagePickUpNotifier.MODID,
                false,
                false,
                I18n.format(VintagePickUpNotifier.MODID + ".configgui.title"));
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            super.actionPerformed(button);
            if (button.id == 2000) {
                Config.save();
                Config.loadConfig(Config.getConfigFile());
            }
        }
    }
}
