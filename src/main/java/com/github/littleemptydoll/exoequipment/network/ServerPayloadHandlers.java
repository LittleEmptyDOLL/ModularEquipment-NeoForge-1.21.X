package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.event.CloakingEvents;
import com.github.littleemptydoll.exoequipment.event.FlightEvents;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
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
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.JetpackInputState;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

final class ServerPayloadHandlers {
    private ServerPayloadHandlers() {}

    static void handleBlink(
            ServerPlayer player
    ) {
        ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .ifPresent(exoskeleton -> {
                    var result = BlinkOperations.activate(
                            ExoskeletonItem.getData(exoskeleton),
                            player
                    );

                    if (result.activated()) {
                        exoskeleton.set(
                                ModDataComponents.EXOSKELETON_DATA.get(),
                                result.data()
                        );
                    }
                });
    }

    static void handleCloaking(
            ServerPlayer player
    ) {
        ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .ifPresent(exoskeleton -> {
                    if (CloakingEvents.isCloakingActive(player)) {
                        CloakingEvents.deactivate(player);
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
                        CloakingEvents.markActive(player);
                    }
                });
    }

    static void handleJetpackInput(
            ServerPlayer player,
            JetpackInputPayload payload
    ) {
        JetpackInputState.set(
                player,
                payload.input()
        );
    }

    static void handleFlight(
            ServerPlayer player
    ) {
        if (player.isCreative()
                || player.isSpectator()) {
            return;
        }

        ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .ifPresent(exoskeleton -> {
                    if (FlightEvents.isActive(player)) {
                        FlightEvents.markInactive(player);

                        var data =
                                ExoskeletonItem.getData(
                                        exoskeleton
                                );

                        for (InstalledModuleReference reference
                                : activeFlightReferences(data)) {
                            data = FlightOperations.deactivate(
                                    data,
                                    reference
                            );
                        }

                        exoskeleton.set(
                                ModDataComponents.EXOSKELETON_DATA.get(),
                                data
                        );
                        return;
                    }

                    ExoskeletonRuntimeState runtime =
                            exoskeleton.getOrDefault(
                                    ModDataComponents
                                            .EXOSKELETON_RUNTIME
                                            .get(),
                                    ExoskeletonRuntimeState.empty()
                            );

                    var result =
                            FlightOperations.activate(
                                    ExoskeletonItem.getData(
                                            exoskeleton
                                    ),
                                    runtime.poweredModules()
                            );

                    if (result.activated()) {
                        exoskeleton.set(
                                ModDataComponents.EXOSKELETON_DATA.get(),
                                result.data()
                        );
                        FlightEvents.markActive(player);
                    }
                });
    }

    private static List<InstalledModuleReference>
    activeFlightReferences(
            com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData data
    ) {
        List<InstalledModuleReference> references =
                new ArrayList<>();

        for (int slot = 0;
             slot < data.matrices().size();
             slot++) {

            var matrix =
                    data.matrices()
                            .get(slot)
                            .matrix()
                            .orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int index = 0;
                 index < matrix.modules().size();
                 index++) {

                if (matrix.modules()
                        .get(index)
                        .flightActive()) {

                    references.add(
                            new InstalledModuleReference(
                                    slot,
                                    index
                            )
                    );
                }
            }
        }

        return List.copyOf(references);
    }

    static void handleOpenExoskeleton(
            ServerPlayer player
    ) {
        ExoskeletonMenuProvider.open(player);

        if (player.containerMenu
                instanceof ExoskeletonMenu menu) {

            PacketDistributor.sendToPlayer(
                    player,
                    new ExoskeletonProfileNameSyncPayload(
                            menu.getServerActiveProfileName()
                    )
            );
        }
    }

    static void handleOpenProfile(
            ServerPlayer player
    ) {
        if (!(player.containerMenu
                instanceof ExoskeletonMenu)) {
            return;
        }

        ExoskeletonProfileMenuProvider.open(player);

        if (player.containerMenu
                instanceof ExoskeletonProfileMenu menu) {

            PacketDistributor.sendToPlayer(
                    player,
                    menu.getSyncPayload()
            );
        }
    }

    static void handleProfileAction(
            ServerPlayer player,
            ProfileActionPayload payload
    ) {
        if (!(player.containerMenu
                instanceof ExoskeletonProfileMenu menu)) {
            return;
        }

        if (menu.handleAction(
                payload.actionType(),
                payload.profile(),
                payload.matrix(),
                payload.name()
        )) {
            PacketDistributor.sendToPlayer(
                    player,
                    menu.getSyncPayload()
            );
        }
    }

    static void handleOpenMatrix(
            ServerPlayer player,
            OpenMatrixPayload payload
    ) {
        if (!(player.containerMenu
                instanceof ExoskeletonMenu menu)) {
            return;
        }

        int matrixSlot = payload.matrixSlot();

        if (matrixSlot < 0
                || matrixSlot
                >= ExoskeletonMenu.MATRIX_COUNT) {
            return;
        }

        var slot =
                menu.getSlot(
                        ExoskeletonMenu.MATRIX_START_SLOT
                                + matrixSlot
                );

        if (!(slot.getItem().getItem()
                instanceof MatrixItem)) {
            return;
        }

        ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .ifPresent(exoskeleton ->
                        MatrixMenuProvider
                                .openFromExoskeleton(
                                        player,
                                        exoskeleton,
                                        matrixSlot,
                                        slot.getItem()
                                )
                );
    }

    static void handleMatrixAction(
            ServerPlayer player,
            MatrixActionPayload payload
    ) {
        if (!(player.containerMenu
                instanceof MatrixMenu menu)) {
            return;
        }

        boolean changed =
                menu.handleAction(
                        player,
                        payload.actionType(),
                        payload.x(),
                        payload.y(),
                        payload.targetX(),
                        payload.targetY(),
                        payload.rotation()
                );

        if (changed) {
            PacketDistributor.sendToPlayer(
                    player,
                    new MatrixSyncPayload(
                            menu.getMatrixStack().copy()
                    )
            );
        }
    }
}
