package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Marks visible living entities for the thermal post effect, with normal depth testing. */
public final class ThermalVisionRenderLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.parse("minecraft:textures/misc/white.png");
    // The post shader maps this distinctive color to a white heat silhouette.
    private static final int HEAT_MARKER = 0xFFF20DE3;

    public ThermalVisionRenderLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T target,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            float partialTick
    ) {
        Player viewer = Minecraft.getInstance().player;
        if (viewer == null || target == viewer || !target.isAlive()
                || target.isSpectator() || target.isInvisible()
                || !ThermalVisionClientEffect.isRendering()) {
            return;
        }

        double range = ThermalVisionClientEffect.visibleRange();
        if (viewer.distanceToSqr(target) > range * range
                || !viewer.hasLineOfSight(target)) {
            return;
        }

        getParentModel().renderToBuffer(
                poseStack,
                bufferSource.getBuffer(RenderType.entityTranslucentEmissive(WHITE_TEXTURE)),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                HEAT_MARKER
        );
    }
}
