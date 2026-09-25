package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.BlockScannerOperations;
import com.github.littleemptydoll.exoequipment.network.BlockScannerPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class BlockScannerServerEvents {
    private static final int INTERVAL = 5;

    private BlockScannerServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % INTERVAL != 0) {
            return;
        }

        List<BlockPos> positions =
                ExoskeletonAccess.findContext(player)
                        .map(context ->
                                BlockScannerOperations.scanBlocks(
                                        player.level(),
                                        player.blockPosition(),
                                        context.data(),
                                        context.poweredModules()
                                )
                        )
                        .orElseGet(List::of);

        PacketDistributor.sendToPlayer(
                player,
                new BlockScannerPayload(positions)
        );
    }
}
