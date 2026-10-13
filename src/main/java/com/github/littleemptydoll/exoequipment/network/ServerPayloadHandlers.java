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
import com.github.littleemptydoll.exoequipment.module.CombatToggleOperations;
import com.github.littleemptydoll.exoequipment.module.FlightOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.JetpackInputState;
import com.github.littleemptydoll.exoequipment.module.SensorToggleOperations;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
                        playModuleSound(
                                player,
                                ModSounds.BLINK_ACTIVATE.get(),
                                0.8F,
                                1.0F
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

    static void handleSensorToggle(ServerPlayer player, SensorTogglePayload payload) {
        SensorToggleOperations.Kind kind = switch (payload.kind()) {
            case SensorTogglePayload.ENTITY -> SensorToggleOperations.Kind.ENTITY;
            case SensorTogglePayload.BLOCK -> SensorToggleOperations.Kind.BLOCK;
            case SensorTogglePayload.THERMAL -> SensorToggleOperations.Kind.THERMAL;
            default -> null;
        };
        if (kind == null) {
            return;
        }

        ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .ifPresent(exoskeleton -> {
                    var data = ExoskeletonItem.getData(exoskeleton);
                    var updated = SensorToggleOperations.toggle(data, kind);
                    if (updated != data) {
                        exoskeleton.set(ModDataComponents.EXOSKELETON_DATA.get(), updated);

                        boolean enabled = SensorToggleOperations.isActive(updated, kind);
                        float pitch = switch (kind) {
                            case ENTITY -> 0.96F;
                            case BLOCK -> 1.05F;
                            case THERMAL -> 1.14F;
                        };
                        playModuleSound(
                                player,
                                enabled
                                        ? ModSounds.VISOR_ON.get()
                                        : ModSounds.VISOR_OFF.get(),
                                0.72F,
                                pitch
                        );
                    }
                });
    }

    static void handleCombatToggle(ServerPlayer player, CombatTogglePayload payload) {
        CombatToggleOperations.Kind kind = switch (payload.kind()) {
            case CombatTogglePayload.LASER -> CombatToggleOperations.Kind.LASER;
            case CombatTogglePayload.DISCHARGE -> CombatToggleOperations.Kind.DISCHARGE;
            default -> null;
        };
        if (kind == null) return;
        ExoskeletonMenuProvider.findBodyExoskeleton(player).ifPresent(exoskeleton -> {
            var data = ExoskeletonItem.getData(exoskeleton);
            var updated = CombatToggleOperations.toggle(data, kind);
            if (updated != data) {
                exoskeleton.set(ModDataComponents.EXOSKELETON_DATA.get(), updated);

                boolean enabled = CombatToggleOperations.isActive(updated, kind);
                SoundEvent sound = switch (kind) {
                    case LASER -> enabled
                            ? ModSounds.LASER_DEFENSE_ON.get()
                            : ModSounds.LASER_DEFENSE_OFF.get();
                    case DISCHARGE -> enabled
                            ? ModSounds.DISCHARGE_DEFENSE_ON.get()
                            : ModSounds.DISCHARGE_DEFENSE_OFF.get();
                };
                playModuleSound(player, sound, 0.82F, 1.0F);
            }
        });
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

    private static void playModuleSound(
            ServerPlayer player,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundSource.PLAYERS,
                volume,
                pitch
        );
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
