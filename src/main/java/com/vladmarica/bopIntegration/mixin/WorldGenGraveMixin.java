package com.vladmarica.bopIntegration.mixin;

import biomesoplenty.api.biome.BOPBiome;
import biomesoplenty.common.world.features.nether.WorldGenGrave;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenGrave.class)
public class WorldGenGraveMixin {

    @SuppressWarnings("rawtypes")
    @Inject(method = "setupGeneration", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelIfDisabled(World world, Random random, BOPBiome biome, String featureName, int x, int z, CallbackInfo ci) {
        if (BOPIntegrationMod.config.removeNetherGravestones) {
            ci.cancel();
        }
    }
}
