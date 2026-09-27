package com.github.littleemptydoll.exoequipment.client.mixin;

import com.github.littleemptydoll.exoequipment.client.ThermalVisionClientEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
abstract class ThermalGameRendererMixin {
    private static final float THERMAL_NIGHT_VISION_STRENGTH = 0.75F;

    // Vanilla assumes an effect is present here; supply our own lightmap strength.
    @Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
    private static void exo$thermalNightVisionScale(
            LivingEntity entity,
            float partialTick,
            CallbackInfoReturnable<Float> result
    ) {
        if (entity != null
                && entity == Minecraft.getInstance().player
                && ThermalVisionClientEffect.isRendering()
                && !entity.hasEffect(MobEffects.NIGHT_VISION)) {
            result.setReturnValue(THERMAL_NIGHT_VISION_STRENGTH);
        }
    }
}
