package com.github.littleemptydoll.exoequipment.event;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.SensorOperations;
import com.github.littleemptydoll.exoequipment.network.SensorHighlightPayload;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ExoEquipment.MODID)
public final class SensorServerEvents {
    private static final int INTERVAL = 5;
    private static final byte GLOW_BIT = 1 << 6;

    private static final Map<UUID, Set<Integer>> HIGHLIGHTED =
            new HashMap<>();

    private SensorServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % INTERVAL != 0) {
            return;
        }

        Set<Integer> previous =
                HIGHLIGHTED.getOrDefault(
                        player.getUUID(),
                        Set.of()
                );

        Set<Integer> current = new HashSet<>();
        List<Integer> hostile = new ArrayList<>();
        List<Integer> mobs = new ArrayList<>();
        List<Integer> players = new ArrayList<>();

        ExoskeletonAccess.findContext(player)
                .ifPresent(context ->
                        SensorOperations.detectEntities(
                                player,
                                context.data(),
                                context.poweredModules()
                        ).forEach(detected -> {
                            Entity entity = detected.entity();

                            current.add(entity.getId());
                            sendGlow(
                                    player,
                                    entity,
                                    true
                            );

                            if (entity instanceof Player) {
                                players.add(entity.getId());
                            } else if (entity instanceof Enemy) {
                                hostile.add(entity.getId());
                            } else if (entity instanceof Mob) {
                                mobs.add(entity.getId());
                            }
                        })
                );

        for (int entityId : previous) {
            if (current.contains(entityId)) {
                continue;
            }

            Entity entity =
                    player.level().getEntity(entityId);

            if (entity != null) {
                sendGlow(
                        player,
                        entity,
                        false
                );
            }
        }

        PacketDistributor.sendToPlayer(
                player,
                new SensorHighlightPayload(
                        hostile,
                        mobs,
                        players
                )
        );

        if (current.isEmpty()) {
            HIGHLIGHTED.remove(player.getUUID());
        } else {
            HIGHLIGHTED.put(
                    player.getUUID(),
                    current
            );
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
}
