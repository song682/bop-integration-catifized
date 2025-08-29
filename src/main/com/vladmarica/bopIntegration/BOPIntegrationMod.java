package com.vladmarica.bopIntegration;

import biomesoplenty.api.biome.BOPBiome;
import biomesoplenty.api.biome.BOPBiomeDecorator;
import biomesoplenty.api.content.BOPCBiomes;
import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCItems;
import biomesoplenty.common.biome.decoration.BOPOverworldBiomeDecorator;
import biomesoplenty.common.biome.decoration.OverworldBiomeFeatures;
import biomesoplenty.common.blocks.BlockBOPFoliage;
import biomesoplenty.common.world.generation.WorldGenFieldAssociation;
import biomesoplenty.api.biome.BiomeFeatures;
import com.vladmarica.bopIntegration.tweaks.BOPLegacyWorldGenerator;
import com.vladmarica.bopIntegration.tweaks.WorldGenNothing;
import com.vladmarica.bopIntegration.thaumcraft.ThaumcraftModCompat;
import com.vladmarica.bopIntegration.tweaks.WorldGenWaspHiveFixed;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@Mod(modid = BOPIntegrationMod.MODID, version = "1.1 - Kittified", dependencies = "required-after:BiomesOPlenty")
public class BOPIntegrationMod {

    static final String MODID = "BOPIntegration";
    public static final Logger logger = LogManager.getLogger(MODID);
    public static Config config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Config(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new BOPLegacyWorldGenerator(), 0);
        decreaseKoruRarity();
        cakeCleanup();

        if (config.waspHiveRarityModifier > 0) {
            WorldGenFieldAssociation.associateFeature("waspHivesPerChunk", new WorldGenWaspHiveFixed());
        }

        if (config.removeNetherGravestones) {
            WorldGenFieldAssociation.associateFeature("gravesPerChunk", new WorldGenNothing());
        }

        if (config.craftableRottenFlesh) {
            GameRegistry.addShapedRecipe(new ItemStack(Items.rotten_flesh, 4), "###", "#X#", "###", '#', new ItemStack(BOPCItems.misc, 1, 3), 'X', new ItemStack(BOPCBlocks.flowers, 1, 13));
        }

        if (config.removeEnderporterRecipe) {
            if (removeRecipe(new ItemStack(BOPCItems.enderporter, 1))) {
                logger.info("Removed Enderporter recipe");
            }
            else {
                logger.error("Failed to remove Enderporter recipe!");
            }
        }

        if (config.harderBiomeFinderRecipe) {
            if (removeRecipe(new ItemStack(BOPCItems.biomeFinder, 1))) {
                GameRegistry.addShapedRecipe(new ItemStack(BOPCItems.biomeFinder, 1), "#X#", "XYX", "#X#", '#', new ItemStack(Items.emerald, 1), 'X', new ItemStack(BOPCBlocks.crystal, 1), 'Y', new ItemStack(BOPCItems.misc, 1, 10));
            }
            else {
                logger.error("Failed to remove Biome Finder recipe!");
            }
        }

        if (Loader.isModLoaded("Thaumcraft")) {
            ThaumcraftModCompat.apply();
        }
        else {
            logger.info("Thaumcraft not found - skipping integration patch");
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean unregisterWorldGenerator(IWorldGenerator worldGenerator) {
        try {
            Field worldGeneratorsField = GameRegistry.class.getDeclaredField("worldGenerators");
            worldGeneratorsField.setAccessible(true);
            Field worldGeneratorIndexField = GameRegistry.class.getDeclaredField("worldGeneratorIndex");
            worldGeneratorIndexField.setAccessible(true);

            Set<IWorldGenerator> generators = (Set<IWorldGenerator>) worldGeneratorsField.get(worldGenerator);
            Map<IWorldGenerator, Integer> generatorIndexMap = (Map<IWorldGenerator, Integer>) worldGeneratorIndexField.get(worldGenerator);
            if (!generators.contains(worldGenerator)) {
                return false;
            }

            generators.remove(worldGenerator);
            generatorIndexMap.remove(worldGenerator);
            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean removeRecipe(ItemStack output) {
        if (output == null) {
            return false;
        }

        try {
            CraftingManager craftingManager = CraftingManager.getInstance();
            List<IRecipe> recipesToRemove = new ArrayList<>();
            for (Object obj : craftingManager.getRecipeList()) {
                if (obj instanceof IRecipe) {
                    IRecipe recipe = (IRecipe) obj;
                    ItemStack thisOutput = recipe.getRecipeOutput();
                    if (thisOutput == null) {
                        continue;
                    }

                    if (thisOutput.getItem() == output.getItem() && thisOutput.getItemDamage() == output.getItemDamage()) {
                        recipesToRemove.add(recipe);
                    }
                }
            }

            return craftingManager.getRecipeList().removeAll(recipesToRemove);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void decreaseKoruRarity() {
        try {
            Field[] biomeFields = BOPCBiomes.class.getDeclaredFields();
            for (Field biomeField : biomeFields) {
                BOPBiome<BOPBiomeDecorator<? extends BiomeFeatures>> biome =
                        (BOPBiome<BOPBiomeDecorator<? extends BiomeFeatures>>) biomeField.get(null);
                if (biome != null && biome.theBiomeDecorator != null && biome.theBiomeDecorator instanceof BOPOverworldBiomeDecorator) {
                    BOPOverworldBiomeDecorator decorator = (BOPOverworldBiomeDecorator) biome.theBiomeDecorator;
                    OverworldBiomeFeatures features = decorator.bopFeatures;
                    if (features.koruPerChunk > 0) {
                        features.koruPerChunk *= 8;
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onHarvest(BlockEvent.HarvestDropsEvent event) {
        if (event.block.getClass() == BlockBOPFoliage.class && event.blockMetadata == 12) {
            event.drops.clear();
            event.dropChance = 1;
            event.drops.add(new ItemStack(BOPCItems.turnipSeeds, 1));
        }
    }

    @SuppressWarnings("unchecked")
    private void cakeCleanup() {
        try {
            Field handlersField = EventBus.class.getDeclaredField("listeners");
            handlersField.setAccessible(true);
            ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) handlersField.get(FMLCommonHandler.instance().bus());
            for (Object o : listeners.keySet()) {
                if (o.getClass().getSimpleName().equals("EventHandlerCake")) {
                    FMLCommonHandler.instance().bus().unregister(o);
                    logger.info("Unregistered cake crafting handler");
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
