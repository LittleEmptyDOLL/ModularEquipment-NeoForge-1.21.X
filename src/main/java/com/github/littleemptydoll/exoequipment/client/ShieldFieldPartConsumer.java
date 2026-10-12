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
 * ShieldFieldMesh supplies local vertices/UVs. This consumer keeps those
 * vertices local, uploads the current humanoid-part pose as ModelMat, and
 * flushes a part before a different ModelMat is used.
 */
public final class ShieldFieldPartConsumer implements VertexConsumer {
    private final MultiBufferSource.BufferSource buffers;
    private final float time;
    private final float activation;
    private final Matrix4f currentModelMat = new Matrix4f();

    private boolean hasCurrentModelMat;
    private RenderType currentType;
    private VertexConsumer delegate;

    public ShieldFieldPartConsumer(
            MultiBufferSource.BufferSource buffers,
            float time,
            float activation
    ) {
        this.buffers = buffers;
        this.time = time;
        this.activation = activation;
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

        /*
         * Do not compare PoseStack.Pose object identity here. Modern PoseStack
         * implementations may reuse a Pose object when repeatedly pushing and
         * popping at the same depth. In that case head/body/arm/leg renders can
         * arrive through the same Pose instance with different matrix contents.
         * If the head matrix is accidentally kept, camera/player pitch rotates
         * the entire procedural field and looks exactly like a camera-bound wave.
         */
        if (hasCurrentModelMat && currentModelMat.equals(nextModelMat)) {
            return;
        }

        flushCurrent();

        currentModelMat.set(nextModelMat);
        hasCurrentModelMat = true;
        currentType = ShieldFieldShader.fieldType(time, activation, currentModelMat);
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
