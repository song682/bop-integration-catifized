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
    public boolean fixSilverwoodTrees;
    public boolean addMissingAspects;

    public Config(File file) {
        configurationFile = new Configuration(file);
        configurationFile.addCustomCategoryComment("Tweaks", "These options modify BOP itself. Some of these features are unavailable in the 1.7.10 version of BOP but existed in previous or later versions.");
        configurationFile.addCustomCategoryComment("Thaumcraft", "Options to make BOP work better with Thaumcraft");
        configurationFile.addCustomCategoryComment("IC2", "Options to make BOP work better with IC2");

        configurationFile.load();
        sync();
        save();
    }

    public void sync() {
        genCelestialCrystals = configurationFile.getBoolean("genCelestialCrystals", "Tweaks", true, "Generate Celestial Crystals in the End. Used to make Ambrosia.");
        genBiomeEssence = configurationFile.getBoolean("genBiomeEssence", "Tweaks", true, "Generate Biome Essence Ore in the End. Drops Biome Essence.");
        removeNetherGravestones = configurationFile.getBoolean("removeNetherGravestones", "Tweaks", true, "Prevent gravestones from spawning in the Nether. They are ugly and useless.");
        craftableRottenFlesh = configurationFile.getBoolean("craftableRottenFlesh", "Tweaks", true, "Adds a recipe to craft rotten flesh out of flesh chunks and an eyebulb.");
        removeEnderporterRecipe = configurationFile.getBoolean("removeEnderporterRecipe", "Tweaks", true, "It can still be cheating in by an op.");
        harderBiomeFinderRecipe = configurationFile.getBoolean("harderBiomeFinderRecipe", "Tweaks", true, "Makes the recipe use end crystals and ghastly souls.");
        waspHiveRarityModifier = configurationFile.getFloat("waspHiveRarityModifier", "Tweaks", 1.0F, 0.0F, 1.0F, "You can use this option to make nether wasp hives rarer.");
        fixSilverwoodTrees = configurationFile.getBoolean("fixSilverwoodTrees", "Thaumcraft", true, "Allows Silverwood trees to spawn in all forest and plains biomes.");
        addMissingAspects = configurationFile.getBoolean("addMissingAspects", "Thaumcraft", true, "Many BOP items don't give any aspects. ");
    }

    public void save() {
        configurationFile.save();
    }

    public boolean hasChanged() {
        return configurationFile.hasChanged();
    }
}
