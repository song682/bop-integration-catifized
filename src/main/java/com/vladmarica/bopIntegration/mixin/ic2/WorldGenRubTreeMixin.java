package com.vladmarica.bopIntegration.mixin.ic2;

import biomesoplenty.api.content.BOPCBiomes;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import ic2.core.block.WorldGenRubTree;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * Controls rubber tree generation in BOP biomes via mixin instead of a separate IWorldGenerator.
 * In BOP grassland/marsh biomes, rubber trees are severely reduced (10% chance).
 */
@Mixin(WorldGenRubTree.class)
public class WorldGenRubTreeMixin {

    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void checkBiomeForGeneration(World world, Random random, int x, int y, int z, CallbackInfoReturnable<Boolean> ci) {
        if (!BOPIntegrationMod.config.fixIC2RubberTrees) {
            return;
        }

        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

        // In BOP grassland/marsh, rubber trees are out of place
        // Severely reduce their generation (10% chance)
        if (biome == BOPCBiomes.grassland || biome == BOPCBiomes.marsh || biome == BOPCBiomes.landOfLakesMarsh) {
            if (random.nextInt(10) != 0) {
                ci.setReturnValue(false);
            }
        }
    }
}
