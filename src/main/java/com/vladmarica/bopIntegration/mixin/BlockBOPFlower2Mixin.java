package com.vladmarica.bopIntegration.mixin;

import biomesoplenty.common.blocks.BlockBOPFlower2;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds compatibility for Miner's Delight (metadata 6) to grow on:
 * - Et Futurum's deepslate
 * - Et Futurum's granite, diorite, andesite (non-polished variants)
 * - Any block registered as stoneGranite / stoneDiorite / stoneAndesite in OreDictionary
 *   (cross-mod compatibility with Chisel, etc.)
 */
@Mixin(value = BlockBOPFlower2.class, remap = false)
public class BlockBOPFlower2Mixin {

    @ModifyReturnValue(method = "isValidPosition", at = @At("RETURN"))
    private boolean onIsValidPosition(boolean original, World world, int x, int y, int z, int metadata) {
        // Only modify Miner's Delight (metadata 6)
        if (metadata != 6) {
            return original;
        }

        // Check if feature is enabled in config
        if (BOPIntegrationMod.config != null && !BOPIntegrationMod.config.minersDelightEtFuturumCompat) {
            return original;
        }

        // Already valid on stone
        if (original) {
            return true;
        }

        Block blockBelow = world.getBlock(x, y - 1, z);
        int blockMeta = world.getBlockMetadata(x, y - 1, z);

        // 1) Check etfuturum blocks by registry name
        String blockName = Block.blockRegistry.getNameForObject(blockBelow);
        if (blockName != null) {
            if ("etfuturum:deepslate".equals(blockName)) {
                return true;
            }
            // Granite(1), Diorite(3), Andesite(5) from BlockBountifulStone
            if ("etfuturum:stone".equals(blockName) && (blockMeta == 1 || blockMeta == 3 || blockMeta == 5)) {
                return true;
            }
        }

        // 2) OreDictionary check — cross-mod compatibility (Chisel etc.)
        Item item = Item.getItemFromBlock(blockBelow);
        if (item != null) {
            ItemStack stack = new ItemStack(item, 1, blockMeta);
            for (int oreId : OreDictionary.getOreIDs(stack)) {
                String name = OreDictionary.getOreName(oreId);
                if ("stoneGranite".equals(name) || "stoneDiorite".equals(name) || "stoneAndesite".equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }
}
