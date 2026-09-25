package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenu;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonProfileMenu;
import com.github.littleemptydoll.exoequipment.gui.MatrixMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(
            RegisterPayloadHandlersEvent event
    ) {
        PayloadRegistrar registrar =
                event.registrar("1");

        registrar.playToClient(
                CloakingStatePayload.TYPE,
                CloakingStatePayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() ->
                                com.github.littleemptydoll
                                        .exoequipment.client
                                        .CloakingClientState
                                        .setActive(
                                                payload.entityId(),
                                                payload.active()
                                        )
                        )
        );

        registrar.playToServer(
                BlinkPayload.TYPE,
                BlinkPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleBlink(player);
                            }
                        })
        );

        registrar.playToServer(
                CloakingPayload.TYPE,
                CloakingPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleCloaking(player);
                            }
                        })
        );

        registrar.playToServer(
                JetpackInputPayload.TYPE,
                JetpackInputPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleJetpackInput(
                                                player,
                                                payload
                                        );
                            }
                        })
        );

        registrar.playToServer(
                FlightPayload.TYPE,
                FlightPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleFlight(player);
                            }
                        })
        );

        registrar.playToServer(
                OpenExoskeletonPayload.TYPE,
                OpenExoskeletonPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleOpenExoskeleton(
                                                player
                                        );
                            }
                        })
        );

        registrar.playToServer(
                OpenProfilePayload.TYPE,
                OpenProfilePayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleOpenProfile(player);
                            }
                        })
        );

        registrar.playToServer(
                ProfileActionPayload.TYPE,
                ProfileActionPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleProfileAction(
                                                player,
                                                payload
                                        );
                            }
                        })
        );

        registrar.playToClient(
                ProfileSyncPayload.TYPE,
                ProfileSyncPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player() != null
                                    && context.player()
                                    .containerMenu
                                    instanceof ExoskeletonProfileMenu menu) {
                                menu.applySync(payload);
                            }
                        })
        );

        registrar.playToClient(
                ExoskeletonSyncPayload.TYPE,
                ExoskeletonSyncPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player() == null) {
                                return;
                            }

                            if (context.player()
                                    .containerMenu
                                    instanceof ExoskeletonMenu menu) {
                                menu.applyExoskeletonSync(
                                        payload.exoskeleton()
                                );
                            } else if (context.player()
                                    .containerMenu
                                    instanceof MatrixMenu menu) {
                                menu.applyExoskeletonSync(
                                        payload.exoskeleton()
                                );
                            }
                        })
        );

        registrar.playToClient(
                ExoskeletonProfileNameSyncPayload.TYPE,
                ExoskeletonProfileNameSyncPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player() != null
                                    && context.player()
                                    .containerMenu
                                    instanceof ExoskeletonMenu menu) {
                                menu.applyProfileNameSync(payload);
                            }
                        })
        );

        registrar.playToServer(
                OpenMatrixPayload.TYPE,
                OpenMatrixPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleOpenMatrix(
                                                player,
                                                payload
                                        );
                            }
                        })
        );

        registrar.playToServer(
                MatrixActionPayload.TYPE,
                MatrixActionPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player()
                                    instanceof ServerPlayer player) {
                                ServerPayloadHandlers
                                        .handleMatrixAction(
                                                player,
                                                payload
                                        );
                            }
                        })
        );

        registrar.playToClient(
                MatrixSyncPayload.TYPE,
                MatrixSyncPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() -> {
                            if (context.player() != null
                                    && context.player()
                                    .containerMenu
                                    instanceof MatrixMenu menu) {
                                menu.setMatrixStack(
                                        payload.matrix()
                                );
                            }
                        })
        );

        registrar.playToClient(
                BlockScannerPayload.TYPE,
                BlockScannerPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() ->
                                com.github.littleemptydoll
                                        .exoequipment.client
                                        .BlockScannerClient
                                        .setPositions(
                                                payload.positions()
                                        )
                        )
        );

        registrar.playToClient(
                SensorHighlightPayload.TYPE,
                SensorHighlightPayload.STREAM_CODEC,
                (payload, context) ->
                        context.enqueueWork(() ->
                                com.github.littleemptydoll
                                        .exoequipment.client
                                        .SensorHighlightClient
                                        .apply(payload)
                        )
        );
    }

    private ModNetworking() {}
}
