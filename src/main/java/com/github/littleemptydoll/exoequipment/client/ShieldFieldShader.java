package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

/**
 * Client-side holder for the shield field shader.
 *
 * This intentionally mirrors Draconic Evolution's chestpiece shield pipeline:
 * each shield body part is rendered from local-space vertices with its own
 * ModelMat uniform, while Time, Activation and BaseColor are supplied immediately
 * before that part is flushed.
 */
public final class ShieldFieldShader {
    private static ShaderInstance shader;
    private static float renderTime;
    private static float activation;
    private static final Matrix4f modelMat = new Matrix4f();

    private static final RenderType FIELD_TYPE = createFieldType();

    private ShieldFieldShader() {}

    public static void setShader(ShaderInstance value) {
        shader = value;
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static RenderType fieldType(
            float time,
            float shieldActivation,
            Matrix4f partModelMat
    ) {
        renderTime = time;
        activation = Math.max(0.0F, Math.min(1.0F, shieldActivation));
        modelMat.set(partModelMat);
        return FIELD_TYPE;
    }

    private static void uploadUniforms() {
        if (shader == null) {
            return;
        }

        Uniform time = shader.getUniform("Time");
        if (time != null) {
            // Draconic Evolution uses render time / 20 with no extra speed scale.
            time.set(renderTime / 20.0F);
        }

        Uniform activationUniform = shader.getUniform("Activation");
        if (activationUniform != null) {
            activationUniform.set(activation);
        }

        Uniform baseColor = shader.getUniform("BaseColor");
        if (baseColor != null) {
            baseColor.set(0.384F, 0.910F, 1.000F, 0.82F);
        }

        Uniform modelMatUniform = shader.getUniform("ModelMat");
        if (modelMatUniform != null) {
            modelMatUniform.set(modelMat);
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
                // DE calls backfacedCopy() on each shield CCModel. With a shader
                // that does not depend on face normal, disabling culling is the
                // equivalent result without duplicating every triangle.
                .setCullState(RenderStateShard.NO_CULL)
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
