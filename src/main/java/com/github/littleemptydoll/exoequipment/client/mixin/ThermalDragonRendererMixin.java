package com.github.littleemptydoll.exoequipment.client.mixin;

import com.github.littleemptydoll.exoequipment.client.ThermalVisionRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The dragon extends EntityRenderer, so it cannot receive a LivingEntityRenderer layer. */
@Mixin(EnderDragonRenderer.class)
abstract class ThermalDragonRendererMixin {
    @Shadow @Final private EnderDragonRenderer.DragonModel model;

    // The third model draw is the normal textured body (after the two death-only draws).
    // The dragon's animation and pose are already set up here; the following eyes and
    // crystal beam render as usual. The marker shader keeps texture alpha and depth.
    @Inject(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EnderDragonRenderer$DragonModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
                    ordinal = 2, shift = At.Shift.AFTER)
    )
    private void exo$renderThermalMarker(EnderDragon dragon, float yaw, float partialTick,
                                         PoseStack poseStack, MultiBufferSource bufferSource,
                                         int packedLight, CallbackInfo callback) {
        if (ThermalVisionRenderLayer.shouldRender(dragon)) {
            ThermalVisionRenderLayer.renderMarker(model, poseStack, bufferSource,
                    ((EnderDragonRenderer) (Object) this).getTextureLocation(dragon));
        }
    }
}
