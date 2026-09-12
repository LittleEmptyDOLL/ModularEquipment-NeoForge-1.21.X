package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import top.theillusivec4.curios.api.CuriosApi;

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
                )
        );
    }

    public static Optional<ItemStack> findBodyExoskeleton(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler("body"))
                .flatMap(stacks -> {
                    for (int slot = 0; slot < stacks.getStacks().getSlots(); slot++) {
                        ItemStack stack = stacks.getStacks().getStackInSlot(slot);
                        if (stack.getItem() instanceof ExoskeletonItem) {
                            return Optional.of(stack);
                        }
                    }
                    return Optional.empty();
                });
    }

    private ExoskeletonMenuProvider() {}
}
