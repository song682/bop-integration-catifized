package com.vladmarica.bopIntegration.tweaks.world;

import com.vladmarica.bopIntegration.BOPIntegrationMod;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class WorldGenBerryBush extends WorldGenerator {

    private final Block bushBlock;
    private final int bushMeta;

    public WorldGenBerryBush(Block block, int meta) {
        this.bushBlock = block;
        this.bushMeta = meta;
    }

    @Override
    public boolean generate(World world, Random rand, int x, int y, int z) {

        // -------------------------------------
        // 1. CONFIG: 单簇最大数量（patch size）
        // -------------------------------------
        int clusterSize = BOPIntegrationMod.config.berryClusterSize;
        if (clusterSize < 1) clusterSize = 1;

        // ----------------------------------------------------
        // 2. 找到合适的地形（落在草/泥土上，照搬 BOP 的检测逻辑）
        // ----------------------------------------------------
        for (int i = 0; i < clusterSize; i++) {

            int dx = x + rand.nextInt(4) - rand.nextInt(4);
            int dz = z + rand.nextInt(4) - rand.nextInt(4);
            int dy = y;

            // 下沉到地表
            while (dy > 1 && world.isAirBlock(dx, dy, dz)) {
                dy--;
            }

            Block ground = world.getBlock(dx, dy, dz);
            if (ground != Blocks.grass && ground != Blocks.dirt) {
                continue;
            }

            // 尝试在地表上放置
            int placeY = dy + 1;

            if (world.isAirBlock(dx, placeY, dz) &&
                    this.bushBlock.canBlockStay(world, dx, placeY, dz)) {

                world.setBlock(dx, placeY, dz, this.bushBlock, this.bushMeta, 2);
            }
        }

        return true;
    }
}