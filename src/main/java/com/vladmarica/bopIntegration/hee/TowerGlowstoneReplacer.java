package com.vladmarica.bopIntegration.hee;

import com.vladmarica.bopIntegration.BOPIntegrationMod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

public class TowerGlowstoneReplacer {

    @SubscribeEvent
    public void onPopulateChunk(PopulateChunkEvent.Post event) {
        World world = event.world;

        // 仅在末地执行
        if (world.provider.dimensionId != 1) return;

        Block crystal = GameRegistry.findBlock("BiomesOPlenty", "crystal");
        if (crystal == null) return;

        // 优化：限定扫描高度（塔大约从 40 到 160）
        int baseX = event.chunkX * 16;
        int baseZ = event.chunkZ * 16;
        int replaced = 0;

        for (int x = 0; x < 16; x++) {
            for (int y = 40; y <= 160; y++) {
                for (int z = 0; z < 16; z++) {
                    Block block = world.getBlock(baseX + x, y, baseZ + z);
                    if (block == Blocks.glowstone) {
                        world.setBlock(baseX + x, y, baseZ + z, crystal, 0, 2);
                        replaced++;
                    }
                }
            }
        }

        if (replaced > 0) {
            BOPIntegrationMod.logger.info("Replaced " + replaced + " Glowstone blocks with crystal in chunk (" + event.chunkX + ", " + event.chunkZ + ")");
        }
    }
}