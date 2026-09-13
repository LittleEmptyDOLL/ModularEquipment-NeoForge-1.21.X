package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.item.MatrixItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

public final class MatrixMenuProvider {

    private MatrixMenuProvider() {}

    public static void openFromHand(ServerPlayer player, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= player.getInventory().getContainerSize()) {
            return;
        }

        ItemStack stack = player.getInventory().getItem(inventorySlot);
        if (!(stack.getItem() instanceof MatrixItem)) {
            return;
        }

        open(
                player,
                stack,
                MatrixMenu.SOURCE_HAND,
                inventorySlot
        );
    }

    public static void openFromExoskeleton(
            ServerPlayer player,
            ItemStack exoskeleton,
            int matrixSlot,
            ItemStack matrix
    ) {
        if (!(matrix.getItem() instanceof MatrixItem)) {
            return;
        }

        open(
                player,
                matrix,
                MatrixMenu.SOURCE_EXOSKELETON,
                matrixSlot,
                exoskeleton
        );
    }

    private static void open(
            ServerPlayer player,
            ItemStack matrix,
            int sourceType,
            int sourceIndex
    ) {
        open(player, matrix, sourceType, sourceIndex, null);
    }

    private static void open(
            ServerPlayer player,
            ItemStack matrix,
            int sourceType,
            int sourceIndex,
            ItemStack exoskeleton
    ) {
        ((IPlayerExtension) player).openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignoredPlayer) ->
                                new MatrixMenu(
                                        containerId,
                                        inventory,
                                        matrix,
                                        sourceType,
                                        sourceIndex
                                ),
                        Component.translatable("menu.exoequipment.matrix")
                ),
                buffer -> {
                    ItemStack.STREAM_CODEC.encode(buffer, matrix.copy());
                    buffer.writeByte(sourceType);
                    buffer.writeByte(sourceIndex);
                }
        );
    }
}
