package com.vladmarica.bopIntegration.mixin.accessor;

import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeGenBase.class)
public interface BiomeGenBaseAccessor {

    @Accessor("theBiomeDecorator")
    BiomeDecorator getTheBiomeDecorator();
}
