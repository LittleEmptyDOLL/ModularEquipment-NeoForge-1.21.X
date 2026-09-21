package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(
        modid = ExoEquipment.MODID,
        value = Dist.CLIENT
)
public final class BlockScannerClient {
    private static final RenderType SEE_THROUGH_LINES = RenderType.create(
            "exoequipment:block_scanner",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(RenderStateShard.DEFAULT_LINE)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLayeringState(RenderStateShard.NO_LAYERING)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setOutputState(RenderStateShard.MAIN_TARGET)
                    .createCompositeState(false)
    );

    private static volatile List<BlockPos> positions = List.of();

    private BlockScannerClient() {}

    public static void setPositions(List<BlockPos> newPositions) {
        positions = List.copyOf(newPositions);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (positions.isEmpty() || event.getPoseStack() == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        var cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource =
                minecraft.renderBuffers().bufferSource();

        var consumer = bufferSource.getBuffer(SEE_THROUGH_LINES);

        event.getPoseStack().pushPose();
        event.getPoseStack().translate(
                -cameraPos.x,
                -cameraPos.y,
                -cameraPos.z
        );

        for (BlockPos pos : positions) {
            LevelRenderer.renderLineBox(
                    event.getPoseStack(),
                    consumer,
                    new AABB(pos).inflate(0.002D),
                    0.1F,
                    0.9F,
                    1.0F,
                    1.0F
            );
        }

        event.getPoseStack().popPose();
    }
}
