package com.vladmarica.bopIntegration.proxy;

import biomesoplenty.api.biome.BOPBiome;
import biomesoplenty.api.biome.BiomeFeatures;
import biomesoplenty.api.content.BOPCBlocks;
import biomesoplenty.api.content.BOPCItems;
import biomesoplenty.common.biome.decoration.OverworldBiomeFeatures;
import biomesoplenty.common.world.generation.WorldGenFieldAssociation;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import com.vladmarica.bopIntegration.Config;
import com.vladmarica.bopIntegration.thaumcraft.ThaumcraftModCompat;
import com.vladmarica.bopIntegration.content.block.BlockHoneyCauldron;
import com.vladmarica.bopIntegration.content.event.EventHoneyMechanics;
import com.vladmarica.bopIntegration.content.world.gen.WorldGenNothing;
import com.vladmarica.bopIntegration.mixin.middle.accessor.CraftingManagerAccessor;
import com.vladmarica.bopIntegration.mixin.middle.accessor.EventBusAccessor;
import com.vladmarica.bopIntegration.mixin.middle.accessor.GameRegistryAccessor;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.vladmarica.bopIntegration.Tags.MODID;

public class CommonProxy {

    public static final Logger logger = LogManager.getLogger(MODID);
    public static BlockHoneyCauldron honeyCauldronBlock;
    public static EventHoneyMechanics honeyHandler;

    public void preInit(FMLPreInitializationEvent event) {
        MixinBootstrap.init();
        if (BOPIntegrationMod.config == null) {
            BOPIntegrationMod.config = new Config(event.getSuggestedConfigurationFile());
        }

        // 注册自身到事件总线
        MinecraftForge.EVENT_BUS.register(this);

        // -------- 蜂蜜炼药锅注册 --------
        if (BOPIntegrationMod.config.honeyEnabled && BOPIntegrationMod.config.honeyCauldronEnabled) {
            honeyCauldronBlock = new BlockHoneyCauldron();
            GameRegistry.registerBlock(honeyCauldronBlock, "honey_cauldron");
            logger.info("Honey cauldron block registered");
        }
    }

    public void init(FMLInitializationEvent event){
        // -------- 蜂蜜机制初始化 --------
        if (BOPIntegrationMod.config.honeyEnabled) {
            honeyHandler = new EventHoneyMechanics(BOPIntegrationMod.config.hotBlocksList);
            // TickEvent 走 FML 总线，PlayerInteractEvent 走 Forge 总线
            MinecraftForge.EVENT_BUS.register(honeyHandler);
            FMLCommonHandler.instance().bus().register(honeyHandler);
            logger.info("Honey mechanics enabled (melt={}, crystallize={}, cauldron={})",
                BOPIntegrationMod.config.honeyMeltEnabled, BOPIntegrationMod.config.honeyCrystallizeEnabled, BOPIntegrationMod.config.honeyCauldronEnabled);
        }

        if (BOPIntegrationMod.config.disableBopOriginalBerryBush) {
            WorldGenFieldAssociation.associateFeature("berryBushesPerChunk", new WorldGenNothing());
        }

        // -------- Koru rarity multiplier --------
        applyKoruFrequencyMultiplier();

        if (BOPIntegrationMod.config.craftableRottenFlesh) {
            Item rottenFleshItem = Items.rotten_flesh;
            if (rottenFleshItem == null) {
                logger.error("Failed to get rotten flesh item!");
                return;
            }

            GameRegistry.addShapedRecipe(new ItemStack(rottenFleshItem, 4), "###", "#X#", "###", '#', new ItemStack(BOPCItems.misc, 1, 3), 'X', new ItemStack(BOPCBlocks.flowers, 1, 13));
        }

        if (BOPIntegrationMod.config.removeEnderporterRecipe) {
            ItemStack enderporter = new ItemStack(BOPCItems.enderporter, 1);
            if (removeRecipe(enderporter)) {
                logger.info("Removed Enderporter recipe");
            }
            else {
                logger.error("Failed to remove Enderporter recipe!");
            }
        }

        if (BOPIntegrationMod.config.harderBiomeFinderRecipe) {
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
        if (BOPIntegrationMod.config.fixIC2RubberTrees) {
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

        // HEE Dungeon Tower glowstone replacement (handled via Mixin into ComponentTower.setupStructure)
        if (BOPIntegrationMod.config.replaceGlowStoneInTower) {
            if (Loader.isModLoaded("HardcoreEnderExpansion")) {
                logger.info("HEE glowstone replacement in the Dungeon Tower applied via Mixin");
            } else {
                logger.info("HEE not found - skipping Dungeon Tower glowstone replacement");
            }
        } else {
            if (Loader.isModLoaded("HardcoreEnderExpansion")) {
                logger.info("HEE is installed, but replaceGlowStoneInTower is disabled.");
            }
        }
    }

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

    /**
     * Applies the configured Koru frequency multiplier to every registered BOP overworld biome.
     * <p>
     * BOP keeps the per-chunk Koru count in {@code OverworldBiomeFeatures.koruPerChunk} and reads
     * it reflectively on each chunk decoration ({@code WorldGenBOPFlora#setupGeneration} calls
     * {@code BiomeFeatures#getFeature}), so scaling the field once after all biomes have been
     * registered is sufficient. A multiplier of 0 disables Koru generation entirely, while the
     * default value of 1 leaves the BOP values untouched.
     */
    @SuppressWarnings("rawtypes")
    private static void applyKoruFrequencyMultiplier() {
        int multiplier = BOPIntegrationMod.config.koruFrequencyMultiplier;
        if (multiplier == 1 || multiplier < 0) {
            return;
        }

        int modified = 0;
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (!(biome instanceof BOPBiome)) {
                continue;
            }

            // Reference BOPBiome's own decorator field by its shipped (SRG) name: the source name
            // "theBiomeDecorator" collides with the vanilla BiomeGenBase field during BOP's
            // reobfuscation, so writing "theBiomeDecorator" here would resolve to the inherited vanilla
            // BiomeDecorator instead of BOP's BOPBiomeDecorator in the released BOP jar.
            BiomeFeatures features = ((BOPBiome) biome).field_76760_I.bopFeatures;
            if (!(features instanceof OverworldBiomeFeatures)) {
                continue;
            }

            OverworldBiomeFeatures overworldFeatures = (OverworldBiomeFeatures) features;
            long scaled = multiplier == 0 ? 0L : (long) overworldFeatures.koruPerChunk * multiplier;
            overworldFeatures.koruPerChunk = (int) Math.min(scaled, Integer.MAX_VALUE);
            modified++;
        }

        logger.info("Applied Koru frequency multiplier x{} to {} BOP overworld biomes", multiplier, modified);
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
