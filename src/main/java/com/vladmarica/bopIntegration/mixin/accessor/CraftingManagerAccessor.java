package com.vladmarica.bopIntegration.mixin.accessor;

import net.minecraft.item.crafting.CraftingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingManager.class)
public interface CraftingManagerAccessor {

    @Accessor("instance")
    static CraftingManager getInstance() {
        throw new AssertionError();
    }
}
