package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.WeakHashMap;

public final class ShieldRenderLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final int SHELL_COLOR = 0x6638E8FF;
    private static final int OUTER_COLOR = 0xFF68F2FF;
    private static final int PATTERN_COLOR_PRIMARY = 0xFFD7FCFF;
    private static final int PATTERN_COLOR_SECONDARY = 0xFF56DFFF;

    private static final float SHELL_DEFORMATION = 0.12F;
    private static final float PATTERN_DEFORMATION_PRIMARY = 0.135F;
    private static final float PATTERN_DEFORMATION_SECONDARY = 0.142F;
    private static final float OUTER_DEFORMATION = 0.155F;

    /*
     * Shield data is synchronized through the Curios ItemStack and can arrive
     * on the client in discrete updates. We therefore do not treat every
     * 0 <-> 0 transition as a visual state transition. Instead, the renderer
     * interpolates a client-side visual energy value toward the latest
     * synchronized value. This prevents flicker when the server changes or
     * recharges the shield.
     */
    private static final float VISUAL_RESPONSE = 0.35F;
    private static final float MIN_VISIBLE_ENERGY = 0.01F;

    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/misc/white.png"
            );

    private static final ResourceLocation ENERGY_PATTERN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "exoequipment",
                    "textures/entity/shield_energy_pattern.png"
            );

    private final PlayerModel<AbstractClientPlayer> shieldModel;
    private final PlayerModel<AbstractClientPlayer> primaryPatternModel;
    private final PlayerModel<AbstractClientPlayer> secondaryPatternModel;
    private final PlayerModel<AbstractClientPlayer> outerModel;

    private final Map<AbstractClientPlayer, VisualState> visualStates =
            new WeakHashMap<>();

    public ShieldRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            boolean slim
    ) {
        super(parent);
        this.shieldModel = createModel(slim, SHELL_DEFORMATION);
        this.primaryPatternModel = createModel(slim, PATTERN_DEFORMATION_PRIMARY);
        this.secondaryPatternModel = createModel(slim, PATTERN_DEFORMATION_SECONDARY);
        this.outerModel = createModel(slim, OUTER_DEFORMATION);
    }

    private static PlayerModel<AbstractClientPlayer> createModel(
            boolean slim,
            float deformation
    ) {
        LayerDefinition layer = LayerDefinition.create(
                PlayerModel.createMesh(
                        new CubeDeformation(deformation),
                        slim
                ),
                64,
                64
        );

        return new PlayerModel<>(
                layer.bakeRoot(),
                slim
        );
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            float partialTick
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

        VisualState state =
                visualStates.computeIfAbsent(
                        player,
                        ignored -> new VisualState(targetEnergy)
                );

        state.update(targetEnergy);

        float energy = state.visualEnergy();

        /*
         * Once the client has received that the shield module itself is gone,
         * there is no reason to keep the old visual state around.
         */
        if (!status.hasShields() && energy <= MIN_VISIBLE_ENERGY) {
            visualStates.remove(player);
            return;
        }

        if (energy <= MIN_VISIBLE_ENERGY) {
            return;
        }

        PlayerModel<AbstractClientPlayer> sourceModel = getParentModel();

        prepareModel(sourceModel, shieldModel);
        prepareModel(sourceModel, primaryPatternModel);
        prepareModel(sourceModel, secondaryPatternModel);
        prepareModel(sourceModel, outerModel);

        float time = player.tickCount + partialTick;
        float pulse = 0.96F + 0.04F * (float) Math.sin(time * 0.22F);

        float shellAlpha = calculateShellAlpha(energy) * pulse;
        float outerAlpha = calculateOuterAlpha(energy)
                * (1.0F + (1.0F - pulse) * 1.5F);
        float patternAlpha = calculatePatternAlpha(energy);

        renderSolidShell(
                poseStack,
                bufferSource,
                outerModel,
                OUTER_COLOR,
                outerAlpha
        );

        renderSolidShell(
                poseStack,
                bufferSource,
                shieldModel,
                SHELL_COLOR,
                shellAlpha
        );

        renderEnergyPattern(
                poseStack,
                bufferSource,
                primaryPatternModel,
                PATTERN_COLOR_PRIMARY,
                patternAlpha,
                wrap01(-time * 0.0060F),
                wrap01(time * 0.0035F)
        );

        renderEnergyPattern(
                poseStack,
                bufferSource,
                secondaryPatternModel,
                PATTERN_COLOR_SECONDARY,
                patternAlpha * 0.60F,
                wrap01(time * 0.0040F),
                wrap01(-time * 0.0050F)
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

    private static float calculateShellAlpha(float energy) {
        /*
         * Keep a readable cyan body at high energy while allowing the shell to
         * become very faint near depletion.
         */
        return 0.06F + 0.28F * smoothStep(energy);
    }

    private static float calculateOuterAlpha(float energy) {
        /*
         * The outer layer is deliberately very subtle. It softens the hard
         * cube boundary of the player-shaped shell without looking like a
         * second piece of armor.
         */
        return 0.015F + 0.045F * smoothStep(energy);
    }

    private static float calculatePatternAlpha(float energy) {
        /*
         * The custom texture has its own transparency, so the animated lattice
         * can stay more visible than the solid shell while still fading with
         * shield energy.
         */
        return 0.16F + 0.46F * smoothStep(energy);
    }

    private static void prepareModel(
            PlayerModel<AbstractClientPlayer> source,
            PlayerModel<AbstractClientPlayer> target
    ) {
        source.copyPropertiesTo(target);
        copyModelParts(source, target);
        copyVisibility(source, target);
    }

    private static void renderSolidShell(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            PlayerModel<AbstractClientPlayer> model,
            int color,
            float alpha
    ) {
        VertexConsumer buffer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucentEmissive(WHITE_TEXTURE)
                );

        model.renderToBuffer(
                poseStack,
                buffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                withAlpha(color, alpha)
        );
    }

    private static void renderEnergyPattern(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            PlayerModel<AbstractClientPlayer> model,
            int color,
            float alpha,
            float u,
            float v
    ) {
        VertexConsumer buffer =
                bufferSource.getBuffer(
                        RenderType.energySwirl(
                                ENERGY_PATTERN_TEXTURE,
                                u,
                                v
                        )
                );

        model.renderToBuffer(
                poseStack,
                buffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                withAlpha(color, alpha)
        );
    }

    private static int withAlpha(int color, float alpha) {
        int clampedAlpha = Math.round(
                ((color >>> 24) & 0xFF)
                        * clamp01(alpha)
        );

        return (color & 0x00FFFFFF) | (clampedAlpha << 24);
    }

    private static void copyModelParts(
            PlayerModel<AbstractClientPlayer> source,
            PlayerModel<AbstractClientPlayer> target
    ) {
        copyModelPart(source.head, target.head);
        copyModelPart(source.hat, target.hat);
        copyModelPart(source.body, target.body);
        copyModelPart(source.rightArm, target.rightArm);
        copyModelPart(source.leftArm, target.leftArm);
        copyModelPart(source.rightLeg, target.rightLeg);
        copyModelPart(source.leftLeg, target.leftLeg);

        copyModelPart(source.jacket, target.jacket);
        copyModelPart(source.rightSleeve, target.rightSleeve);
        copyModelPart(source.leftSleeve, target.leftSleeve);
        copyModelPart(source.rightPants, target.rightPants);
        copyModelPart(source.leftPants, target.leftPants);
    }

    private static void copyModelPart(
            ModelPart source,
            ModelPart target
    ) {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;

        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;

        target.xScale = source.xScale;
        target.yScale = source.yScale;
        target.zScale = source.zScale;

        target.skipDraw = source.skipDraw;
    }

    private static void copyVisibility(
            PlayerModel<AbstractClientPlayer> source,
            PlayerModel<AbstractClientPlayer> target
    ) {
        target.head.visible = source.head.visible;
        target.hat.visible = source.hat.visible;
        target.body.visible = source.body.visible;
        target.rightArm.visible = source.rightArm.visible;
        target.leftArm.visible = source.leftArm.visible;
        target.rightLeg.visible = source.rightLeg.visible;
        target.leftLeg.visible = source.leftLeg.visible;

        target.jacket.visible = source.jacket.visible;
        target.rightSleeve.visible = source.rightSleeve.visible;
        target.leftSleeve.visible = source.leftSleeve.visible;
        target.rightPants.visible = source.rightPants.visible;
        target.leftPants.visible = source.leftPants.visible;
    }

    private static float wrap01(float value) {
        return value - (float) Math.floor(value);
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
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
