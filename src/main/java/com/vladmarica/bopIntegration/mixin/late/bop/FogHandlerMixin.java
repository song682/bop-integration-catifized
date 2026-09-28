package com.vladmarica.bopIntegration.mixin.late.bop;

import biomesoplenty.client.fog.FogHandler;
import com.vladmarica.bopIntegration.BOPIntegrationMod;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Exposes BOP's client-side biome fog to this mod's configuration.
 * <p>
 * BOP renders its colored biome fog (Desert, Ominous Woods, Wasteland and other
 * {@code IBiomeFog} biomes) by rewriting the OpenGL fog color ({@code onGetFogColour}) and the
 * fog start/end planes ({@code onRenderFog} through {@code renderFog}). These hooks allow:
 * <ul>
 * <li>{@code disableBopFog}: cancel both handlers so vanilla fog color and distances apply.</li>
 * <li>{@code minFogDistance}: shift the world fog range ({@code fogMode >= 0}) outwards until
 * it starts no closer than the configured amount of chunks, keeping its original thickness.
 * The sky pass ({@code fogMode < 0}) is left untouched so the atmosphere above the horizon
 * keeps its original look.</li>
 * </ul>
 */
@Mixin(FogHandler.class)
public class FogHandlerMixin {

    /** Cancels BOP's fog color blending while the biome fog is disabled. */
    @Inject(method = "onGetFogColour", at = @At("HEAD"), cancellable = true, remap = false)
    private void bopintegrations$disableFogColor(FogColors event, CallbackInfo ci) {
        if (BOPIntegrationMod.ensureConfigLoaded().disableBopFog) {
            ci.cancel();
        }
    }

    /** Cancels BOP's fog distance handling while the biome fog is disabled. */
    @Inject(method = "onRenderFog", at = @At("HEAD"), cancellable = true, remap = false)
    private void bopintegrations$disableFogDistance(RenderFogEvent event, CallbackInfo ci) {
        if (BOPIntegrationMod.ensureConfigLoaded().disableBopFog) {
            ci.cancel();
        }
    }

    /**
     * Pushes the fog range outwards when its start would sit closer than the configured minimum
     * distance, then cancels BOP's own {@code renderFog} body because the GL planes have already
     * been set. See {@code minFogDistance} in {@code Config} for the semantics of the value.
     */
    @Inject(method = "renderFog", at = @At("HEAD"), cancellable = true, remap = false)
    private static void bopintegrations$applyMinFogDistance(int fogMode, float farPlaneDistance, float farPlaneDistanceScale, CallbackInfo ci) {
        int minFogDistanceChunks = BOPIntegrationMod.ensureConfigLoaded().minFogDistance;
        if (minFogDistanceChunks <= 0 || fogMode < 0) {
            return;
        }

        float minDistance = minFogDistanceChunks * 16.0F;
        float fogStart = farPlaneDistance * farPlaneDistanceScale;
        if (fogStart >= minDistance) {
            return;
        }

        GL11.glFogf(GL11.GL_FOG_START, minDistance);
        GL11.glFogf(GL11.GL_FOG_END, farPlaneDistance + (minDistance - fogStart));
        ci.cancel();
    }
}
