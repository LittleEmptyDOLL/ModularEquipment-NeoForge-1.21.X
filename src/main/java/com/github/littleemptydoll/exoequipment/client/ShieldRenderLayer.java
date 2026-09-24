package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class ShieldRenderLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final int COLOR = 0x6638E8FF;
    private static final int GLINT_COLOR = 0xFFFFFFFF;

    private static final float DEFORMATION = 0.045F;
    private static final float GLINT_DEFORMATION = 0.065F;

    private static final float ACTIVATION_DURATION = 8.0F;
    private static final float DISCHARGE_DURATION = 7.0F;

    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/misc/white.png"
            );

    private final PlayerModel<AbstractClientPlayer> shieldModel;
    private final PlayerModel<AbstractClientPlayer> glintModel;

    private final Map<AbstractClientPlayer, VisualState> visualStates =
            new WeakHashMap<>();

    public ShieldRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            boolean slim
    ) {
        super(parent);
        this.shieldModel = createModel(slim, DEFORMATION);
        this.glintModel = createModel(slim, GLINT_DEFORMATION);
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

        Optional<ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(player)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack -> stack.getItem() instanceof ExoskeletonItem
                                )
                        )
                        .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            visualStates.remove(player);
            return;
        }

        ExoskeletonData data =
                ExoskeletonItem.getData(exoskeletonStack.get());

        ShieldOperations.ShieldStatus status =
                ShieldOperations.getStatus(data);

        double currentEnergy = status.currentEnergy();

        VisualState state =
                visualStates.computeIfAbsent(
                        player,
                        ignored -> new VisualState(currentEnergy, ageInTicks)
                );

        state.update(currentEnergy, ageInTicks);

        float alpha = state.getAlpha(currentEnergy, ageInTicks);

        if (alpha <= 0.0F) {
            return;
        }

        PlayerModel<AbstractClientPlayer> sourceModel = getParentModel();

        prepareModel(sourceModel, shieldModel);
        prepareModel(sourceModel, glintModel);

        renderShield(
                poseStack,
                bufferSource,
                shieldModel,
                alpha
        );

        if (state.shouldRenderGlint(currentEnergy, ageInTicks)) {
            renderGlint(
                    poseStack,
                    bufferSource,
                    glintModel,
                    alpha
            );
        }
    }

    private static void prepareModel(
            PlayerModel<AbstractClientPlayer> source,
            PlayerModel<AbstractClientPlayer> target
    ) {
        source.copyPropertiesTo(target);
        copyModelParts(source, target);
        copyVisibility(source, target);
    }

    private static void renderShield(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            PlayerModel<AbstractClientPlayer> model,
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
                withAlpha(COLOR, alpha)
        );
    }

    private static void renderGlint(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            PlayerModel<AbstractClientPlayer> model,
            float alpha
    ) {
        VertexConsumer buffer =
                bufferSource.getBuffer(RenderType.armorEntityGlint());

        model.renderToBuffer(
                poseStack,
                buffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                withAlpha(GLINT_COLOR, alpha)
        );
    }

    private static int withAlpha(int color, float alpha) {
        int clampedAlpha = Math.round(
                ((color >>> 24) & 0xFF)
                        * Math.max(0.0F, Math.min(1.0F, alpha))
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

    private static void copyModelPart(ModelPart source, ModelPart target) {
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

    private static final class VisualState {
        private double previousEnergy;
        private float activationStart;
        private float dischargeStart = Float.NEGATIVE_INFINITY;

        private VisualState(double initialEnergy, float ageInTicks) {
            this.previousEnergy = initialEnergy;
            this.activationStart =
                    initialEnergy > 0.0D
                            ? ageInTicks - ACTIVATION_DURATION
                            : Float.NEGATIVE_INFINITY;
        }

        private void update(double currentEnergy, float ageInTicks) {
            boolean wasActive = previousEnergy > 0.0D;
            boolean isActive = currentEnergy > 0.0D;

            if (!wasActive && isActive) {
                activationStart = ageInTicks;
                dischargeStart = Float.NEGATIVE_INFINITY;
            } else if (wasActive && !isActive) {
                dischargeStart = ageInTicks;
            }

            previousEnergy = currentEnergy;
        }

        private float getAlpha(double currentEnergy, float ageInTicks) {
            if (currentEnergy > 0.0D) {
                float activationProgress =
                        (ageInTicks - activationStart) / ACTIVATION_DURATION;

                if (activationProgress < 1.0F) {
                    float t = clamp01(activationProgress);
                    return smoothStep(t);
                }

                return 1.0F;
            }

            if (dischargeStart != Float.NEGATIVE_INFINITY) {
                float dischargeProgress =
                        (ageInTicks - dischargeStart) / DISCHARGE_DURATION;

                if (dischargeProgress >= 0.0F
                        && dischargeProgress < 1.0F) {
                    float t = clamp01(dischargeProgress);
                    return 1.0F - smoothStep(t);
                }
            }

            return 0.0F;
        }

        private boolean shouldRenderGlint(
                double currentEnergy,
                float ageInTicks
        ) {
            if (currentEnergy > 0.0D) {
                return true;
            }

            if (dischargeStart == Float.NEGATIVE_INFINITY) {
                return false;
            }

            float dischargeProgress =
                    (ageInTicks - dischargeStart) / DISCHARGE_DURATION;

            return dischargeProgress >= 0.0F
                    && dischargeProgress < 0.45F;
        }

        private static float clamp01(float value) {
            return Math.max(0.0F, Math.min(1.0F, value));
        }

        private static float smoothStep(float value) {
            return value * value * (3.0F - 2.0F * value);
        }
    }
}
