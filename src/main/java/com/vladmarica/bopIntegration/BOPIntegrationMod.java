package com.vladmarica.bopIntegration;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCItems;
import biomesoplenty.common.world.generation.WorldGenFieldAssociation;
import com.vladmarica.bopIntegration.hee.TowerGlowstoneReplacer;
import com.vladmarica.bopIntegration.thaumcraft.ThaumcraftModCompat;
import com.vladmarica.bopIntegration.tweaks.BlockBOPBerryBush;
import com.vladmarica.bopIntegration.tweaks.event.EventBerryPlanting;
import com.vladmarica.bopIntegration.tweaks.world.WorldGenBerryBush;
import com.vladmarica.bopIntegration.tweaks.world.WorldGenNothing;
import com.vladmarica.bopIntegration.mixin.accessor.CraftingManagerAccessor;
import com.vladmarica.bopIntegration.mixin.accessor.EventBusAccessor;
import com.vladmarica.bopIntegration.mixin.accessor.GameRegistryAccessor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.vladmarica.bopIntegration.Tags.MODID;

@Mod(modid = MODID, name = Tags.MODNAME, version = Tags.VERSION, dependencies = "required-after:BiomesOPlenty", acceptedMinecraftVersions = "[1.7.10]")
public class BOPIntegrationMod {

    public static final Logger logger = LogManager.getLogger(MODID);
    public static Config config;
    public static Block bopBerryBush;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Config(event.getSuggestedConfigurationFile());

        if(config.growableBopBerry) {
            EventBerryPlanting.register();
            bopBerryBush = new BlockBOPBerryBush();
            GameRegistry.registerBlock(bopBerryBush, "berry_bush");
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event){
        if (config.disableBopOriginalBerryBush) {
            WorldGenFieldAssociation.associateFeature("berryBushesPerChunk", new WorldGenNothing());
        }

        if(config.growableBopBerry){
            WorldGenFieldAssociation.associateFeature("berryBushesPerChunk", new WorldGenBerryBush(BOPIntegrationMod.bopBerryBush, 0));
        }

        if (config.craftableRottenFlesh) {
            Item rottenFleshItem = Items.rotten_flesh;
            if (rottenFleshItem == null) {
                logger.error("Failed to get rotten flesh item!");
                return;
            }

            GameRegistry.addShapedRecipe(new ItemStack(rottenFleshItem, 4), "###", "#X#", "###", '#', new ItemStack(BOPCItems.misc, 1, 3), 'X', new ItemStack(BOPCBlocks.flowers, 1, 13));
        }

        if (config.removeEnderporterRecipe) {
            ItemStack enderporter = new ItemStack(BOPCItems.enderporter, 1);
            if (removeRecipe(enderporter)) {
                logger.info("Removed Enderporter recipe");
            }
            else {
                logger.error("Failed to remove Enderporter recipe!");
            }
        }

        if (config.harderBiomeFinderRecipe) {
            ItemStack biomeFinder = new ItemStack(BOPCItems.biomeFinder, 1);
            if (removeRecipe(biomeFinder)) {
                Item emeraldItem = Items.emerald;
                Item crystalItem = null;

                try {
                    crystalItem = (Item) Item.itemRegistry.getObject("BiomesOPlenty:crystal");
                } catch (Exception e) {
                    logger.error("Failed to get crystal item");
                }

                if (emeraldItem != null && crystalItem != null) {
                    GameRegistry.addShapedRecipe(new ItemStack(BOPCItems.biomeFinder, 1), "#X#", "XYX", "#X#", '#', new ItemStack(emeraldItem, 1), 'X', new ItemStack(crystalItem, 1), 'Y', new ItemStack(BOPCItems.misc, 1, 10));
                } else {
                    logger.error("Failed to add harder Biome Finder recipe - missing items");
                }
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

        // IC2 rubber tree fix (handled via Mixin into WorldGenRubTree)
        if (config.fixIC2RubberTrees) {
            if (Loader.isModLoaded("IC2")) {
                logger.info ("IC2 rubber tree fix applied via Mixin");
            } else {
                logger.info ("IC2 not found - skipping rubber tree fix");
            }
        } else {
            if (Loader.isModLoaded("IC2")) {
                logger.info ("IC2 is installed, but fixIC2RubberTrees is disabled.");
            }
        }

        // 新增 HardcoreEnderExpansion 兼容性
        if(config.replaceGlowStoneInTower) {
            if (Loader.isModLoaded("HardcoreEnderExpansion")) {
                // 注册自定义的世界生成器
                MinecraftForge.TERRAIN_GEN_BUS.register(new TowerGlowstoneReplacer());
                logger.info("Found HEE in mod list, the config replaceGlowStoneInTower is enabled, applying it into the game instance");
            } else {
                logger.info("HEE Not Found in mod list, though the config replaceGlowStoneInTower is enabled, skipping it.");
            }
        } else {
            if(Loader.isModLoaded("HardcoreEnderExpansion")){
                logger.info("Found HEE in mod list, while the config replaceGlowStoneInTower is enabled, skipping it.");
            } else {
                logger.info("Neither HEE is installed, nor the co nfig enabled, will do nothing.");
            }
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        cakeCleanup();
    }

    public static boolean unregisterWorldGenerator(IWorldGenerator worldGenerator) {
        try {
            Set<IWorldGenerator> generators = GameRegistryAccessor.getWorldGenerators();
            Map<IWorldGenerator, Integer> generatorIndexMap = GameRegistryAccessor.getWorldGeneratorIndex();
            if (!generators.contains(worldGenerator)) {
                return false;
            }

            generators.remove(worldGenerator);
            generatorIndexMap.remove(worldGenerator);
            return true;
        }
        catch (Exception ex) {
            logger.error("Failed to unregister world generator:" + worldGenerator + ". Returned:" + ex);
            return false;
        }
    }

    public static boolean removeRecipe(ItemStack output) {
        if (output == null) {
            return false;
        }

        try {
            CraftingManager craftingManager = CraftingManagerAccessor.getInstance();

            List<IRecipe> recipes = craftingManager.getRecipeList();
            boolean removed = false;

            // 遍历并移除匹配的配方
            for (int i = 0; i < recipes.size(); i++) {
                IRecipe recipe = recipes.get(i);
                if (recipe == null) continue;

                ItemStack recipeOutput = recipe.getRecipeOutput();
                if (recipeOutput == null) continue;

                // 标准化比较（忽略堆叠数量）
                ItemStack compareOutput = output.copy();
                compareOutput.stackSize = 1;
                recipeOutput = recipeOutput.copy();
                recipeOutput.stackSize = 1;

                if (ItemStack.areItemStacksEqual(compareOutput, recipeOutput)) {
                    recipes.remove(i--);
                    removed = true;
                    logger.info("Removed recipe for: " + output.getDisplayName());
                }
            }

            return removed;
        }
        catch (Exception ex) {
            logger.error("Error removing recipe for " + output.getDisplayName() + ": ", ex);
            return false;
        }
    }

    private void cakeCleanup() {
        try {
            EventBus bus = FMLCommonHandler.instance().bus();
            ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = ((EventBusAccessor) bus).getListeners();
            for (Object o : listeners.keySet()) {
                if (o.getClass().getSimpleName().equals("EventHandlerCake")) {
                    bus.unregister(o);
                    logger.info("Unregistered cake crafting handler");
                }
            }
        }
        catch (Exception ex) {
            logger.error("Failed to unregister cake crafting handler", ex);
        }
    }
}