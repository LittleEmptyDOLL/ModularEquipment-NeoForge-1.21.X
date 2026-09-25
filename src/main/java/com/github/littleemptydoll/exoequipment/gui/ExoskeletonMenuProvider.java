package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

import java.util.Optional;

public final class ExoskeletonMenuProvider {

    public static void open(ServerPlayer player) {
        Optional<ItemStack> exoskeleton = findBodyExoskeleton(player);

        if (exoskeleton.isEmpty()) {
            return;
        }

        ItemStack stack = exoskeleton.get();

        ((IPlayerExtension) player).openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignoredPlayer) ->
                                new ExoskeletonMenu(
                                        containerId,
                                        inventory,
                                        stack
                                ),
                        Component.translatable(
                                "menu.exoequipment.exoskeleton"
                        )
                ),
                buffer -> ItemStack.STREAM_CODEC.encode(buffer, stack.copy())
        );
    }

    public static Optional<ItemStack> findBodyExoskeleton(
            LivingEntity entity
    ) {
        return ExoskeletonAccess.findEquipped(entity);
    }

    private ExoskeletonMenuProvider() {}
}
