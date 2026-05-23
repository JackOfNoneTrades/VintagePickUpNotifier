package org.fentanylsolutions.vintagepickupnotifier.client.compat;

import net.minecraft.client.gui.FontRenderer;

import org.fentanylsolutions.vintagepickupnotifier.VintagePickUpNotifier;

import cpw.mods.fml.common.Loader;

public final class AngelicaFontBatcher {

    private static boolean initialized;
    private static boolean available;

    private AngelicaFontBatcher() {}

    public static void flush(FontRenderer fontRenderer) {
        if (fontRenderer == null) {
            return;
        }

        init();
        if (!available) {
            return;
        }

        try {
            AngelicaFontBatchBridge.flush(fontRenderer);
        } catch (Throwable e) {
            available = false;
            VintagePickUpNotifier.debug("Disabling Angelica font batch flush: " + getReason(e));
        }
    }

    private static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        available = Loader.isModLoaded("angelica");
        if (available) {
            VintagePickUpNotifier.debug("Angelica font batch flushing enabled");
        }
    }

    private static String getReason(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) {
            return throwable.getClass()
                .getSimpleName();
        }
        return message;
    }
}
