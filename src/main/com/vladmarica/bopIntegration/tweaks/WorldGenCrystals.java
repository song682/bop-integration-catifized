package com.vladmarica.bopIntegration.tweaks;

import java.util.Random;

import biomesoplenty.api.content.BOPCBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenCrystals extends WorldGenerator
{
    @Override
    public boolean generate(World world, Random random, int x, int y, int z)
    {
        if (!world.isAirBlock(x, y, z))
        {
            return false;
        }
        else if (world.getBlock(x, y + 1, z) != Blocks.end_stone)
        {
            return false;
        }
        else
        {
            world.setBlock(x, y, z, BOPCBlocks.crystal, 0, 2);

            for (int l = 0; l < 900; ++l)
            {
                int i1 = x + random.nextInt(8) - random.nextInt(8);
                int j1 = y - random.nextInt(12);
                int k1 = z + random.nextInt(8) - random.nextInt(8);

                if (world.getBlock(i1, j1, k1).getMaterial() == Material.air)
                {
                    int l1 = 0;

                    for (int i2 = 0; i2 < 6; ++i2)
                    {
                        Block block = null;

                        if (i2 == 0)
                        {
                            block = world.getBlock(i1 - 1, j1, k1);
                        }

                        if (i2 == 1)
                        {
                            block = world.getBlock(i1 + 1, j1, k1);
                        }

                        if (i2 == 2)
                        {
                            block = world.getBlock(i1, j1 - 1, k1);
                        }

                        if (i2 == 3)
                        {
                            block = world.getBlock(i1, j1 + 1, k1);
                        }

                        if (i2 == 4)
                        {
                            block = world.getBlock(i1, j1, k1 - 1);
                        }

                        if (i2 == 5)
                        {
                            block = world.getBlock(i1, j1, k1 + 1);
                        }

                        if (block == BOPCBlocks.crystal)
                        {
                            ++l1;
                        }
                    }

                    if (l1 == 1)
                    {
                        world.setBlock(i1, j1, k1, BOPCBlocks.crystal, 0, 2);
                    }
                }
            }

            return true;
        }
    }
}