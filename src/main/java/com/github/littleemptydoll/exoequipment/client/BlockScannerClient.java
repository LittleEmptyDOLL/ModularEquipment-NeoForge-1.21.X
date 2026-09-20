package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
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

        var consumer = bufferSource.getBuffer(RenderType.lines());

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
