package com.github.littleemptydoll.exoequipment.client.mixin;

import com.github.littleemptydoll.exoequipment.client.ThermalVisionClientEffect;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
abstract class ThermalLightTextureMixin {
    // Enter the vanilla night vision lightmap branch without granting a status effect.
    @ModifyExpressionValue(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z",
                    ordinal = 0
            )
    )
    private boolean exo$thermalNightVision(boolean hasNightVision) {
        return hasNightVision || ThermalVisionClientEffect.isRendering();
    }
}
