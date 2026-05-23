package org.fentanylsolutions.vintagepickupnotifier.client.compat;

import java.lang.reflect.Method;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.angelica.client.font.BatchingFontRenderer;
import com.gtnewhorizons.angelica.mixins.interfaces.FontRendererAccessor;

final class AngelicaFontBatchBridge {

    private static final Method FLUSH_BATCH_METHOD = getFlushBatchMethod();

    private AngelicaFontBatchBridge() {}

    static void flush(FontRenderer fontRenderer) throws Exception {
        if (!(fontRenderer instanceof FontRendererAccessor)) {
            return;
        }

        BatchingFontRenderer batcher = ((FontRendererAccessor) fontRenderer).angelica$getBatcher();
        if (batcher != null) {
            FLUSH_BATCH_METHOD.invoke(batcher);
        }
    }

    private static Method getFlushBatchMethod() {
        try {
            Method method = BatchingFontRenderer.class.getDeclaredMethod("flushBatch");
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            throw new IllegalStateException("Angelica font batch flush is unavailable", e);
        }
    }
}
