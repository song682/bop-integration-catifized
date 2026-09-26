package com.vladmarica.bopIntegration;

import com.vladmarica.bopIntegration.proxy.CommonProxy;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

import static com.vladmarica.bopIntegration.Tags.MODID;

@Mod(
        modid = MODID,
        name = Tags.MODNAME,
        version = Tags.VERSION,
        dependencies = "required-after:BiomesOPlenty;required-after:catframe@[0.5.0,)",
        acceptedMinecraftVersions = "[1.7.10]")
public class BOPIntegrationMod {

    /** Sided proxy holding the mod logic: {@link CommonProxy} on the server, ClientProxy on the client. */
    @SidedProxy(
            clientSide = "com.vladmarica.bopIntegration.proxy.ClientProxy",
            serverSide = "com.vladmarica.bopIntegration.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Config config;

    /**
     * Returns the configuration, loading it on first use. Late mixin loaders evaluate their
     * {@code setApplyIf} conditions during {@code LoaderState.CONSTRUCTING} -- before
     * {@link #preInit} runs -- so they read config values through this early path. Safe to call
     * repeatedly; the same instance is reused afterwards.
     */
    public static Config ensureConfigLoaded() {
        if (config == null) {
            config = new Config(new File(Loader.instance().getConfigDir(), MODID + ".cfg"));
        }
        return config;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
