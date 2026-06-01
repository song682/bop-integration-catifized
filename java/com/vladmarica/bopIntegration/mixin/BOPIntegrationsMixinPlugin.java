package com.vladmarica.bopIntegration.mixin;

import cpw.mods.fml.common.Loader;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class BOPIntegrationsMixinPlugin implements IMixinConfigPlugin {

    private static final String IC2_MIXIN_CLASS = "com.vladmarica.bopIntegration.mixin.ic2.WorldGenRubTreeMixin";
    private static final String BOP_FLOWER2_MIXIN_CLASS = "com.vladmarica.bopIntegration.mixin.BlockBOPFlower2Mixin";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Only apply IC2 mixin if IC2 is loaded
        if (IC2_MIXIN_CLASS.equals(mixinClassName)) {
            return Loader.isModLoaded("IC2");
        }
        // Only apply BlockBOPFlower2 mixin if Et Futurum is loaded
        if (BOP_FLOWER2_MIXIN_CLASS.equals(mixinClassName)) {
            return Loader.isModLoaded("etfuturum");
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
