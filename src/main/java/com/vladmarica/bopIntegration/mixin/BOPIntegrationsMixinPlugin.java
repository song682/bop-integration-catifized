package com.vladmarica.bopIntegration.mixin;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class BOPIntegrationsMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // All conditional mixins have been moved to late phase
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    /**
     * Selects the regular mixins from {@link Mixins.NormalMixins} when this config is loaded:
     * the GTNHMixins builder resolves the load-time state (physical side and {@code applyIf}
     * conditions) and returns only the mixin classes valid for this run.
     */
    @Override
    public List<String> getMixins() {
        return IMixins.getMixins(Mixins.NormalMixins.class);
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
