package com.vladmarica.bopIntegration.mixin;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;

import java.util.List;
import java.util.Set;

/**
 * Queues the late mixin config once Forge has constructed all mods, so that its mixin list
 * can be filtered against the set of actually loaded mods (e.g. IC2).
 */
@LateMixin
public class BOPIntegrationsLateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.bopintegration.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return IMixins.getLateMixins(Mixins.LateMixins.class, loadedMods);
    }
}
