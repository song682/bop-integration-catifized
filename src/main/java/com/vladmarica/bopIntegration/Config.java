package com.vladmarica.bopIntegration;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {

    private Configuration configurationFile;

    public boolean genCelestialCrystals;
    public boolean genBiomeEssence;
    public boolean removeNetherGravestones;
    public boolean craftableRottenFlesh;
    public boolean removeEnderporterRecipe;
    public boolean harderBiomeFinderRecipe;
    public float waspHiveRarityModifier;
    public int koruFrequencyMultiplier;
    public boolean fixSilverwoodTrees;
    public boolean addMissingAspects;
    public boolean fixIC2RubberTrees;
    public boolean amethystEndGen;
    public boolean genAmethystOreOverworld;
    public boolean replaceGlowStoneInTower;
    public boolean growableBopBerry;
    public boolean disableBopOriginalBerryBush;
    public int berryClusterSize;
    // -------- BOP fog options --------
    public boolean disableBopFog;
    public int minFogDistance;
    // -------- 蜂蜜机制配置 --------
    public boolean honeyEnabled;
    public boolean honeyMeltEnabled;
    public boolean honeyCrystallizeEnabled;
    public boolean honeyCauldronEnabled;
    public String[] hotBlocksList;
    public int honeyMeltInterval;
    public int crystallizeExposureTime;
    public int crystallizeHotTime;

    public Config(File file) {
        configurationFile = new Configuration(file);
        configurationFile.addCustomCategoryComment("Tweaks", "These options modify BOP itself.");
        configurationFile.addCustomCategoryComment("Thaumcraft", "Options to make BOP work better with Thaumcraft");
        configurationFile.addCustomCategoryComment("HardCoreEnderExpansion", "Expand HEE's Feature with BOP");
        configurationFile.addCustomCategoryComment("IC2", "Options to make BOP work better with IC2");
        configurationFile.addCustomCategoryComment("Honey", "Honey mechanics: melting, crystallization, and cauldron integration.");

        configurationFile.load();
        BopIntegrateOptions();
        saveConfigurationFile();
    }

    public void BopIntegrateOptions() {
        genCelestialCrystals = configurationFile.getBoolean("genCelestialCrystals", "Tweaks", true, "Generate Celestial Crystals in the End. Used to make Ambrosia.");
        genBiomeEssence = configurationFile.getBoolean("genBiomeEssence", "Tweaks", true, "Generate Biome Essence Ore in the End. Drops Biome Essence.");
        removeNetherGravestones = configurationFile.getBoolean("removeNetherGravestones", "Tweaks", true, "Prevent gravestones from spawning in the Nether. They are ugly and useless.");
        craftableRottenFlesh = configurationFile.getBoolean("craftableRottenFlesh", "Tweaks", false, "Adds a recipe to craft rotten flesh out of flesh chunks and an eyebulb.");
        removeEnderporterRecipe = configurationFile.getBoolean("removeEnderporterRecipe", "Tweaks", false, "It can still be cheating in by an op.");
        harderBiomeFinderRecipe = configurationFile.getBoolean("harderBiomeFinderRecipe", "Tweaks", false, "Makes the recipe use end crystals and ghastly souls.");
        waspHiveRarityModifier = configurationFile.getFloat("waspHiveRarityModifier", "Tweaks", 1.0F, 0.0F, 1.0F, "You can use this option to make nether wasp hives rarer.");
        fixSilverwoodTrees = configurationFile.getBoolean("fixSilverwoodTrees", "Thaumcraft", false, "Allows Silverwood trees to spawn in all forest and plains biomes.");
        addMissingAspects = configurationFile.getBoolean("addMissingAspects", "Thaumcraft", true, "Many BOP items don't give any aspects. ");
        fixIC2RubberTrees = configurationFile.getBoolean("fixRubberTrees", "IC2", false, "Fix rubber trees incorrecting spawning in grassland and marsh biomes.");
        koruFrequencyMultiplier = configurationFile.getInt("koruFrequencyMultiplier", "Tweaks", 1, 0, 128, "Multiplier for Koru generation frequency. Set to 0 to disable Koru entirely.");
        amethystEndGen = configurationFile.getBoolean("amethystEndGen", "Tweaks", false, "The Ender Amethyst ore is able to generate in the end now.");
        genAmethystOreOverworld = configurationFile.getBoolean("genAmethystOreOverworld", "Tweaks", true, "Set false to disable it generated in the overworld");
        replaceGlowStoneInTower = configurationFile.getBoolean("replaceGlowStoneInTower", "HardcoreEnderExpansion", false,"Replace the Glow Stone as Celestial Crystals ");
        growableBopBerry = configurationFile.getBoolean("growableBopBerry", "Tweaks", false, "Enable the berry bush planting and growing feature.");
        disableBopOriginalBerryBush = configurationFile.getBoolean("disableBopOriginalBerryBush", "Tweaks", false, "Disable the original Berry Bush of Biomes O' Plenty generate in the world");
        berryClusterSize = configurationFile.getInt("berryClusterSize", "Tweaks", 8, 0, 64, "Berry Bushes per chunk");
        // -------- BOP fog options --------
        disableBopFog = configurationFile.getBoolean("disableBopFog", "Tweaks", false, "Disable BOP's biome fog entirely: removes both the biome fog color and the shortened fog render distance in every BOP biome (Desert, Ominous Woods, Wasteland, etc.), restoring vanilla fog.");
        minFogDistance = configurationFile.getInt("minFogDistance", "Tweaks", 0, 0, 32, "Minimum distance in chunks at which BOP biome fog may begin. 0 = untouched BOP behavior. For example, 5 keeps the view free of BOP fog within 5 chunks (80 blocks) by pushing the fog range further out. Ignored when disableBopFog is true.");
        // -------- 蜂蜜机制选项 --------
        honeyEnabled = configurationFile.getBoolean("honeyEnabled", "Honey", true, "Master switch for all honey mechanics.");
        honeyMeltEnabled = configurationFile.getBoolean("honeyMeltEnabled", "Honey", true, "When enabled, Honey Blocks above hot blocks melt into Liquid Honey.");
        honeyCrystallizeEnabled = configurationFile.getBoolean("honeyCrystallizeEnabled", "Honey", true, "When enabled, Liquid Honey exposed to air or on hot blocks will crystallize into Honey Blocks over time.");
        honeyCauldronEnabled = configurationFile.getBoolean("honeyCauldronEnabled", "Honey", true, "When enabled, cauldrons on hot blocks can melt honey blocks and allow jar collection.");
        hotBlocksList = configurationFile.getStringList("hotBlocksList", "Honey", new String[]{"minecraft:lava", "minecraft:flowing_lava", "minecraft:fire"}, "List of block registry names that count as hot blocks (e.g. minecraft:lava, minecraft:fire).");
        honeyMeltInterval = configurationFile.getInt("honeyMeltInterval", "Honey", 20, 1, 200, "Ticks between honey block→liquid honey checks (20 ticks = 1 second).");
        crystallizeExposureTime = configurationFile.getInt("crystallizeExposureTime", "Honey", 600, 20, 72000, "Ticks of air exposure before Liquid Honey crystallizes into a Honey Block (600 = 30 seconds).");
        crystallizeHotTime = configurationFile.getInt("crystallizeHotTime", "Honey", 1200, 20, 72000, "Ticks on a hot block before Liquid Honey crystallizes into a Honey Block (1200 = 60 seconds).");
    }

    public void saveConfigurationFile() {
        configurationFile.save();
    }

    public boolean hasChanged() {
        return configurationFile.hasChanged();
    }
}
