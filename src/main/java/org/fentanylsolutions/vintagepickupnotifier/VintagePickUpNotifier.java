package org.fentanylsolutions.vintagepickupnotifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = VintagePickUpNotifier.MODID,
    version = Tags.VERSION,
    name = "Vintage Pick Up Notifier",
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*",
    customProperties = { @Mod.CustomProperty(k = "license", v = "LGPLv3"),
        @Mod.CustomProperty(
            k = "issueTrackerUrl",
            v = "https://github.com/JackOfNoneTrades/VintagePickUpNotifier/issues"),
        @Mod.CustomProperty(k = "iconFile", v = "assets/vintagepickupnotifier/logo.png"), },
    guiFactory = "org.fentanylsolutions.vintagepickupnotifier.gui.GuiFactory")
public class VintagePickUpNotifier {

    public static final String MODID = "vintagepickupnotifier";
    public static final Logger LOG = LogManager.getLogger(MODID);
    private static boolean DEBUG_MODE;

    @SidedProxy(
        clientSide = "org.fentanylsolutions.vintagepickupnotifier.ClientProxy",
        serverSide = "org.fentanylsolutions.vintagepickupnotifier.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        String debugVar = System.getenv("MCMODDING_DEBUG_MODE");
        DEBUG_MODE = debugVar != null;
        VintagePickUpNotifier.LOG.info("MCMODDING_DEBUG_MODE env var: {}", DEBUG_MODE);
        VintagePickUpNotifier.LOG.info("Using config file {}", event.getSuggestedConfigurationFile());
        proxy.preInit(event);
        VintagePickUpNotifier.LOG.info("debugMode config option: {}", Config.debugMode);
        VintagePickUpNotifier.LOG.info("isDebugMode: {}", isDebugMode());
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    public static boolean isDebugMode() {
        return DEBUG_MODE || Config.debugMode;
    }

    public static void debug(String message) {
        if (isDebugMode()) {
            LOG.info("DEBUG: {}", message);
        }
    }
}
