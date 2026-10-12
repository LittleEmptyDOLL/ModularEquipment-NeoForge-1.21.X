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
    private static final int DAMAGE_SLOTS = 4;
    private static final int DAMAGE_COMPONENTS = 4;

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
            float opacity,
            List<DamageImpact> damageImpacts
    ) {
        if (!collecting) {
            return;
        }

        Matrix4f rootModelMat = new Matrix4f(poseStack.last().pose());
        CaptureConsumer capture = new CaptureConsumer(rootModelMat);

        ShieldFieldMesh.render(poseStack, capture, source);

        List<PartBatch> parts = capture.finish();
        if (!parts.isEmpty()) {
            QUEUE.add(new QueuedShield(
                    time,
                    opacity,
                    List.copyOf(damageImpacts),
                    parts
            ));
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
                float[] damageData = resolveDamageData(
                        shield.damageImpacts(),
                        part
                );

                RenderType type = ShieldFieldShader.fieldType(
                        shield.time(),
                        shield.opacity(),
                        part.modelMat(),
                        part.fieldMat(),
                        damageData
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

                // Uniforms contain a different ModelMat and damage set for every
                // body part, so flush before those values are changed again.
                buffers.endBatch(type);
            }
        }

        QUEUE.clear();
    }

    private static float[] resolveDamageData(
            List<DamageImpact> impacts,
            PartBatch part
    ) {
        float[] data = new float[DAMAGE_SLOTS * DAMAGE_COMPONENTS];
        if (impacts.isEmpty() || part.vertices().size() < 3) {
            return data;
        }

        int slot = 0;
        for (DamageImpact impact : impacts) {
            if (impact.partIndex() != part.partIndex()) {
                continue;
            }
            if (slot >= DAMAGE_SLOTS) {
                break;
            }

            float[] center = sampleUvInsidePart(
                    part,
                    impact.seed(),
                    impact.directionX(),
                    impact.directionZ()
            );
            int offset = slot * DAMAGE_COMPONENTS;
            data[offset] = center[0];
            data[offset + 1] = center[1];
            data[offset + 2] = impact.radius();
            data[offset + 3] = impact.strength();
            slot++;
        }

        return data;
    }

    private static float[] sampleUvInsidePart(
            PartBatch part,
            int seed,
            float directionX,
            float directionZ
    ) {
        List<VertexData> vertices = part.vertices();
        int triangleCount = vertices.size() / 3;
        int triangle;

        float directionLength = (float) Math.sqrt(
                (directionX * directionX)
                        + (directionZ * directionZ)
        );

        if (directionLength <= 0.0001F) {
            triangle = Math.floorMod(seed, triangleCount);
        } else {
            directionX /= directionLength;
            directionZ /= directionLength;
            triangle = selectFacingTriangle(
                    part,
                    directionX,
                    directionZ,
                    seed
            );
        }

        int first = triangle * 3;

        VertexData a = vertices.get(first);
        VertexData b = vertices.get(first + 1);
        VertexData c = vertices.get(first + 2);

        float randomA = unitFloat(mixSeed(seed ^ 0x68BC21EB));
        float randomB = unitFloat(mixSeed(seed ^ 0x02E5BE93));

        // Uniform barycentric sampling keeps the impact center on the selected
        // real UV triangle rather than in an unused atlas region. The selected
        // triangle now belongs to the shield face pointing toward the source.
        float root = (float) Math.sqrt(randomA);
        float weightA = 1.0F - root;
        float weightB = root * (1.0F - randomB);
        float weightC = root * randomB;

        float rawU = (a.u() * weightA)
                + (b.u() * weightB)
                + (c.u() * weightC);
        float rawV = (a.v() * weightA)
                + (b.v() * weightB)
                + (c.v() * weightC);

        // CodeChickenLib flips OBJ V before drawing the original DE mesh. The
        // fragment shader performs the same conversion for texCoord0, so pass
        // damage centers in that already-converted shieldUv coordinate space.
        return new float[] { rawU, 1.0F - rawV };
    }

    private static int selectFacingTriangle(
            PartBatch part,
            float directionX,
            float directionZ,
            int seed
    ) {
        List<VertexData> vertices = part.vertices();
        Matrix4f fieldMat = part.fieldMat();
        int triangleCount = vertices.size() / 3;
        int bestTriangle = Math.floorMod(seed, triangleCount);
        float bestScore = -Float.MAX_VALUE;

        for (int triangle = 0; triangle < triangleCount; triangle++) {
            VertexData vertex = vertices.get(triangle * 3);

            // Vertex normals are in the local coordinates of the animated body
            // part. FieldMat transforms the direction into the player's root
            // body space without introducing camera rotation or translation.
            float normalX = vertex.normalX();
            float normalY = vertex.normalY();
            float normalZ = vertex.normalZ();

            float rootNormalX = (fieldMat.m00() * normalX)
                    + (fieldMat.m10() * normalY)
                    + (fieldMat.m20() * normalZ);
            float rootNormalZ = (fieldMat.m02() * normalX)
                    + (fieldMat.m12() * normalY)
                    + (fieldMat.m22() * normalZ);

            float horizontalNormalLength = (float) Math.sqrt(
                    (rootNormalX * rootNormalX)
                            + (rootNormalZ * rootNormalZ)
            );

            // Top/bottom faces have no meaningful horizontal facing direction
            // for the current hit payload, so prefer one of the side surfaces.
            float score = -2.0F;
            if (horizontalNormalLength > 0.0001F) {
                score = ((rootNormalX * directionX)
                        + (rootNormalZ * directionZ))
                        / horizontalNormalLength;
            }

            // Both triangles of one rectangular face normally have identical
            // normals. A tiny deterministic jitter chooses between them without
            // ever overpowering the actual face-direction score.
            float jitter = unitFloat(mixSeed(
                    seed ^ (triangle * 0x632BE5AB)
            )) * 0.002F;
            score += jitter;

            if (score > bestScore) {
                bestScore = score;
                bestTriangle = triangle;
            }
        }

        return bestTriangle;
    }

    private static float unitFloat(int value) {
        return (value & 0x00FFFFFF) / 16777216.0F;
    }

    private static int mixSeed(int value) {
        value ^= value >>> 16;
        value *= 0x7FEB352D;
        value ^= value >>> 15;
        value *= 0x846CA68B;
        value ^= value >>> 16;
        return value;
    }

    /**
     * A transient local break in the shield lattice. Radius is expressed in
     * hex-grid cells; strength decays from 1 to 0 while the field repairs.
     * directionX/directionZ point toward the damage source in root body space.
     */
    public record DamageImpact(
            int partIndex,
            int seed,
            float radius,
            float strength,
            float directionX,
            float directionZ
    ) {}

    private record QueuedShield(
            float time,
            float opacity,
            List<DamageImpact> damageImpacts,
            List<PartBatch> parts
    ) {}

    private record PartBatch(
            int partIndex,
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
        private final int partIndex;
        private final Matrix4f modelMat;
        private final Matrix4f fieldMat;
        private final List<VertexData> vertices = new ArrayList<>();

        private PartBuilder(
                int partIndex,
                Matrix4f modelMat,
                Matrix4f fieldMat
        ) {
            this.partIndex = partIndex;
            this.modelMat = modelMat;
            this.fieldMat = fieldMat;
        }

        private PartBatch build() {
            return new PartBatch(
                    partIndex,
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
                    parts.size(),
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
