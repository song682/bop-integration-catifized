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

    public Config(File file) {
        configurationFile = new Configuration(file);
        configurationFile.addCustomCategoryComment("Tweaks", "These options modify BOP itself. Some of these features are unavailable in the 1.7.10 version of BOP but existed in previous or later versions.");
        configurationFile.addCustomCategoryComment("Thaumcraft", "Options to make BOP work better with Thaumcraft");
        configurationFile.addCustomCategoryComment("IC2", "Options to make BOP work better with IC2");

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
    }

    public void saveConfigurationFile() {
        configurationFile.save();
    }

    public boolean hasChanged() {
        return configurationFile.hasChanged();
    }
}
