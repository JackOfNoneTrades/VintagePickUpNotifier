package org.fentanylsolutions.vintagepickupnotifier.server.compat.okbackpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.fentanylsolutions.vintagepickupnotifier.VintagePickUpNotifier;

import cpw.mods.fml.common.Loader;

public final class OkBackpackCompat {

    private static final String MODID = "okbackpack";

    private static boolean initialized;
    private static boolean available;

    private OkBackpackCompat() {}

    public static boolean isLoaded() {
        init();
        return available;
    }

    public static int getPickupAmount(EntityPlayer player, ItemStack stack) {
        init();
        if (!available || player == null || stack == null || stack.stackSize <= 0) {
            return 0;
        }

        try {
            return OkBackpackCompatBridge.getPickupAmount(player, stack);
        } catch (Throwable e) {
            available = false;
            VintagePickUpNotifier.debug("Disabling OKBackpack pickup compat: " + getReason(e));
            return 0;
        }
    }

    private static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        available = Loader.isModLoaded(MODID);
        if (available) {
            VintagePickUpNotifier.debug("OKBackpack pickup compat enabled");
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
