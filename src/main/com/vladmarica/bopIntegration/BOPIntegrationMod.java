package com.vladmarica.bopIntegration;

import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCItems;
import biomesoplenty.common.blocks.BlockBOPFoliage;
import biomesoplenty.common.world.generation.WorldGenFieldAssociation;
import com.vladmarica.bopIntegration.ic2.IC2CompatWorldGenerator;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.vladmarica.bopIntegration.Tags.MODID;

@Mod(modid = MODID, name = Tags.MODNAME, version = Tags.VERSION, dependencies = "required-after:BiomesOPlenty")
public class BOPIntegrationMod {

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
        cakeCleanup();

        if (config.waspHiveRarityModifier > 0) {
            WorldGenFieldAssociation.associateFeature("waspHivesPerChunk", new WorldGenWaspHiveFixed());
        }

        if (config.removeNetherGravestones) {
            WorldGenFieldAssociation.associateFeature("gravesPerChunk", new WorldGenNothing());
        }

        if (config.craftableRottenFlesh) {
            // 使用更兼容的方法获取腐肉物品
            Item rottenFleshItem;
            try {
                // 尝试通过字段获取
                Field rottenFleshField = Items.class.getDeclaredField("rotten_flesh");
                rottenFleshItem = (Item) rottenFleshField.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // 如果字段不存在，尝试通过注册名获取
                rottenFleshItem = (Item) Item.itemRegistry.getObject("rotten_flesh");
                if (rottenFleshItem == null) {
                    logger.error("Failed to get rotten flesh item!");
                    return;
                }
            }

            GameRegistry.addShapedRecipe(new ItemStack(rottenFleshItem, 4), "###", "#X#", "###", '#', new ItemStack(BOPCItems.misc, 1, 3), 'X', new ItemStack(BOPCBlocks.flowers, 1, 13));
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

        // 新增 IC2 兼容性
        if (Loader.isModLoaded("IC2") && config.fixIC2RubberTrees) {
            GameRegistry.registerWorldGenerator(new IC2CompatWorldGenerator(), 10);
            logger.info("IC2 rubber tree fix applied");
        }
        else if (config.fixIC2RubberTrees) {
            logger.info("IC2 not found - skipping rubber tree fix");
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
            // 使用更健壮的方法获取 CraftingManager 实例
            CraftingManager craftingManager;

            // 尝试通过反射获取实例字段
            try {
                Field instanceField = CraftingManager.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                craftingManager = (CraftingManager) instanceField.get(null);
            } catch (NoSuchFieldException e) {
                // 如果字段不存在，尝试调用 getInstance() 方法
                try {
                    Method getInstanceMethod = CraftingManager.class.getDeclaredMethod("getInstance");
                    craftingManager = (CraftingManager) getInstanceMethod.invoke(null);
                } catch (NoSuchMethodException ex) {
                    logger.error("Could not find CraftingManager instance getter");
                    return false;
                }
            }

            List<IRecipe> recipesToRemove = getIRecipes(output, craftingManager);

            return craftingManager.getRecipeList().removeAll(recipesToRemove);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static List<IRecipe> getIRecipes(ItemStack output, CraftingManager craftingManager) {
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
        return recipesToRemove;
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