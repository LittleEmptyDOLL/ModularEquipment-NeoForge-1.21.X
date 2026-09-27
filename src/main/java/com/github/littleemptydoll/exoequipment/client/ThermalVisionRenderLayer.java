package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Marks the visible portions of living entities for the thermal post effect. */
public final class ThermalVisionRenderLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.parse("minecraft:textures/misc/white.png");
    // The post shader maps this distinctive color to a white heat silhouette.
    private static final int HEAT_MARKER = 0xFFF20DE3;
    private static final RenderType HEAT_SILHOUETTE = RenderType.create(
            "exoequipment:thermal_silhouette",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    // The eyes shader keeps the marker color identical on every face.
                    .setShaderState(RenderStateShard.RENDERTYPE_EYES_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(WHITE_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setOutputState(RenderStateShard.MAIN_TARGET)
                    .createCompositeState(false)
    );

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

        getParentModel().renderToBuffer(
                poseStack,
                bufferSource.getBuffer(HEAT_SILHOUETTE),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                HEAT_MARKER
        );
    }
}
