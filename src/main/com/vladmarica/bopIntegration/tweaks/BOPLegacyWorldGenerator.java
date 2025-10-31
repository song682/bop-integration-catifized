package com.vladmarica.bopIntegration.tweaks;

import biomesoplenty.api.content.BOPCBlocks;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.block.Block;

import java.util.Random;
import java.lang.reflect.Field;

public class BOPLegacyWorldGenerator implements IWorldGenerator {

    private WorldGenCrystals crystalGenerator = new WorldGenCrystals();
    private Field providerField = null;

    public BOPLegacyWorldGenerator() {
        try {
            // 使用反射获取 World 类的 provider 字段
            providerField = World.class.getDeclaredField("provider");
            providerField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            BOPIntegrationMod.logger.error("Could not find provider field in World class", e);
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        try {
            // 使用反射获取世界提供者
            Object provider = providerField.get(world);
            // 使用反射获取维度ID字段
            Field dimensionIdField = provider.getClass().getDeclaredField("dimensionId");
            dimensionIdField.setAccessible(true);
            int dimensionId = (Integer) dimensionIdField.get(provider);

            if (dimensionId == 1) {
                generateEnd(random, chunkX, chunkZ, world);
            }
        } catch (Exception e) {
            // 如果反射失败，尝试使用传统方法
            try {
                if (world.provider.dimensionId == 1) {
                    generateEnd(random, chunkX, chunkZ, world);
                }
            } catch (NoSuchFieldError ex) {
                BOPIntegrationMod.logger.error("Failed to access dimension ID", ex);
            }
        }
    }

    private void generateEnd(Random random, int chunkX, int chunkZ, World world) {
        if (BOPIntegrationMod.config.genBiomeEssence) {
            for (int i = 0; i < 30; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int y = 10 + random.nextInt(60);
                int z = chunkZ * 16 + random.nextInt(16);

                if (world.getBlock(x, y, z) == Blocks.end_stone) {
                    world.setBlock(x, y, z, BOPCBlocks.biomeBlock);
                }
            }
        }

        if (BOPIntegrationMod.config.genCelestialCrystals) {
            for (int i = 0; i < 40; i++) {
                int x = chunkX * 16 + random.nextInt(16);
                int y = 10 + random.nextInt(60);
                int z = chunkZ * 16 + random.nextInt(16);
                crystalGenerator.generate(world, random, x, y, z);
            }
        }

        if (BOPIntegrationMod.config.amethystEndGen) {
            Block gemOre = Block.getBlockFromName("biomesoplenty:gem_ore");
            if (gemOre == null) {
                BOPIntegrationMod.logger.warn("Cannot find block biomesoplenty:gem_ore; skipping End generation.");
                return;
            }

            for (int i = 0; i < 30; i++) { // 控制稀有度
                int x = chunkX * 16 + random.nextInt(16);
                int y = random.nextInt(31); // 高度范围 0-30
                int z = chunkZ * 16 + random.nextInt(16);

                if (world.getBlock(x, y, z) == Blocks.end_stone) {
                    world.setBlock(x, y, z, gemOre, 0, 2);
                }
            }
        }
    }
}