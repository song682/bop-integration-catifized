package com.vladmarica.bopIntegration.ic2;

import biomesoplenty.api.content.BOPCBiomes;
import cpw.mods.fml.common.IWorldGenerator;
import ic2.core.block.WorldGenRubTree;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Random;

public class IC2CompatWorldGenerator implements IWorldGenerator {

    private WorldGenRubTree rubberTreeGenerator = new WorldGenRubTree();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        BiomeGenBase biome = world.getBiomeGenForCoords(chunkX * 16 + 8, chunkZ * 16 + 8);

        // 在沼泽生物群系中减少橡胶树生成
        if (biome == BOPCBiomes.grassland || biome == BOPCBiomes.marsh || biome == BOPCBiomes.landOfLakesMarsh) {
            // 在沼泽生物群系中只有 10% 的概率生成橡胶树
            if (random.nextInt(10) != 0) {
                return;
            }
        }

        int numTrees = 0;
        if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.CONIFEROUS)) {
            numTrees += random.nextInt(3);
        }
        if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.FOREST) || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.JUNGLE)) {
            numTrees += random.nextInt(5) + 2;
        }
        if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SWAMP)) {
            numTrees += random.nextInt(10) + 3;
        }

        if (random.nextInt(100) + 1 <= numTrees * 2) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);

            // 使用 WorldGenRubTree 的 getGrowHeight 方法来检查是否可以生成橡胶树
            int y = world.getHeightValue(x, z);
            int growHeight = rubberTreeGenerator.getGrowHeight(world, x, y, z);

            if (growHeight > 0) {
                // 使用 WorldGenRubTree 的 grow 方法来生成橡胶树
                rubberTreeGenerator.grow(world, x, y, z, random);
            }
        }
    }
}