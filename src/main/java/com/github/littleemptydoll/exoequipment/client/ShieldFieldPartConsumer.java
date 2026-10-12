package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

/**
 * Adapter that makes the dedicated shield mesh follow Draconic Evolution's
 * per-part draw path without requiring CodeChickenLib's VBORenderType.
 *
 * The normal ModelMat still places each part in the rendered world. A second
 * FieldMat strips the render-layer root transform and leaves only the
 * humanoid-part transform, giving the fragment shader camera-independent
 * entity-local coordinates for the animated wave.
 */
public final class ShieldFieldPartConsumer implements VertexConsumer {
    private final MultiBufferSource.BufferSource buffers;
    private final float time;
    private final float activation;
    private final Matrix4f rootInverse = new Matrix4f();
    private final Matrix4f currentModelMat = new Matrix4f();
    private final Matrix4f currentFieldMat = new Matrix4f();

    private boolean hasCurrentModelMat;
    private RenderType currentType;
    private VertexConsumer delegate;

    public ShieldFieldPartConsumer(
            MultiBufferSource.BufferSource buffers,
            float time,
            float activation,
            Matrix4f rootModelMat
    ) {
        this.buffers = buffers;
        this.time = time;
        this.activation = activation;
        this.rootInverse.set(rootModelMat).invert();
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose pose, float x, float y, float z) {
        ensurePart(pose);
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z) {
        ensurePart(pose);
        delegate.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        requireDelegate().addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        requireDelegate().setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        requireDelegate().setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        requireDelegate().setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        requireDelegate().setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        requireDelegate().setNormal(x, y, z);
        return this;
    }

    public void finish() {
        flushCurrent();
        hasCurrentModelMat = false;
        delegate = null;
        currentType = null;
    }

    private void ensurePart(PoseStack.Pose pose) {
        Matrix4f nextModelMat = pose.pose();

        if (hasCurrentModelMat && currentModelMat.equals(nextModelMat)) {
            return;
        }

        flushCurrent();

        currentModelMat.set(nextModelMat);
        currentFieldMat.set(rootInverse).mul(currentModelMat);
        hasCurrentModelMat = true;

        currentType = ShieldFieldShader.fieldType(
                time,
                activation,
                currentModelMat,
                currentFieldMat
        );
        delegate = buffers.getBuffer(currentType);
    }

    private void flushCurrent() {
        if (currentType != null) {
            buffers.endBatch(currentType);
        }
    }

    private VertexConsumer requireDelegate() {
        if (delegate == null) {
            throw new IllegalStateException("Shield vertex attributes were written before a part pose was selected");
        }
        return delegate;
    }
}
