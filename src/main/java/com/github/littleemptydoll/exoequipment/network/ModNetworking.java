package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenu;
import com.github.littleemptydoll.exoequipment.gui.ExoskeletonMenuProvider;
import com.github.littleemptydoll.exoequipment.gui.MatrixMenu;
import com.github.littleemptydoll.exoequipment.gui.MatrixMenuProvider;
import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                OpenExoskeletonPayload.TYPE,
                OpenExoskeletonPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        ExoskeletonMenuProvider.open(serverPlayer);
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
                            payload.targetY()
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
    }

    private ModNetworking() {}
}
