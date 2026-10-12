package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

/**
 * Client-side holder and render type for the procedural exoskeleton shield field.
 *
 * Per-player energy is passed through the vertex colour alpha. Time and the
 * inverse root pose are uploaded immediately before this RenderType is flushed,
 * so the wave can be evaluated in player/model space instead of camera space.
 */
public final class ShieldFieldShader {
    private static ShaderInstance shader;
    private static float renderTime;
    private static final Matrix4f inversePose = new Matrix4f();

    private static final RenderType FIELD_TYPE = createFieldType();

    private ShieldFieldShader() {}

    public static void setShader(ShaderInstance value) {
        shader = value;
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    public static RenderType fieldType(float time, Matrix4f poseMatrix) {
        renderTime = time;
        inversePose.set(poseMatrix).invert();
        return FIELD_TYPE;
    }

    private static void uploadUniforms() {
        if (shader == null) {
            return;
        }

        Uniform time = shader.getUniform("Time");
        if (time != null) {
            time.set(renderTime / 20.0F);
        }

        Uniform inversePoseUniform = shader.getUniform("InversePoseMat");
        if (inversePoseUniform != null) {
            inversePoseUniform.set(inversePose);
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
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setOverlayState(RenderStateShard.NO_OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .createCompositeState(false);

        return RenderType.create(
                "exoequipment_shield_field",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                state
        );
    }
}
