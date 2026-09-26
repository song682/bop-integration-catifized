package com.vladmarica.bopIntegration.mixin.hee;

import biomesoplenty.api.content.BOPCBlocks;
import chylex.hee.world.structure.tower.ComponentTower;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Replaces the glowstone blocks of HEE's Dungeon Tower with BOP celestial crystals.
 */
@Mixin(value = ComponentTower.class, remap = false)
public abstract class MixinComponetTower {

    /**
     * Rewrites the block argument of every {@code setBlock(x, y, z, block, meta, flag)} call made in
     * {@code setupStructure}; only glowstone is swapped, because the same overload is also used for the
     * obsidian decoration of the tower, which must stay untouched.
     */
    @ModifyArg(
            method = "setupStructure(J)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lchylex/hee/world/structure/util/pregen/LargeStructureWorld;setBlock(IIILnet/minecraft/block/Block;IZ)V",
                    remap = false
            ),
            index = 3,
            remap = false
    )
    private Block replaceGlowstoneWithCrystal(Block block) {
        if (BOPIntegrationMod.config.replaceGlowStoneInTower && block == Blocks.glowstone) {
            return BOPCBlocks.crystal;
        }
        return block;
    }
}
