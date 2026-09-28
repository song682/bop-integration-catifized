package com.vladmarica.bopIntegration.mixin;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;
import com.vladmarica.bopIntegration.BOPIntegrationMod;

/**
 * Declarative registry of every mixin shipped by the mod.
 * <p>
 * {@link NormalMixins} are served by {@link BOPIntegrationsMixinPlugin} through the ordinary
 * {@code mixins.bopintegration.json} config, so their builders must leave the phase unset
 * ({@code null}). {@link LateMixins} are served by {@link BOPIntegrationsLateMixinLoader}
 * through {@code mixins.bopintegration.late.json}, so their builders must set
 * {@code Phase.LATE}, with mod targets resolved against the loaded mods.
 */
public class Mixins {

    public enum NormalMixins implements IMixins {

        /** Accessors into vanilla and Forge internals shared by the tweak modules. */
        ACCESSORS(Side.COMMON,
                "accessor.GameRegistryAccessor",
                "accessor.CraftingManagerAccessor",
                "accessor.EventBusAccessor",
                "accessor.BiomeGenBaseAccessor");

        private final MixinBuilder builder;

        NormalMixins(Side side, String... mixins) {
            this.builder = new MixinBuilder().addSidedMixins(side, mixins);
        }

        @Override
        public MixinBuilder getBuilder() {
            return this.builder;
        }
    }

    public enum LateMixins implements IMixins {

        /** Reduced IC2 rubber tree generation in BOP grassland and marsh biomes. */
        IC2_RUBBER_TREES(new MixinBuilder().setPhase(Phase.LATE)
                .addCommonMixins("ic2.WorldGenRubTreeMixin")
                .addRequiredMod(new TargetModBuilder().setModId("IC2"))
                .setApplyIf(() -> BOPIntegrationMod.ensureConfigLoaded().fixIC2RubberTrees)
        ),

        /** Replaces the glowstone blocks of HEE's Dungeon Tower with BOP celestial crystals. */
        HEE_TOWER_GLOWSTONE(new MixinBuilder().setPhase(Phase.LATE)
                .addCommonMixins("hee.MixinComponetTower")
                .addRequiredMod(new TargetModBuilder().setModId("HardcoreEnderExpansion"))
                .setApplyIf(() -> BOPIntegrationMod.ensureConfigLoaded().replaceGlowStoneInTower)
        ),
        /** BOP world generation tweaks: nether gravestone and wasp hive conditions, plus end chunk population. */
        BOP_WORLDGEN(new MixinBuilder().setPhase(Phase.LATE)
                .addCommonMixins(
                        "bop.WorldGenWaspHiveMixin",
                        "bop.WorldGenGraveMixin",
                        "bop.ChunkProviderBOPEndMixin")
        ),

        /** Client-side BOP biome fog (FogHandler): global disable and minimum fog distance. */
        BOP_FOG(new MixinBuilder().setPhase(Phase.LATE)
                .addClientMixins("bop.FogHandlerMixin")
                .setApplyIf(() -> BOPIntegrationMod.ensureConfigLoaded().disableBopFog)
        ),

        /** Bone meal growth for whitelisted BlockBOPFoliage variants, duck-typed into IGrowable. */
        BOP_FOLIAGE_GROWTH(new MixinBuilder().setPhase(Phase.LATE)
                .addCommonMixins("bop.BlockBOPFoliageGrowthMixin")
                .addRequiredMod(new TargetModBuilder().setModId("BiomesOPlenty"))
                .setApplyIf(() -> BOPIntegrationMod.ensureConfigLoaded().growableBopFoliage.length > 0)
        );

        private final MixinBuilder builder;

        LateMixins(MixinBuilder builder) {
            this.builder = builder;
        }

        @Override
        public MixinBuilder getBuilder() {
            return this.builder;
        }
    }
}
