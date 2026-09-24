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
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public final class ShieldRenderLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final int COLOR = 0x6638E8FF;
    private static final float DEFORMATION = 0.045F;
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/misc/white.png"
            );

    private final PlayerModel<AbstractClientPlayer> shieldModel;

    public ShieldRenderLayer(
            RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
            boolean slim
    ) {
        super(parent);
        this.shieldModel = createModel(slim);
    }

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

        Optional<net.minecraft.world.item.ItemStack> exoskeletonStack =
                CuriosApi.getCuriosInventory(player)
                        .flatMap(curios ->
                                curios.findFirstCurio(
                                        stack -> stack.getItem()
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

        PlayerModel<AbstractClientPlayer> sourceModel = getParentModel();

        sourceModel.copyPropertiesTo(shieldModel);
        copyModelParts(sourceModel, shieldModel);
        copyVisibility(sourceModel, shieldModel);

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
