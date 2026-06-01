package com.vladmarica.bopIntegration.mixin.accessor;

import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.registry.GameRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(GameRegistry.class)
public interface GameRegistryAccessor {

    @Accessor(value = "worldGenerators", remap = false)
    static Set<IWorldGenerator> getWorldGenerators() {
        throw new AssertionError();
    }

    @Accessor(value = "worldGeneratorIndex", remap = false)
    static Map<IWorldGenerator, Integer> getWorldGeneratorIndex() {
        throw new AssertionError();
    }
}
