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

    private static final float DEFORMATION = 0.045F;
    private static final float GLINT_DEFORMATION = 0.065F;

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

    private static final ResourceLocation GLINT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/misc/enchanted_glint_entity.png"
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
        prepareModel(sourceModel, glintModel);

        float alpha = calculateAlpha(energy);

        renderShield(
                poseStack,
                bufferSource,
                shieldModel,
                alpha
        );

        renderGlint(
                poseStack,
                bufferSource,
                glintModel,
                alpha
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

    private static float calculateAlpha(float energy) {
        /*
         * Keep the shield readable even when partially depleted while still
         * making a nearly empty shield fade naturally.
         */
        return 0.12F + 0.58F * smoothStep(energy);
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
        /*
         * armorEntityGlint() did not produce a visible result on this custom
         * geometry. The energy_swirl pipeline already animates its texture
         * using Minecraft's render-time shader animation. Supplying a
         * changing per-frame offset here would restart/rephase that
         * animation every render pass, which makes the glint appear to
         * stutter or reset.
         */
        VertexConsumer buffer =
                bufferSource.getBuffer(
                        RenderType.energySwirl(
                                GLINT_TEXTURE,
                                0.0F,
                                0.0F
                        )
                );

        model.renderToBuffer(
                poseStack,
                buffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                withAlpha(0xCCFFFFFF, alpha)
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
