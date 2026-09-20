package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.BlockScannerOperations;
import com.github.littleemptydoll.exoequipment.network.BlockScannerPayload;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Optional;

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

        List<BlockPos> positions = List.of();

        Optional<ItemStack> stack = CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.findFirstCurio(
                        item -> item.getItem() instanceof ExoskeletonItem
                ))
                .map(result -> result.stack());

        if (stack.isPresent()) {
            ExoskeletonRuntimeState runtime =
                    stack.get().get(ModDataComponents.EXOSKELETON_RUNTIME.get());

            if (runtime == null) {
                runtime = ExoskeletonRuntimeState.empty();
            }

            positions = BlockScannerOperations.scanBlocks(
                    player.level(),
                    player.blockPosition(),
                    ExoskeletonItem.getData(stack.get()),
                    runtime.poweredModules()
            );
        }

        PacketDistributor.sendToPlayer(
                player,
                new BlockScannerPayload(positions)
        );
    }
}
