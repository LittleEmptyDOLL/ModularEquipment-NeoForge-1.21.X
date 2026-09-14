package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

import java.util.Optional;

public final class ExoskeletonProfileMenuProvider {
    private ExoskeletonProfileMenuProvider() {}

    public static void open(ServerPlayer player) {
        Optional<ItemStack> exoskeleton = ExoskeletonMenuProvider.findBodyExoskeleton(player);
        if (exoskeleton.isEmpty()) {
            return;
        }

        ItemStack stack = exoskeleton.get();
        ExoskeletonData data = ExoskeletonItem.getData(stack);
        if (data.controller().isEmpty() || data.profiles().isEmpty()) {
            return;
        }

        ((IPlayerExtension) player).openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignoredPlayer) ->
                                new ExoskeletonProfileMenu(
                                        containerId,
                                        inventory,
                                        stack
                                ),
                        Component.translatable("menu.exoequipment.profiles")
                ),
                buffer -> ItemStack.STREAM_CODEC.encode(buffer, stack.copy())
        );
    }
}
