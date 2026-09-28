package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/** Marks the textured, visible portions of living entities for the thermal post effect. */
public final class ThermalVisionRenderLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    // The post shader maps this distinctive color to a white heat silhouette.
    public static final int HEAT_MARKER = 0xFFF20DE3;
    private static final Map<ResourceLocation, RenderType> SILHOUETTES = new HashMap<>();
    private static final RenderStateShard.ShaderStateShard MARKER_SHADER =
            new RenderStateShard.ShaderStateShard(ThermalVisionClientRenderer::markerShader);

    private static RenderType silhouette(ResourceLocation texture) {
        return SILHOUETTES.computeIfAbsent(texture, ThermalVisionRenderLayer::createSilhouette);
    }

    private static RenderType createSilhouette(ResourceLocation texture) {
        return RenderType.create(
                "exoequipment:thermal_silhouette",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(MARKER_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setOutputState(RenderStateShard.MAIN_TARGET)
                        .createCompositeState(false)
        );
    }

    public ThermalVisionRenderLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    /** For renderers with their own model pass, draw this after the ordinary textured body. */
    public static void renderMarker(EntityModel<?> model, PoseStack poseStack,
                                    MultiBufferSource bufferSource, ResourceLocation texture) {
        model.renderToBuffer(poseStack, markerBuffer(bufferSource, texture),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, HEAT_MARKER);
    }

    /** Custom renderers can submit their own textured geometry with the marker color. */
    public static VertexConsumer markerBuffer(MultiBufferSource bufferSource, ResourceLocation texture) {
        return bufferSource.getBuffer(silhouette(texture));
    }

    public static boolean shouldRender(LivingEntity target) {
        return ThermalVisionClientEffect.isRendering()
                && ThermalVisionClientRenderer.markerShader() != null
                && ThermalVisionTargets.shouldHighlight(target, Minecraft.getInstance().player);
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
        if (!shouldRender(target)) {
            return;
        }

        renderMarker(getParentModel(), poseStack, bufferSource, getTextureLocation(target));
    }
}
