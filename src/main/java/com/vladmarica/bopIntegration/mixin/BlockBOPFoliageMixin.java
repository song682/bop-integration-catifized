package com.vladmarica.bopIntegration.mixin;

import biomesoplenty.api.content.BOPCItems;
import biomesoplenty.common.blocks.BlockBOPFoliage;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;

/**
 * Replaces the HarvestDropsEvent workaround for turnip seed drops.
 * Meta 12 (Koru) now always drops a turnip seed instead of the original 1/32 rare drop.
 */
@Mixin(BlockBOPFoliage.class)
public class BlockBOPFoliageMixin {

    /**
     * @reason Replace the koru (meta 12) drop behavior: always drop 1 turnip seed.
     * Original logic for all other metadata values is preserved.
     * This avoids needing a high-priority HarvestDropsEvent hack.
     * @author BOPIntegrations
     */
    @Overwrite(remap = false)
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        switch (meta) {
            case 1:
            case 2:
            case 3:
            case 10:
            case 11:
                if (world.rand.nextInt(8) != 0)
                    return ret;
                // Fall through to grass seed logic -- keep original
                break;

            case 5:
                if (world.rand.nextInt(50) != 0)
                    return ret;
                if (world.rand.nextInt(2) == 0) {
                    ret.add(new ItemStack(net.minecraft.init.Items.carrot, 1));
                } else {
                    ret.add(new ItemStack(net.minecraft.init.Items.potato, 1));
                }
                return ret;

            case 12:
                // Always drop 1 turnip seed (original was 1/32 * 1/2 chance)
                ret.add(new ItemStack(BOPCItems.turnipSeeds, 1));
                return ret;

            case 8:
                ret.add(new ItemStack(BOPCItems.food, 1, 0));
                return ret;
        }

        // Reuse original grass seed logic for unhandled cases
        net.minecraft.item.ItemStack item = net.minecraftforge.common.ForgeHooks.getGrassSeed(world);
        if (item != null) {
            ret.add(item);
        }

        return ret;
    }
}
