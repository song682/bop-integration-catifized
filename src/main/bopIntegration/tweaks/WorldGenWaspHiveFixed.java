package com.vladmarica.bopIntegration.tweaks;

import biomesoplenty.common.world.features.nether.WorldGenWaspHive;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenWaspHiveFixed extends WorldGenWaspHive {

    @Override
    public boolean generate(World world, Random rand, int x, int y, int z) {
        if (rand.nextInt(100) + 1 <= BOPIntegrationMod.config.waspHiveRarityModifier * 100) {
            return super.generate(world, rand, x, y, z);
        }
        return false;
    }
}