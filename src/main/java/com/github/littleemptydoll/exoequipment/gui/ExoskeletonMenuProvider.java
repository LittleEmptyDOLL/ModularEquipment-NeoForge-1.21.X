package com.github.littleemptydoll.exoequipment.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

public final class ExoskeletonMenuProvider {

    public static void open(
            ServerPlayer player,
            int inventorySlot
    ) {
        ((IPlayerExtension) player).openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignoredPlayer) ->
                                new ExoskeletonMenu(
                                        containerId,
                                        inventory,
                                        inventorySlot
                                ),
                        Component.translatable(
                                "menu.exoequipment.exoskeleton"
                        )
                ),
                buffer -> buffer.writeVarInt(inventorySlot)
        );
    }

    private ExoskeletonMenuProvider() {}
}
