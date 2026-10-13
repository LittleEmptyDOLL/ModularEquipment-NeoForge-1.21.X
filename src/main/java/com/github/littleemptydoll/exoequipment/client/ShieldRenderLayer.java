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
 * Shield charge no longer controls the radial size of the field. Server-side
 * shield-hit events create short-lived local damage impulses which the fragment
 * shader uses to break individual cells of the hex lattice.
 */
public final class ShieldRenderLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float VISUAL_RESPONSE = 0.35F;
    private static final float MIN_VISIBLE_ENERGY = 0.01F;
    private static final float DAMAGE_LIFETIME_TICKS = 52.0F;
    private static final float BREAK_DURATION_TICKS = 16.0F;
    private static final float DAMAGE_HOLD_TICKS = 5.0F;
    private static final int MAX_DAMAGE_IMPACTS = 4;

    /*
     * Fallback weights used only for damage sources without spatial context.
     * ShieldFieldMesh renders parts in this order:
     *   0 head, 1 body, 2 right arm, 3 left arm, 4 right leg, 5 left leg.
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

        for (ShieldHitClientState.Hit hit
                : ShieldHitClientState.drain(player.getId())) {
            state.spawnImpact(
                    hit,
                    time,
                    player.getUUID().hashCode()
            );
        }

        state.update(targetEnergy, time);
        float energy = state.visualEnergy();

        if (!status.hasShields() && energy <= MIN_VISIBLE_ENERGY) {
            visualStates.remove(player);
            return;
        }

        if ((energy <= MIN_VISIBLE_ENERGY && !state.isBreaking(time))
                || ShieldFieldShader.getShader() == null) {
            return;
        }

        ShieldDeferredRenderer.enqueue(
                poseStack,
                getParentModel(),
                time,
                state.isBreaking(time) ? 1.0F : fieldOpacity(energy),
                state.breakProgress(time),
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
        private float breakStart = -10000.0F;
        private float lastTargetEnergy;

        private VisualState(float initialEnergy) {
            this.visualEnergy = initialEnergy;
            this.lastTargetEnergy = initialEnergy;
        }

        private void update(
                float targetEnergy,
                float time
        ) {
            // Trigger once on depletion, not continuously while empty. Preserve a
            // short draw window even after energy reaches zero.
            if (lastTargetEnergy > MIN_VISIBLE_ENERGY
                    && targetEnergy <= MIN_VISIBLE_ENERGY) {
                breakStart = time;
            }
            lastTargetEnergy = targetEnergy;
            impacts.removeIf(impact ->
                    time - impact.startTime() >= DAMAGE_LIFETIME_TICKS
            );

            visualEnergy +=
                    (targetEnergy - visualEnergy) * VISUAL_RESPONSE;

            if (Math.abs(targetEnergy - visualEnergy) < 0.001F) {
                visualEnergy = targetEnergy;
            }

            visualEnergy = clamp01(visualEnergy);
        }

        private void spawnImpact(
                ShieldHitClientState.Hit hit,
                float time,
                int playerSeed
        ) {
            int seed = mixSeed(hit.seed() ^ playerSeed);
            int partIndex = hit.partIndex();

            if (partIndex < 0 || partIndex > 5) {
                int weightedPart = Math.floorMod(
                        seed,
                        DAMAGE_PART_WEIGHTS.length
                );
                partIndex = DAMAGE_PART_WEIGHTS[weightedPart];
            }

            // Radius is measured in hex-grid cells. Preserve the scale of the
            // original energy-delta effect, but drive it from the exact amount
            // absorbed by the shield on the server.
            float severity = clamp01(hit.energyLoss() * 8.0F);
            float radius = 1.35F + (1.65F * (float) Math.sqrt(severity));

            float directionX = hit.directionX();
            float directionZ = hit.directionZ();
            float directionLength = (float) Math.sqrt(
                    (directionX * directionX)
                            + (directionZ * directionZ)
            );
            if (directionLength > 0.0001F) {
                directionX /= directionLength;
                directionZ /= directionLength;
            } else {
                directionX = 0.0F;
                directionZ = 0.0F;
            }

            if (impacts.size() >= MAX_DAMAGE_IMPACTS) {
                impacts.remove(0);
            }

            impacts.add(new ImpactState(
                    partIndex,
                    seed,
                    radius,
                    directionX,
                    directionZ,
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
                            strength,
                            impact.directionX(),
                            impact.directionZ()
                    ));
                }
            }

            return List.copyOf(result);
        }

        private boolean isBreaking(float time) {
            return time >= breakStart && time - breakStart < BREAK_DURATION_TICKS
                    && lastTargetEnergy <= MIN_VISIBLE_ENERGY;
        }

        private float breakProgress(float time) {
            return isBreaking(time)
                    ? clamp01((time - breakStart) / BREAK_DURATION_TICKS)
                    : 0.0F;
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
            float directionX,
            float directionZ,
            float startTime
    ) {}
}
