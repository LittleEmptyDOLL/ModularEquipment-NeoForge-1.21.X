package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
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
        copyVisibility(sourceModel, shieldModel);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();

        poseStack.pushPose();

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
