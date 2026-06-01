package com.vladmarica.bopIntegration.mixin;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.common.world.ChunkProviderBOPEnd;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import com.vladmarica.bopIntegration.Config;
import com.vladmarica.bopIntegration.tweaks.world.WorldGenCrystals;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ChunkProviderBOPEnd.class)
public class ChunkProviderBOPEndMixin {

    @Shadow(remap = false) @Final private Random rand;
    @Shadow(remap = false) @Final private World worldObj;

    private WorldGenerator bopCrystalGenerator = new WorldGenCrystals();

    @Inject(method = "populate", at = @At("TAIL"))
    private void onPopulateEnd(IChunkProvider ichunkprovider, int chunkX, int chunkZ, CallbackInfo ci) {
        com.vladmarica.bopIntegration.Config cfg = BOPIntegrationMod.config;
        Random random = this.rand;

        // --- Biome Essence ---
        if (cfg.genBiomeEssence) {
            for (int i = 0; i < 30; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int y = 10 + random.nextInt(60);
                int z = chunkZ * 16 + random.nextInt(16);

                if (worldObj.getBlock(x, y, z) == Blocks.end_stone) {
                    worldObj.setBlock(x, y, z, BOPCBlocks.biomeBlock);
                }
            }
        }

        // --- Celestial Crystals ---
        if (cfg.genCelestialCrystals) {
            for (int i = 0; i < 40; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int y = 10 + random.nextInt(60);
                int z = chunkZ * 16 + random.nextInt(16);
                bopCrystalGenerator.generate(worldObj, random, x, y, z);
            }
        }

        // --- Ender Amethyst ---
        if (cfg.amethystEndGen) {
            Block gemOre = Block.getBlockFromName("BiomesOPlenty:gemOre");
            if (gemOre != null) {
                for (int i = 0; i < 30; i++) {
                    int x = chunkX * 16 + random.nextInt(16);
                    int y = random.nextInt(31);
                    int z = chunkZ * 16 + random.nextInt(16);

                    if (worldObj.getBlock(x, y, z) == Blocks.end_stone) {
                        worldObj.setBlock(x, y, z, gemOre, 0, 2);
                    }
                }
            }
        }
    }


}
