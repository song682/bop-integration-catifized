package com.vladmarica.bopIntegration.thaumcraft;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCItems;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public final class ThaumcraftModCompat {

    public static void apply() {
        GameRegistry.registerWorldGenerator(new ThaumcraftCompatWorldGenerator(), 0);
        if (BOPIntegrationMod.config.addMissingAspects) {
            addMissingAspects();
        }
        BOPIntegrationMod.logger.info("Thaumcraft Biomes O' Plenty integration patch has been applied");
    }

    private static void addMissingAspects() {
        // Chunk of Flesh
        ThaumcraftApi.registerObjectTag(new ItemStack(BOPCItems.misc, 1, 3), new AspectList().add(Aspect.FLESH, 1));

        // Ghastly Soul
        ThaumcraftApi.registerObjectTag(new ItemStack(BOPCItems.misc, 1, 10), new AspectList().add(Aspect.DARKNESS, 8).add(Aspect.SOUL, 4));

        // Pinecone
        ThaumcraftApi.registerObjectTag(new ItemStack(BOPCItems.misc, 1, 13), new AspectList().add(Aspect.PLANT, 1));

        // Eyebulb
        ThaumcraftApi.registerObjectTag(new ItemStack(BOPCBlocks.flowers, 1, 13), new AspectList().add(Aspect.PLANT, 1).add(Aspect.FLESH, 1));

        // All other flowers
        for (int i = 0; i < 16; i++) {
            if (i == 13) continue;
            ThaumcraftApi.registerObjectTag(new ItemStack(BOPCBlocks.flowers, 1, i), new AspectList().add(Aspect.PLANT, 1));
        }

        // Burning Blossom
        ThaumcraftApi.registerObjectTag(new ItemStack(BOPCBlocks.flowers2, 1, 2), new AspectList().add(Aspect.PLANT, 1).add(Aspect.FIRE, 1));

        // All other flowers 2
        for (int i = 0; i < 9; i++) {
            if (i == 2) continue;
            ThaumcraftApi.registerObjectTag(new ItemStack(BOPCBlocks.flowers2, 1, i), new AspectList().add(Aspect.PLANT, 1));
        }
    }
}
