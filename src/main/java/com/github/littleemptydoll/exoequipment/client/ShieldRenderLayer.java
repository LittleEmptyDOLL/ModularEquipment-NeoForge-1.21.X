package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Player shield render layer.
 *
 * The field keeps Draconic Evolution's dedicated per-part mesh/render path,
 * while the animated wave itself is evaluated in entity-local space. This
 * prevents a change in the visible UV face or camera pitch from changing the
 * phase/location of the energy front.
 */
public final class ShieldRenderLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float VISUAL_RESPONSE = 0.35F;
    private static final float MIN_VISIBLE_ENERGY = 0.01F;

    private final Map<AbstractClientPlayer, VisualState> visualStates =
            new WeakHashMap<>();

    public ShieldRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            boolean slim
    ) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (player.isInvisible() || player.isSpectator()) {
            return;
        }

        var context = ExoskeletonAccess.findContext(player).orElse(null);
        if (context == null) {
            visualStates.remove(player);
            return;
        }

        ShieldOperations.ShieldStatus status =
                ShieldOperations.getStatus(context.data());

        float targetEnergy = getEnergyRatio(status);
        VisualState state = visualStates.computeIfAbsent(
                player,
                ignored -> new VisualState(targetEnergy)
        );

        state.update(targetEnergy);
        float energy = state.visualEnergy();

        if (!status.hasShields() && energy <= MIN_VISIBLE_ENERGY) {
            visualStates.remove(player);
            return;
        }

        if (energy <= MIN_VISIBLE_ENERGY || ShieldFieldShader.getShader() == null) {
            return;
        }

        if (!(bufferSource instanceof MultiBufferSource.BufferSource immediate)) {
            return;
        }

        float time;
        if (Minecraft.getInstance().level != null) {
            time = Minecraft.getInstance().level.getGameTime() + partialTick;
        } else {
            time = player.tickCount + partialTick;
        }

        /*
         * Capture the render-layer root before individual head/body/limb
         * transforms are applied. ShieldFieldPartConsumer removes this root
         * from each part matrix and supplies the resulting entity-local matrix
         * separately to the shader. Camera/entity placement therefore affects
         * gl_Position, but never the coordinates used to calculate the wave.
         */
        Matrix4f rootModelMat = new Matrix4f(poseStack.last().pose());

        ShieldFieldPartConsumer consumer =
                new ShieldFieldPartConsumer(immediate, time, energy, rootModelMat);

        ShieldFieldMesh.render(
                poseStack,
                consumer,
                getParentModel()
        );

        consumer.finish();
    }

    private static float getEnergyRatio(
            ShieldOperations.ShieldStatus status
    ) {
        if (!status.hasShields() || status.capacity() <= 0) {
            return 0.0F;
        }

        return clamp01(
                (float) (status.currentEnergy() / status.capacity())
        );
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static final class VisualState {
        private float visualEnergy;

        private VisualState(float initialEnergy) {
            this.visualEnergy = initialEnergy;
        }

        private void update(float targetEnergy) {
            visualEnergy +=
                    (targetEnergy - visualEnergy) * VISUAL_RESPONSE;

            if (Math.abs(targetEnergy - visualEnergy) < 0.001F) {
                visualEnergy = targetEnergy;
            }

            visualEnergy = clamp01(visualEnergy);
        }

        private float visualEnergy() {
            return visualEnergy;
        }
    }
}
