package com.vladmarica.bopIntegration.mixin;

import biomesoplenty.api.biome.BOPBiome;
import biomesoplenty.common.world.features.nether.WorldGenWaspHive;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldGenWaspHive.class)
public class WorldGenWaspHiveMixin {

    @SuppressWarnings("rawtypes")
    @Inject(method = "setupGeneration", at = @At("HEAD"), cancellable = true, remap = false)
    private void applyRarityCheck(World world, Random random, BOPBiome biome, String featureName, int x, int z, CallbackInfo ci) {
        float rarity = BOPIntegrationMod.config.waspHiveRarityModifier;
        if (rarity > 0 && random.nextFloat() > rarity) {
            ci.cancel();
        }
    }
}
