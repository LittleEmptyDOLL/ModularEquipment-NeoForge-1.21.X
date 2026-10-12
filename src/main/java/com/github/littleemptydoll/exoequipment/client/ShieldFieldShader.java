package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

/**
 * Client-side holder for the shield field shader.
 *
 * The rendering setup intentionally mirrors the shield path used by Draconic
 * Evolution 1.21: translucent blending, normal culling, triangle geometry and
 * color+depth writes. Per-player values are uploaded immediately before the
 * field RenderType is flushed by ShieldRenderLayer.
 */
public final class ShieldFieldShader {
    private static ShaderInstance shader;
    private static float renderTime;
    private static float activation;

    private static final RenderType FIELD_TYPE = createFieldType();

    private ShieldFieldShader() {}

    public static void setShader(ShaderInstance value) {
        shader = value;
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static RenderType fieldType(float time, float shieldActivation) {
        renderTime = time;
        activation = Math.max(0.0F, Math.min(1.0F, shieldActivation));
        return FIELD_TYPE;
    }

    private static void uploadUniforms() {
        if (shader == null) {
            return;
        }

        Uniform time = shader.getUniform("Time");
        if (time != null) {
            time.set((renderTime / 20.0F) * 0.42F);
        }

        Uniform activationUniform = shader.getUniform("Activation");
        if (activationUniform != null) {
            activationUniform.set(activation);
        }

        Uniform baseColor = shader.getUniform("BaseColor");
        if (baseColor != null) {
            baseColor.set(0.384F, 0.910F, 1.000F, 0.82F);
        }
    }

    private static RenderType createFieldType() {
        RenderStateShard.ShaderStateShard shaderState =
                new RenderStateShard.ShaderStateShard(() -> {
                    uploadUniforms();
                    return shader;
                });

        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(shaderState)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .createCompositeState(false);

        return RenderType.create(
                "exoequipment_shield_field",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES,
                1536,
                false,
                false,
                state
        );
    }
}
