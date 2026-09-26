package com.vladmarica.bopIntegration.mixin.late.bop;

import biomesoplenty.common.blocks.BlockBOPFoliage;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import net.minecraft.block.IGrowable;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Random;

/**
 * Duck-types {@link BlockBOPFoliage} into {@link IGrowable} so bone meal can advance whitelisted
 * foliage variants to their next growth stage.
 * <p>
 * BOP's foliage never implements {@code IGrowable}, so vanilla's bone meal path
 * ({@code ItemDye.applyBonemeal} checks {@code block instanceof IGrowable}) simply skips it.
 * This mixin merges the interface and its three methods into the block at class load time;
 * nothing else about the block is touched and no random tick behavior is added, growth only
 * happens when a player uses bone meal on the plant.
 * <p>
 * The whitelist lives in the config ({@code growableBopFoliage}, a list of source metadata
 * values). The achievable stages are fixed: 1 (shortgrass) -&gt; 2 (mediumgrass) and
 * 4 (bush) -&gt; 8 (berrybush, berries regrow). A whitelisted metadata value without a built-in
 * next stage cannot grow. The 4 -&gt; 8 transition mirrors BOP's own berry bush harvesting, which
 * turns meta 8 back into meta 4 when picked, so bushes can be harvested and regrown repeatedly.
 * <p>
 * The config gate is evaluated once at startup (see {@code Mixins.LateMixins.BOP_FOLIAGE_GROWTH}),
 * so changing the whitelist requires a game restart.
 */
@Mixin(BlockBOPFoliage.class)
public abstract class BlockBOPFoliageGrowthMixin implements IGrowable {

    /**
     * Bone meal applies when the block is whitelisted and has a built-in next stage. Deterministic
     * on both sides, because the client calls this too as a prediction of the server result.
     */
    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isClient) {
        return bopintegrations$nextStage(world.getBlockMetadata(x, y, z)) != -1;
    }

    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return true;
    }

    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        int nextStage = bopintegrations$nextStage(world.getBlockMetadata(x, y, z));

        if (nextStage != -1) {
            // Flag 2 matches vanilla crop growth: sync the new metadata to clients without
            // notifying neighbors, which no foliage variant depends on.
            world.setBlockMetadataWithNotify(x, y, z, nextStage, 2);
        }
    }

    /**
     * Returns the metadata this variant grows into, or -1 when it cannot grow. A variant can only
     * grow when it is both whitelisted in the config and has a built-in next stage.
     */
    @Unique
    private static int bopintegrations$nextStage(int meta) {
        boolean whitelisted = false;
        for (int allowed : BOPIntegrationMod.ensureConfigLoaded().growableBopFoliage) {
            if (allowed == meta) {
                whitelisted = true;
                break;
            }
        }

        if (!whitelisted) {
            return -1;
        }

        switch (meta) {
            case 1:
                return 2; // shortgrass -> mediumgrass
            case 4:
                return 8; // bush -> berrybush (berries regrow)
            default:
                return -1;
        }
    }
}
