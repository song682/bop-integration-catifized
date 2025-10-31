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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.vladmarica.bopIntegration.Tags.MODID;

@Mod(modid = MODID, name = Tags.MODNAME, version = Tags.VERSION, dependencies = "required-after:BiomesOPlenty", acceptedMinecraftVersions = "1.7.10")
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
                // 使用更兼容的方法获取绿宝石和晶体
                Item emeraldItem;
                Item crystalItem = null;

                try {
                    Field emeraldField = Items.class.getDeclaredField("emerald");
                    emeraldItem = (Item) emeraldField.get(null);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    emeraldItem = (Item) Item.itemRegistry.getObject("emerald");
                    if (emeraldItem == null) {
                        emeraldItem = (Item) Item.itemRegistry.getObject("minecraft:emerald");
                    }
                }

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

        if (config.koruFrequencyMultiplier >= 0) {
            increaseKoruFrequency();
        }

        // 新增 IC2 兼容性
        if (config.fixIC2RubberTrees) {
            if(Loader.isModLoaded("IC2")){
            GameRegistry.registerWorldGenerator(new IC2CompatWorldGenerator(), 10);
            logger.info("IC2 rubber tree fix applied");
            } else {
                logger.info("IC2 not found - skipping rubber tree fix");
            }
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
            // 1.7.10 方式获取 CraftingManager
            Field instanceField = CraftingManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            CraftingManager craftingManager = (CraftingManager) instanceField.get(null);

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

    @SuppressWarnings("unchecked")
    private void increaseKoruFrequency() {
        try {
            int multiplier = config == null ? 8 : config.koruFrequencyMultiplier;
            // 允许运行时设置 0 表示 "禁用 Koru"
            if (multiplier < 0) multiplier = 0;

            int modifiedCount = 0;
            Field[] biomeFields = BOPCBiomes.class.getDeclaredFields();
            for (Field biomeField : biomeFields) {
                Object obj = biomeField.get(null); // 读取静态字段值
                if (!(obj instanceof BOPBiome)) continue;

                BOPBiome<?> biome = (BOPBiome<?>) obj;
                if (biome == null) continue;

                Object decoratorObj = biome.theBiomeDecorator;
                if (!(decoratorObj instanceof BOPOverworldBiomeDecorator)) continue;

                BOPOverworldBiomeDecorator decorator = (BOPOverworldBiomeDecorator) decoratorObj;
                OverworldBiomeFeatures features = decorator.bopFeatures;
                if (features == null) continue;

                // 如果配置为0 -> 禁用 koru（设为0）
                if (multiplier == 0) {
                    if (features.koruPerChunk != 0) {
                        features.koruPerChunk = 0;
                        modifiedCount++;
                    }
                } else {
                    // 乘法可能导致溢出或生成过多，这里做个上限保护（可根据需要调整）
                    long newVal = (long) features.koruPerChunk * (long) multiplier;
                    int capped = (int) Math.min(newVal, Integer.MAX_VALUE / 2); // 安全上限
                    if (features.koruPerChunk != capped) {
                        features.koruPerChunk = capped;
                        modifiedCount++;
                    }
                }
            }

            logger.info("Adjusted Koru frequency with multiplier={} for {} biomes", multiplier, modifiedCount);
        } catch (Exception ex) {
            logger.warn("Failed to modify Koru frequency", ex);
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