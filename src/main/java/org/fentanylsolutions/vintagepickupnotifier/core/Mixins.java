package org.fentanylsolutions.vintagepickupnotifier.core;

import org.fentanylsolutions.fentlib.core.FentMixins;
import org.fentanylsolutions.fentlib.util.MiscUtil.Side;
import org.fentanylsolutions.fentlib.util.MixinUtil;
import org.fentanylsolutions.fentlib.util.MixinUtil.Phase;

public class Mixins extends FentMixins {

    private static final Mixins INSTANCE = new Mixins();

    @Override
    protected void registerMixins(MixinUtil.Registry registry) {
        registry.mixin("MixinNetHandlerPlayClient")
            .phase(Phase.EARLY)
            .side(Side.CLIENT)
            .build();
    }

    public static java.util.List<String> getEarlyMixinsForLoader() {
        return INSTANCE.getEarlyMixins();
    }

    public static java.util.List<String> getLateMixinsForLoader(java.util.Set<String> loadedCoreMods) {
        return INSTANCE.getLateMixins(loadedCoreMods);
    }
}
