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

    private static final Map<UUID, SensorState> STATES =
            new HashMap<>();

    private SensorServerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % INTERVAL != 0) {
            return;
        }

        SensorState previous =
                STATES.getOrDefault(
                        player.getUUID(),
                        SensorState.empty()
                );

        Set<Integer> current = new HashSet<>();
        Set<Integer> hostile = new HashSet<>();
        Set<Integer> mobs = new HashSet<>();
        Set<Integer> players = new HashSet<>();

        ExoskeletonAccess.findContext(player)
                .ifPresent(context ->
                        SensorOperations.detectEntities(
                                player,
                                context.data(),
                                context.poweredModules()
                        ).forEach(detected -> {
                            Entity entity = detected.entity();
                            int entityId = entity.getId();

                            current.add(entityId);

                            if (!previous.contains(entityId)) {
                                sendGlow(
                                        player,
                                        entity,
                                        true
                                );
                            }

                            if (entity instanceof Player) {
                                players.add(entityId);
                            } else if (entity instanceof Enemy) {
                                hostile.add(entityId);
                            } else if (entity instanceof Mob) {
                                mobs.add(entityId);
                            }
                        })
                );

        for (int entityId : previous.all()) {
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

        SensorState currentState =
                new SensorState(
                        hostile,
                        mobs,
                        players
                );

        if (!currentState.equals(previous)) {
            PacketDistributor.sendToPlayer(
                    player,
                    currentState.payload()
            );
        }

        if (currentState.isEmpty()) {
            STATES.remove(player.getUUID());
        } else {
            STATES.put(
                    player.getUUID(),
                    currentState
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

        var data =
                new SynchedEntityData.DataValue<>(
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

    private static byte getSharedFlags(
            Entity entity
    ) {
        List<SynchedEntityData.DataValue<?>> values =
                entity.getEntityData()
                        .getNonDefaultValues();

        if (values != null) {
            for (var value : values) {
                if (value.id() == 0) {
                    return (Byte) value.value();
                }
            }
        }

        return 0;
    }

    private record SensorState(
            Set<Integer> hostile,
            Set<Integer> mobs,
            Set<Integer> players
    ) {
        private SensorState {
            hostile = Set.copyOf(hostile);
            mobs = Set.copyOf(mobs);
            players = Set.copyOf(players);
        }

        private static SensorState empty() {
            return new SensorState(
                    Set.of(),
                    Set.of(),
                    Set.of()
            );
        }

        private boolean contains(int entityId) {
            return hostile.contains(entityId)
                    || mobs.contains(entityId)
                    || players.contains(entityId);
        }

        private Set<Integer> all() {
            if (isEmpty()) {
                return Set.of();
            }

            Set<Integer> result = new HashSet<>();
            result.addAll(hostile);
            result.addAll(mobs);
            result.addAll(players);
            return result;
        }

        private boolean isEmpty() {
            return hostile.isEmpty()
                    && mobs.isEmpty()
                    && players.isEmpty();
        }

        private SensorHighlightPayload payload() {
            return new SensorHighlightPayload(
                    List.copyOf(hostile),
                    List.copyOf(mobs),
                    List.copyOf(players)
            );
        }
    }
}
