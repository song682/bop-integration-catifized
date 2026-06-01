package com.vladmarica.bopIntegration.mixin;

import biomesoplenty.common.biome.decoration.OverworldBiomeFeatures;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the koru frequency multiplier lazily on the first feature lookup.
 * This avoids config timing issues (BOP biomes are created before our config is loaded).
 */
@Mixin(OverworldBiomeFeatures.class)
public class OverworldBiomeFeaturesMixin {

    @Unique
    private boolean koruMultiplierApplied = false;

    @Inject(method = "getFeature", at = @At("HEAD"), remap = false)
    private void applyKoruMultiplierLazy(String featureName, CallbackInfoReturnable<Object> ci) {
        if (!koruMultiplierApplied && "koruPerChunk".equals(featureName)) {
            koruMultiplierApplied = true;

            int multiplier = BOPIntegrationMod.config.koruFrequencyMultiplier;
            if (multiplier >= 0) {
                OverworldBiomeFeatures self = (OverworldBiomeFeatures) (Object) this;

                if (multiplier == 0) {
                    self.koruPerChunk = 0;
                } else {
                    long newVal = (long) self.koruPerChunk * (long) multiplier;
                    self.koruPerChunk = (int) Math.min(newVal, Integer.MAX_VALUE / 2);
                }
            }
        }
    }
}
