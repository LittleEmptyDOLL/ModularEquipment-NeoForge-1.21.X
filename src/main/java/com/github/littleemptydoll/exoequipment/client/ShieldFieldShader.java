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
 * ModelMat is the normal render transform. FieldMat is retained for the
 * dedicated shield pipeline even though the current wave phase is UV-driven.
 * Damage0..Damage3 contain per-part local hex damage in the form
 * (centerU, centerV, radiusInCells, strength).
 */
public final class ShieldFieldShader {
    private static final int DAMAGE_SLOTS = 4;
    private static final int DAMAGE_COMPONENTS = 4;
    private static final float[] NO_DAMAGE =
            new float[DAMAGE_SLOTS * DAMAGE_COMPONENTS];

    private static ShaderInstance shader;
    private static float renderTime;
    private static float opacity;
    private static float breakProgress;
    private static final Matrix4f modelMat = new Matrix4f();
    private static final Matrix4f fieldMat = new Matrix4f();
    private static final float[] damageData =
            new float[DAMAGE_SLOTS * DAMAGE_COMPONENTS];

    private static final RenderType FIELD_TYPE = createFieldType();

    private ShieldFieldShader() {}

    public static void setShader(ShaderInstance value) {
        shader = value;
    }

    public static ShaderInstance getShader() {
        return shader;
    }

    /**
     * Compatibility overload for the older immediate per-part consumer. The
     * active player renderer uses the deferred overload below with damage data.
     */
    public static RenderType fieldType(
            float time,
            float shieldOpacity,
            Matrix4f partModelMat,
            Matrix4f partFieldMat
    ) {
        return fieldType(
                time,
                shieldOpacity,
                0.0F,
                partModelMat,
                partFieldMat,
                NO_DAMAGE
        );
    }

    public static RenderType fieldType(
            float time,
            float shieldOpacity,
            float shieldBreakProgress,
            Matrix4f partModelMat,
            Matrix4f partFieldMat,
            float[] partDamageData
    ) {
        renderTime = time;
        opacity = Math.max(0.0F, Math.min(1.0F, shieldOpacity));
        // Positive values are destruction, negative values reconstruction.
        breakProgress = Math.max(-1.0F, Math.min(1.0F, shieldBreakProgress));
        modelMat.set(partModelMat);
        fieldMat.set(partFieldMat);

        for (int i = 0; i < damageData.length; i++) {
            damageData[i] = i < partDamageData.length
                    ? partDamageData[i]
                    : 0.0F;
        }

        return FIELD_TYPE;
    }

    private static void uploadUniforms() {
        if (shader == null) {
            return;
        }

        Uniform time = shader.getUniform("Time");
        if (time != null) {
            // Same speed as the original Draconic Evolution shield shader.
            time.set(renderTime / 20.0F);
        }

        Uniform activationUniform = shader.getUniform("Activation");
        if (activationUniform != null) {
            // Remaining charge must not shrink the shield envelope. Local hex
            // damage communicates hits instead; Activation is kept fully open.
            activationUniform.set(1.0F);
        }

        Uniform opacityUniform = shader.getUniform("Opacity");
        if (opacityUniform != null) {
            opacityUniform.set(opacity);
        }

        Uniform breakUniform = shader.getUniform("BreakProgress");
        if (breakUniform != null) {
            breakUniform.set(breakProgress);
        }

        Uniform baseColor = shader.getUniform("BaseColor");
        if (baseColor != null) {
            baseColor.set(0.384F, 0.910F, 1.000F, 0.82F);
        }

        Uniform modelMatUniform = shader.getUniform("ModelMat");
        if (modelMatUniform != null) {
            modelMatUniform.set(modelMat);
        }

        Uniform fieldMatUniform = shader.getUniform("FieldMat");
        if (fieldMatUniform != null) {
            fieldMatUniform.set(fieldMat);
        }

        for (int slot = 0; slot < DAMAGE_SLOTS; slot++) {
            Uniform damageUniform = shader.getUniform("Damage" + slot);
            if (damageUniform == null) {
                continue;
            }

            int offset = slot * DAMAGE_COMPONENTS;
            damageUniform.set(
                    damageData[offset],
                    damageData[offset + 1],
                    damageData[offset + 2],
                    damageData[offset + 3]
            );
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
                // The draw itself is deferred until AFTER_ENTITIES. Keeping the
                // item/entity target preserves the water/translucent behaviour
                // that worked with colour-only writes, while the deferred stage
                // fixes ordering against other entities.
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
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
