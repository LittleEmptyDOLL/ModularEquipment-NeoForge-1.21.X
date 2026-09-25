package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.module.BlockScannerOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.network.BlockScannerPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class BlockScannerServerEvents {
    private static final int INTERVAL = 5;

    private static final Map<UUID, ScanCache> CACHE =
            new HashMap<>();

    private BlockScannerServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % INTERVAL != 0) {
            return;
        }

        var context =
                ExoskeletonAccess.findContext(player)
                        .orElse(null);

        if (context == null) {
            clear(player);
            return;
        }

        ScanKey key =
                new ScanKey(
                        player.level()
                                .dimension()
                                .location(),
                        player.blockPosition(),
                        context.data(),
                        context.poweredModules()
                );

        ScanCache cached =
                CACHE.get(player.getUUID());

        if (cached != null
                && cached.key().equals(key)) {
            return;
        }

        List<BlockPos> positions =
                BlockScannerOperations.scanBlocks(
                        player.level(),
                        player.blockPosition(),
                        context.data(),
                        context.poweredModules()
                );

        if (cached == null
                || !cached.positions().equals(positions)) {
            PacketDistributor.sendToPlayer(
                    player,
                    new BlockScannerPayload(positions)
            );
        }

        CACHE.put(
                player.getUUID(),
                new ScanCache(
                        key,
                        positions
                )
        );
    }

    private static void clear(
            ServerPlayer player
    ) {
        ScanCache cached =
                CACHE.remove(player.getUUID());

        if (cached == null
                || cached.positions().isEmpty()) {
            return;
        }

        PacketDistributor.sendToPlayer(
                player,
                new BlockScannerPayload(List.of())
        );
    }

    private record ScanKey(
            ResourceLocation dimension,
            BlockPos position,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        private ScanKey {
            position = position.immutable();
            poweredModules =
                    Set.copyOf(poweredModules);
        }
    }

    private record ScanCache(
            ScanKey key,
            List<BlockPos> positions
    ) {
        private ScanCache {
            positions = List.copyOf(positions);
        }
    }
}
