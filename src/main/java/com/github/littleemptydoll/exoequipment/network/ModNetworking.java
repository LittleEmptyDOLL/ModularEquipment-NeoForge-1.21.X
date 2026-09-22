package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.event.CloakingEvents;
import com.github.littleemptydoll.exoequipment.event.FlightEvents;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenu;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenuProvider;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonProfileMenu;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonProfileMenuProvider;
import com.github.littleemptydoll.exoequipment.gui.MatrixMenu;
import com.github.littleemptydoll.exoequipment.gui.MatrixMenuProvider;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import com.github.littleemptydoll.exoequipment.module.BlinkOperations;
import com.github.littleemptydoll.exoequipment.module.CloakingOperations;
import com.github.littleemptydoll.exoequipment.module.FlightOperations;
import com.github.littleemptydoll.exoequipment.module.JetpackInputState;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                CloakingStatePayload.TYPE,
                CloakingStatePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        com.github.littleemptydoll.exoequipment.client.CloakingClientState
                                .setActive(payload.entityId(), payload.active())
                )
        );

        registrar.playToServer(
                BlinkPayload.TYPE,
                BlinkPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                        return;
                    }

                    ExoskeletonMenuProvider.findBodyExoskeleton(serverPlayer)
                            .ifPresent(exoskeleton -> {
                                var result = BlinkOperations.activate(
                                        ExoskeletonItem.getData(exoskeleton),
                                        serverPlayer
                                );

                                if (result.activated()) {
                                    exoskeleton.set(
                                            ModDataComponents.EXOSKELETON_DATA.get(),
                                            result.data()
                                    );
                                }
                            });
                })
        );

        registrar.playToServer(
                CloakingPayload.TYPE,
                CloakingPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                        return;
                    }

                    ExoskeletonMenuProvider.findBodyExoskeleton(serverPlayer)
                            .ifPresent(exoskeleton -> {
                                if (CloakingEvents.isCloakingActive(serverPlayer)) {
                                    CloakingEvents.deactivate(serverPlayer);
                                    return;
                                }

                                var result = CloakingOperations.activate(
                                        ExoskeletonItem.getData(exoskeleton)
                                );

                                if (result.activated()) {
                                    exoskeleton.set(
                                            ModDataComponents.EXOSKELETON_DATA.get(),
                                            result.data()
                                    );
                                    CloakingEvents.markActive(serverPlayer);
                                }
                            });
                })
        );

        registrar.playToServer(
                JetpackInputPayload.TYPE,
                JetpackInputPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        JetpackInputState.set(serverPlayer, payload.input());
                    }
                })
        );

        registrar.playToServer(
                FlightPayload.TYPE,
                FlightPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer serverPlayer)
                            || serverPlayer.isCreative()
                            || serverPlayer.isSpectator()) {
                        return;
                    }

                    ExoskeletonMenuProvider.findBodyExoskeleton(serverPlayer)
                            .ifPresent(exoskeleton -> {
                                if (FlightEvents.isActive(serverPlayer)) {
                                    ExoskeletonItem.getData(exoskeleton);
                                    FlightEvents.markInactive(serverPlayer);
                                    var data = ExoskeletonItem.getData(exoskeleton);
                                    var refs = new java.util.ArrayList<com.github.littleemptydoll.exoequipment.module.InstalledModuleReference>();

                                    for (int slot = 0; slot < data.matrices().size(); slot++) {
                                        var matrix = data.matrices().get(slot).matrix().orElse(null);
                                        if (matrix == null) continue;
                                        for (int index = 0; index < matrix.modules().size(); index++) {
                                            var module = matrix.modules().get(index);
                                            if (module.flightActive()) {
                                                refs.add(new com.github.littleemptydoll.exoequipment.module.InstalledModuleReference(slot, index));
                                            }
                                        }
                                    }

                                    for (var reference : refs) {
                                        data = FlightOperations.deactivate(data, reference);
                                    }

                                    exoskeleton.set(
                                            ModDataComponents.EXOSKELETON_DATA.get(),
                                            data
                                    );
                                    return;
                                }

                                var runtime = exoskeleton.get(
                                        ModDataComponents.EXOSKELETON_RUNTIME.get()
                                );
                                if (runtime == null) {
                                    runtime = com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState.empty();
                                }

                                var result = FlightOperations.activate(
                                        ExoskeletonItem.getData(exoskeleton),
                                        runtime.poweredModules()
                                );

                                if (result.activated()) {
                                    exoskeleton.set(
                                            ModDataComponents.EXOSKELETON_DATA.get(),
                                            result.data()
                                    );
                                    FlightEvents.markActive(serverPlayer);
                                }
                            });
                })
        );

        registrar.playToServer(
                OpenExoskeletonPayload.TYPE,
                OpenExoskeletonPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        ExoskeletonMenuProvider.open(serverPlayer);

                        if (serverPlayer.containerMenu instanceof ExoskeletonMenu menu) {
                            PacketDistributor.sendToPlayer(
                                    serverPlayer,
                                    new ExoskeletonProfileNameSyncPayload(
                                            menu.getServerActiveProfileName()
                                    )
                            );
                        }
                    }
                })
        );

        registrar.playToServer(
                OpenProfilePayload.TYPE,
                OpenProfilePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                        return;
                    }

                    if (!(serverPlayer.containerMenu instanceof ExoskeletonMenu)) {
                        return;
                    }

                    ExoskeletonProfileMenuProvider.open(serverPlayer);

                    if (serverPlayer.containerMenu instanceof ExoskeletonProfileMenu menu) {
                        PacketDistributor.sendToPlayer(
                                serverPlayer,
                                menu.getSyncPayload()
                        );
                    }
                })
        );

        registrar.playToServer(
                ProfileActionPayload.TYPE,
                ProfileActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                        return;
                    }
                    if (!(serverPlayer.containerMenu instanceof ExoskeletonProfileMenu menu)) {
                        return;
                    }

                    if (menu.handleAction(
                            payload.action(),
                            payload.profile(),
                            payload.matrix(),
                            payload.name()
                    )) {
                        PacketDistributor.sendToPlayer(
                                serverPlayer,
                                menu.getSyncPayload()
                        );
                    }
                })
        );

        registrar.playToClient(
                ProfileSyncPayload.TYPE,
                ProfileSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() != null
                            && context.player().containerMenu instanceof ExoskeletonProfileMenu menu) {
                        menu.applySync(payload);
                    }
                })
        );

        registrar.playToClient(
                ExoskeletonSyncPayload.TYPE,
                ExoskeletonSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() == null) {
                        return;
                    }

                    if (context.player().containerMenu instanceof ExoskeletonMenu menu) {
                        menu.applyExoskeletonSync(payload.exoskeleton());
                    } else if (context.player().containerMenu instanceof MatrixMenu menu) {
                        menu.applyExoskeletonSync(payload.exoskeleton());
                    }
                })
        );

        registrar.playToClient(
                ExoskeletonProfileNameSyncPayload.TYPE,
                ExoskeletonProfileNameSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() != null
                            && context.player().containerMenu instanceof ExoskeletonMenu menu) {
                        menu.applyProfileNameSync(payload);
                    }
                })
        );

        registrar.playToServer(
                OpenMatrixPayload.TYPE,
                OpenMatrixPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                        return;
                    }

                    if (!(serverPlayer.containerMenu instanceof ExoskeletonMenu menu)) {
                        return;
                    }

                    int matrixSlot = payload.matrixSlot();
                    if (matrixSlot < 0 || matrixSlot >= ExoskeletonMenu.MATRIX_COUNT) {
                        return;
                    }

                    var slot = menu.getSlot(
                            ExoskeletonMenu.MATRIX_START_SLOT + matrixSlot
                    );

                    if (!(slot.getItem().getItem() instanceof MatrixItem)) {
                        return;
                    }

                    ExoskeletonMenuProvider.findBodyExoskeleton(serverPlayer)
                            .ifPresent(exoskeleton ->
                                    MatrixMenuProvider.openFromExoskeleton(
                                            serverPlayer,
                                            exoskeleton,
                                            matrixSlot,
                                            slot.getItem()
                                    )
                            );
                })
        );

        registrar.playToServer(
                MatrixActionPayload.TYPE,
                MatrixActionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                        return;
                    }
                    if (!(serverPlayer.containerMenu instanceof MatrixMenu menu)) {
                        return;
                    }

                    boolean changed = menu.handleAction(
                            serverPlayer,
                            payload.action(),
                            payload.x(),
                            payload.y(),
                            payload.targetX(),
                            payload.targetY(),
                            payload.rotation()
                    );

                    if (changed) {
                        PacketDistributor.sendToPlayer(
                                serverPlayer,
                                new MatrixSyncPayload(menu.getMatrixStack().copy())
                        );
                    }
                })
        );

        registrar.playToClient(
                MatrixSyncPayload.TYPE,
                MatrixSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() != null
                            && context.player().containerMenu instanceof MatrixMenu menu) {
                        menu.setMatrixStack(payload.matrix());
                    }
                })
        );

        registrar.playToClient(
                BlockScannerPayload.TYPE,
                BlockScannerPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        com.github.littleemptydoll.exoequipment.client.BlockScannerClient
                                .setPositions(payload.positions())
                )
        );

        registrar.playToClient(
                SensorHighlightPayload.TYPE,
                SensorHighlightPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> com.github.littleemptydoll.exoequipment.client.SensorHighlightClient.apply(payload)
                )
        );
    }

    private ModNetworking() {}
}
