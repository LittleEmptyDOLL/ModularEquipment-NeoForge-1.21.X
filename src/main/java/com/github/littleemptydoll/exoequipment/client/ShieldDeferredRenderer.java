package com.github.littleemptydoll.exoequipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects shield geometry while players are rendered, then draws it once the
 * normal entity pass has finished. This lets the translucent shield blend over
 * entities behind it without writing depth and hiding water/translucent blocks.
 */
public final class ShieldDeferredRenderer {
    private static final List<QueuedShield> QUEUE = new ArrayList<>();
    private static boolean collecting;

    private ShieldDeferredRenderer() {}

    public static void beginFrame() {
        QUEUE.clear();
        collecting = true;
    }

    public static boolean isCollecting() {
        return collecting;
    }

    public static void enqueue(
            PoseStack poseStack,
            PlayerModel<AbstractClientPlayer> source,
            float time,
            float activation
    ) {
        if (!collecting) {
            return;
        }

        Matrix4f rootModelMat = new Matrix4f(poseStack.last().pose());
        CaptureConsumer capture = new CaptureConsumer(rootModelMat);

        ShieldFieldMesh.render(poseStack, capture, source);

        List<PartBatch> parts = capture.finish();
        if (!parts.isEmpty()) {
            QUEUE.add(new QueuedShield(time, activation, parts));
        }
    }

    public static void renderQueued() {
        collecting = false;

        if (QUEUE.isEmpty() || ShieldFieldShader.getShader() == null) {
            QUEUE.clear();
            return;
        }

        MultiBufferSource.BufferSource buffers =
                Minecraft.getInstance().renderBuffers().bufferSource();

        for (QueuedShield shield : QUEUE) {
            for (PartBatch part : shield.parts()) {
                RenderType type = ShieldFieldShader.fieldType(
                        shield.time(),
                        shield.activation(),
                        part.modelMat(),
                        part.fieldMat()
                );

                VertexConsumer buffer = buffers.getBuffer(type);
                for (VertexData vertex : part.vertices()) {
                    buffer.addVertex(vertex.x(), vertex.y(), vertex.z())
                            .setColor(vertex.red(), vertex.green(), vertex.blue(), vertex.alpha())
                            .setUv(vertex.u(), vertex.v())
                            .setUv1(vertex.overlayU(), vertex.overlayV())
                            .setUv2(vertex.lightU(), vertex.lightV())
                            .setNormal(vertex.normalX(), vertex.normalY(), vertex.normalZ());
                }

                // Uniforms contain a different ModelMat for every body part, so
                // each part must be flushed before the next one changes them.
                buffers.endBatch(type);
            }
        }

        QUEUE.clear();
    }

    private record QueuedShield(
            float time,
            float activation,
            List<PartBatch> parts
    ) {}

    private record PartBatch(
            Matrix4f modelMat,
            Matrix4f fieldMat,
            List<VertexData> vertices
    ) {}

    private record VertexData(
            float x,
            float y,
            float z,
            int red,
            int green,
            int blue,
            int alpha,
            float u,
            float v,
            int overlayU,
            int overlayV,
            int lightU,
            int lightV,
            float normalX,
            float normalY,
            float normalZ
    ) {}

    private static final class PartBuilder {
        private final Matrix4f modelMat;
        private final Matrix4f fieldMat;
        private final List<VertexData> vertices = new ArrayList<>();

        private PartBuilder(Matrix4f modelMat, Matrix4f fieldMat) {
            this.modelMat = modelMat;
            this.fieldMat = fieldMat;
        }

        private PartBatch build() {
            return new PartBatch(
                    new Matrix4f(modelMat),
                    new Matrix4f(fieldMat),
                    List.copyOf(vertices)
            );
        }
    }

    /**
     * VertexConsumer used only while the normal player renderer is active. It
     * records the local vertices and the exact per-part pose matrices instead
     * of submitting a draw call immediately.
     */
    private static final class CaptureConsumer implements VertexConsumer {
        private final Matrix4f rootInverse = new Matrix4f();
        private final List<PartBuilder> parts = new ArrayList<>();
        private final Matrix4f currentModelMat = new Matrix4f();

        private boolean hasCurrentModelMat;
        private PartBuilder currentPart;

        private float x;
        private float y;
        private float z;
        private int red = 255;
        private int green = 255;
        private int blue = 255;
        private int alpha = 255;
        private float u;
        private float v;
        private int overlayU;
        private int overlayV;
        private int lightU;
        private int lightV;
        private boolean vertexStarted;

        private CaptureConsumer(Matrix4f rootModelMat) {
            rootInverse.set(rootModelMat).invert();
        }

        @Override
        public VertexConsumer addVertex(PoseStack.Pose pose, float x, float y, float z) {
            ensurePart(pose.pose());
            beginVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z) {
            ensurePart(pose.pose());
            finishVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            if (currentPart == null) {
                throw new IllegalStateException("Shield vertex was written before a part pose was selected");
            }
            beginVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.overlayU = u;
            this.overlayV = v;
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.lightU = u;
            this.lightV = v;
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            finishVertex(x, y, z);
            return this;
        }

        private void ensurePart(Matrix4f nextModelMat) {
            if (hasCurrentModelMat && currentModelMat.equals(nextModelMat)) {
                return;
            }

            if (vertexStarted) {
                throw new IllegalStateException("Shield part changed before the current vertex was completed");
            }

            currentModelMat.set(nextModelMat);
            hasCurrentModelMat = true;

            Matrix4f fieldMat = new Matrix4f(rootInverse).mul(currentModelMat);
            currentPart = new PartBuilder(
                    new Matrix4f(currentModelMat),
                    fieldMat
            );
            parts.add(currentPart);
        }

        private void beginVertex(float x, float y, float z) {
            if (vertexStarted) {
                throw new IllegalStateException("Shield vertex started before the previous vertex was completed");
            }

            this.x = x;
            this.y = y;
            this.z = z;
            this.red = 255;
            this.green = 255;
            this.blue = 255;
            this.alpha = 255;
            this.u = 0.0F;
            this.v = 0.0F;
            this.overlayU = 0;
            this.overlayV = 0;
            this.lightU = 0;
            this.lightV = 0;
            this.vertexStarted = true;
        }

        private void finishVertex(float normalX, float normalY, float normalZ) {
            if (!vertexStarted || currentPart == null) {
                throw new IllegalStateException("Shield normal was written before a vertex was started");
            }

            currentPart.vertices.add(new VertexData(
                    x, y, z,
                    red, green, blue, alpha,
                    u, v,
                    overlayU, overlayV,
                    lightU, lightV,
                    normalX, normalY, normalZ
            ));
            vertexStarted = false;
        }

        private List<PartBatch> finish() {
            if (vertexStarted) {
                throw new IllegalStateException("Shield capture ended with an incomplete vertex");
            }

            List<PartBatch> result = new ArrayList<>(parts.size());
            for (PartBuilder part : parts) {
                if (!part.vertices.isEmpty()) {
                    result.add(part.build());
                }
            }
            return List.copyOf(result);
        }
    }
}
