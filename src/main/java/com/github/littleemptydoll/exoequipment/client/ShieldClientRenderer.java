package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
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
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.Quaternionf;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class ShieldClientRenderer {
    private static final float DEFORMATION = 0.045F;
    private static final int COLOR = 0x6638E8FF;
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/misc/white.png"
            );

    private static final PlayerModel<AbstractClientPlayer> WIDE_MODEL =
            createModel(false);
    private static final PlayerModel<AbstractClientPlayer> SLIM_MODEL =
            createModel(true);

    private ShieldClientRenderer() {}

    private static PlayerModel<AbstractClientPlayer> createModel(boolean slim) {
        LayerDefinition layer = LayerDefinition.create(
                PlayerModel.createMesh(
                        new CubeDeformation(DEFORMATION),
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

    @SubscribeEvent
    public static void renderShield(RenderPlayerEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }

        if (player.isInvisible() || player.isSpectator()) {
            return;
        }

        Optional<net.minecraft.world.item.ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(player)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack ->
                                                stack.getItem()
                                                        instanceof ExoskeletonItem
                                )
                        )
                        .map(result -> result.stack());

        if (exoskeletonStack.isEmpty()) {
            return;
        }

        ExoskeletonData data =
                ExoskeletonItem.getData(exoskeletonStack.get());

        ShieldOperations.ShieldStatus status =
                ShieldOperations.getStatus(data);

        if (status.currentEnergy() <= 0.0D) {
            return;
        }

        PlayerRenderer renderer = event.getRenderer();

        @SuppressWarnings("unchecked")
        PlayerModel<AbstractClientPlayer> sourceModel =
                (PlayerModel<AbstractClientPlayer>) renderer.getModel();

        PlayerModel<AbstractClientPlayer> shieldModel =
                player.getSkin().model() == PlayerSkin.Model.SLIM
                        ? SLIM_MODEL
                        : WIDE_MODEL;

        sourceModel.copyPropertiesTo(shieldModel);
        copyModelParts(sourceModel, shieldModel);
        copyVisibility(sourceModel, shieldModel);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();

        poseStack.pushPose();

        applyPlayerRenderTransform(player, event.getPartialTick(), poseStack);

        VertexConsumer buffer =
                bufferSource.getBuffer(
                        RenderType.entityTranslucentEmissive(WHITE_TEXTURE)
                );

        shieldModel.renderToBuffer(
                poseStack,
                buffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                COLOR
        );

        poseStack.popPose();
    }

    private static void applyPlayerRenderTransform(
            AbstractClientPlayer player,
            float partialTick,
            PoseStack poseStack
    ) {
        float bodyRotation = Mth.rotLerp(
                partialTick,
                player.yBodyRotO,
                player.yBodyRot
        );

        if (player.getPose() == Pose.SLEEPING) {
            Direction direction = player.getBedOrientation();

            if (direction != null) {
                float eyeHeight = player.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate(
                        -direction.getStepX() * eyeHeight,
                        0.0D,
                        -direction.getStepZ() * eyeHeight
                );
                poseStack.mulPose(
                        new Quaternionf()
                                .rotateY((float) Math.toRadians(
                                        sleepDirectionToRotation(direction)
                                ))
                );
                poseStack.mulPose(
                        new Quaternionf()
                                .rotateZ((float) Math.toRadians(90.0F))
                );
            }
        } else {
            poseStack.mulPose(
                    new Quaternionf()
                            .rotateY((float) Math.toRadians(
                                    180.0F - bodyRotation
                            ))
            );

            if (player.isVisuallySwimming()) {
                float pitch = Mth.rotLerp(
                        partialTick,
                        player.xRotO,
                        player.getXRot()
                );

                // Swimming rotates the player around the body instead of the
                // feet/hitbox bottom. Move the model pivot to the body center,
                // apply the swimming rotation, then restore the pivot.
                poseStack.translate(0.0D, 0.75D, 0.0D);
                poseStack.mulPose(
                        new Quaternionf()
                                .rotateX((float) Math.toRadians(
                                        -90.0F - pitch
                                ))
                );
                poseStack.translate(0.0D, -0.75D, 0.0D);
            }
        }

        // PlayerRenderer scales the vanilla player model before rendering it.
        // The shield model must use the same scale, otherwise its pivots
        // (head, shoulders, hips) appear noticeably too high.
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -1.501D, 0.0D);
    }

    private static float sleepDirectionToRotation(Direction direction) {
        return switch (direction) {
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
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
}
