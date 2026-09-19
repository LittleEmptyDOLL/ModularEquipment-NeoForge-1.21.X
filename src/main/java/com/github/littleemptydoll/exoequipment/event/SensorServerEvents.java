package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.module.SensorOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class SensorServerEvents {
    private static final int INTERVAL = 5;
    private static final byte GLOW_BIT = 1 << 6;

    private static final Map<UUID, Set<Integer>> HIGHLIGHTED = new HashMap<>();

    private SensorServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % INTERVAL != 0) {
            return;
        }

        Set<Integer> previous =
                HIGHLIGHTED.getOrDefault(player.getUUID(), Set.of());

        Set<Integer> current = new HashSet<>();

        Optional<ItemStack> stack = findExoskeleton(player);

        if (stack.isPresent()) {
            ExoskeletonRuntimeState runtime =
                    stack.get().get(ModDataComponents.EXOSKELETON_RUNTIME.get());

            if (runtime == null) {
                runtime = ExoskeletonRuntimeState.empty();
            }

            SensorOperations.detectEntities(
                    player,
                    ExoskeletonItem.getData(stack.get()),
                    runtime.poweredModules()
            ).forEach(detected -> {
                Entity entity = detected.entity();
                current.add(entity.getId());
                sendGlow(player, entity, true);
            });
        }

        for (int entityId : previous) {
            if (current.contains(entityId)) {
                continue;
            }

            Entity entity = player.level().getEntity(entityId);

            if (entity != null) {
                sendGlow(player, entity, false);
            }
        }

        if (current.isEmpty()) {
            HIGHLIGHTED.remove(player.getUUID());
        } else {
            HIGHLIGHTED.put(player.getUUID(), current);
        }
    }

    private static void sendGlow(
            ServerPlayer player,
            Entity target,
            boolean glow
    ) {
        byte flags = getSharedFlags(target);

        if (glow) {
            flags |= GLOW_BIT;
        } else {
            flags &= ~GLOW_BIT;
        }

        var data = new SynchedEntityData.DataValue<>(
                0,
                EntityDataSerializers.BYTE,
                flags
        );

        player.connection.send(
                new ClientboundSetEntityDataPacket(
                        target.getId(),
                        List.of(data)
                )
        );
    }

    private static byte getSharedFlags(Entity entity) {
        List<SynchedEntityData.DataValue<?>> values =
                entity.getEntityData().getNonDefaultValues();

        if (values != null) {
            for (var value : values) {
                if (value.id() == 0) {
                    return (Byte) value.value();
                }
            }
        }

        return 0;
    }

    private static Optional<ItemStack> findExoskeleton(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(curios ->
                        curios.findFirstCurio(
                                stack -> stack.getItem() instanceof ExoskeletonItem
                        )
                )
                .map(result -> result.stack());
    }
}
