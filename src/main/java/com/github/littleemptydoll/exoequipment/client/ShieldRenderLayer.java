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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Player shield render layer.
 *
 * The layer no longer submits shield draw calls immediately. During the normal
 * entity pass it captures the player's current shield pose into
 * ShieldDeferredRenderer; the field is actually drawn after every normal
 * entity has rendered so translucent blending has the correct entity order.
 *
 * Shield charge no longer controls the radial size of the field. A loss of
 * charge is instead converted into short-lived local damage impulses which the
 * fragment shader uses to break individual cells of the hex lattice.
 */
public final class ShieldRenderLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float VISUAL_RESPONSE = 0.35F;
    private static final float MIN_VISIBLE_ENERGY = 0.01F;
    private static final float DAMAGE_LIFETIME_TICKS = 52.0F;
    private static final float DAMAGE_HOLD_TICKS = 5.0F;
    private static final int MAX_DAMAGE_IMPACTS = 4;

    /*
     * ShieldFieldMesh renders parts in this order:
     *   0 head, 1 body, 2 right arm, 3 left arm, 4 right leg, 5 left leg.
     * Weight the larger torso/head surfaces slightly more heavily than each
     * individual limb while still allowing damage anywhere on the field.
     */
    private static final int[] DAMAGE_PART_WEIGHTS = {
            0, 0,
            1, 1, 1,
            2, 3,
            4, 5
    };

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
        if (!ShieldDeferredRenderer.isCollecting()) {
            return;
        }

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
        float time;
        if (Minecraft.getInstance().level != null) {
            time = Minecraft.getInstance().level.getGameTime() + partialTick;
        } else {
            time = player.tickCount + partialTick;
        }

        VisualState state = visualStates.computeIfAbsent(
                player,
                ignored -> new VisualState(targetEnergy)
        );

        state.update(
                targetEnergy,
                time,
                player.getUUID().hashCode()
        );
        float energy = state.visualEnergy();

        if (!status.hasShields() && energy <= MIN_VISIBLE_ENERGY) {
            visualStates.remove(player);
            return;
        }

        if (energy <= MIN_VISIBLE_ENERGY || ShieldFieldShader.getShader() == null) {
            return;
        }

        ShieldDeferredRenderer.enqueue(
                poseStack,
                getParentModel(),
                time,
                fieldOpacity(energy),
                state.damageImpacts(time)
        );
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

    private static float fieldOpacity(float energy) {
        // Keep a charged shield full-sized and visually solid. Only the final
        // few percent are used as a short fade when the field actually breaks.
        float t = clamp01(energy / 0.08F);
        return t * t * (3.0F - (2.0F * t));
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static final class VisualState {
        private final List<ImpactState> impacts = new ArrayList<>();
        private float visualEnergy;
        private float lastTargetEnergy;
        private int impactSerial;

        private VisualState(float initialEnergy) {
            this.visualEnergy = initialEnergy;
            this.lastTargetEnergy = initialEnergy;
        }

        private void update(
                float targetEnergy,
                float time,
                int playerSeed
        ) {
            impacts.removeIf(impact ->
                    time - impact.startTime() >= DAMAGE_LIFETIME_TICKS
            );

            float lostEnergy = lastTargetEnergy - targetEnergy;
            if (lostEnergy > 0.000001F) {
                spawnImpact(lostEnergy, time, playerSeed);
            }
            lastTargetEnergy = targetEnergy;

            visualEnergy +=
                    (targetEnergy - visualEnergy) * VISUAL_RESPONSE;

            if (Math.abs(targetEnergy - visualEnergy) < 0.001F) {
                visualEnergy = targetEnergy;
            }

            visualEnergy = clamp01(visualEnergy);
        }

        private void spawnImpact(
                float lostEnergy,
                float time,
                int playerSeed
        ) {
            int serial = ++impactSerial;
            int seed = mixSeed(
                    playerSeed
                            ^ Integer.rotateLeft(serial * 0x9E3779B9, serial & 15)
                            ^ Float.floatToIntBits(lastTargetEnergy)
            );

            int weightedPart = Math.floorMod(seed, DAMAGE_PART_WEIGHTS.length);
            int partIndex = DAMAGE_PART_WEIGHTS[weightedPart];

            // Radius is measured in hex-grid cells. Small hits remove a tight
            // cluster; large hits can punch out several neighbouring cells.
            float severity = clamp01(lostEnergy * 8.0F);
            float radius = 1.35F + (1.65F * (float) Math.sqrt(severity));

            if (impacts.size() >= MAX_DAMAGE_IMPACTS) {
                impacts.remove(0);
            }

            impacts.add(new ImpactState(
                    partIndex,
                    seed,
                    radius,
                    time
            ));
        }

        private List<ShieldDeferredRenderer.DamageImpact> damageImpacts(
                float time
        ) {
            if (impacts.isEmpty()) {
                return List.of();
            }

            List<ShieldDeferredRenderer.DamageImpact> result =
                    new ArrayList<>(impacts.size());

            for (ImpactState impact : impacts) {
                float age = time - impact.startTime();
                float repairProgress = clamp01(
                        (age - DAMAGE_HOLD_TICKS)
                                / (DAMAGE_LIFETIME_TICKS - DAMAGE_HOLD_TICKS)
                );
                repairProgress = repairProgress * repairProgress
                        * (3.0F - (2.0F * repairProgress));
                float strength = 1.0F - repairProgress;

                if (strength > 0.001F) {
                    result.add(new ShieldDeferredRenderer.DamageImpact(
                            impact.partIndex(),
                            impact.seed(),
                            impact.radius(),
                            strength
                    ));
                }
            }

            return List.copyOf(result);
        }

        private float visualEnergy() {
            return visualEnergy;
        }

        private static int mixSeed(int value) {
            value ^= value >>> 16;
            value *= 0x7FEB352D;
            value ^= value >>> 15;
            value *= 0x846CA68B;
            value ^= value >>> 16;
            return value;
        }
    }

    private record ImpactState(
            int partIndex,
            int seed,
            float radius,
            float startTime
    ) {}
}
