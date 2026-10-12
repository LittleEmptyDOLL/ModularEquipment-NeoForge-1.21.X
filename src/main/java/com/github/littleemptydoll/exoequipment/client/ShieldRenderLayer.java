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

    private static final float SHELL_DEFORMATION = 0.12F;
    private static final float OUTER_DEFORMATION = 0.155F;

    private static final float VISUAL_RESPONSE = 0.35F;
    private static final float MIN_VISIBLE_ENERGY = 0.01F;

    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/misc/white.png"
            );

    private final PlayerModel<AbstractClientPlayer> shieldModel;
    private final PlayerModel<AbstractClientPlayer> outerModel;

    private final Map<AbstractClientPlayer, VisualState> visualStates =
            new WeakHashMap<>();

    public ShieldRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            boolean slim
    ) {
        super(parent);
        this.shieldModel = createModel(slim, SHELL_DEFORMATION);
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

        if (!status.hasShields() && energy <= MIN_VISIBLE_ENERGY) {
            visualStates.remove(player);
            return;
        }

        if (energy <= MIN_VISIBLE_ENERGY) {
            return;
        }

        PlayerModel<AbstractClientPlayer> sourceModel = getParentModel();

        prepareModel(sourceModel, shieldModel);
        prepareModel(sourceModel, outerModel);

        float time = player.tickCount + partialTick;
        float pulse = 0.96F + 0.04F * (float) Math.sin(time * 0.22F);

        float shellAlpha = calculateShellAlpha(energy) * pulse;
        float outerAlpha = calculateOuterAlpha(energy)
                * (1.0F + (1.0F - pulse) * 1.5F);

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

        renderProceduralField(
                poseStack,
                bufferSource,
                sourceModel,
                energy,
                time
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
        return 0.045F + 0.23F * smoothStep(energy);
    }

    private static float calculateOuterAlpha(float energy) {
        return 0.010F + 0.035F * smoothStep(energy);
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

    private static void renderProceduralField(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            PlayerModel<AbstractClientPlayer> sourceModel,
            float energy,
            float time
    ) {
        if (ShieldFieldShader.getShader() == null) {
            return;
        }

        RenderType fieldType = ShieldFieldShader.fieldType(time, energy);
        VertexConsumer buffer = bufferSource.getBuffer(fieldType);

        /*
         * Do not render the procedural field through PlayerModel skin cubes.
         * Draconic Evolution uses six dedicated shield meshes with their own UVs
         * and applies the matching humanoid part transform to each one. The
         * dedicated ShieldFieldMesh mirrors that architecture without requiring
         * CodeChickenLib/VBO wrappers.
         */
        ShieldFieldMesh.render(poseStack, buffer, sourceModel);

        /*
         * Time/Activation are per-player shader values. Flush this RenderType
         * immediately, matching the effective lifetime of DE's per-part VBO
         * callback values and preventing another player from replacing them.
         */
        if (bufferSource instanceof MultiBufferSource.BufferSource immediate) {
            immediate.endBatch(fieldType);
        }
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
        target.body.visible = source.body.visible;
        target.rightArm.visible = source.rightArm.visible;
        target.leftArm.visible = source.leftArm.visible;
        target.rightLeg.visible = source.rightLeg.visible;
        target.leftLeg.visible = source.leftLeg.visible;

        target.hat.visible = false;
        target.jacket.visible = false;
        target.rightSleeve.visible = false;
        target.leftSleeve.visible = false;
        target.rightPants.visible = false;
        target.leftPants.visible = false;
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
