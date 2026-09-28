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
import org.objectweb.asm.Opcodes;

/** The dragon extends EntityRenderer, so it cannot receive a LivingEntityRenderer layer. */
@Mixin(EnderDragonRenderer.class)
abstract class ThermalDragonRendererMixin {
    @Shadow @Final private EnderDragonRenderer.DragonModel model;

    // The eyes render after the body in both the normal and death branches. At this
    // point the model is animated and the dragon's pose is still on the stack.
    // Anchor to the eye render type instead of the model call: vanilla uses both
    // the four-argument and five-argument model render overloads here.
    @Inject(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/entity/EnderDragonRenderer;EYES:Lnet/minecraft/client/renderer/RenderType;",
                    opcode = Opcodes.GETSTATIC),
            require = 0
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
