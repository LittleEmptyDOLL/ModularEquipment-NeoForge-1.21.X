package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExoskeletonMenu extends AbstractContainerMenu {

    private final Inventory playerInventory;
    private final int exoskeletonSlot;

    // Client
    public ExoskeletonMenu(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buffer
    ) {
        this(
                containerId,
                playerInventory,
                buffer.readInt()
        );
    }

    public ExoskeletonMenu(
            int containerId,
            Inventory playerInventory,
            int exoskeletonSlot
    ) {
        super(
                ModMenus.EXOSKELETON.get(),
                containerId
        );

        this.playerInventory = playerInventory;
        this.exoskeletonSlot = exoskeletonSlot;

        addSlot(
                new ExoskeletonSlot(
                        playerInventory,
                        exoskeletonSlot,
                        8,
                        8
                )
        );
    }

    public ItemStack getExoskeleton() {
        return playerInventory.getItem(exoskeletonSlot);
    }

    public ExoskeletonItem getExoskeletonItem() {
        return ExoskeletonItem.get(
                getExoskeleton()
        );
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack stack =
                player.getInventory()
                        .getItem(exoskeletonSlot);

        return !stack.isEmpty()
                && stack.getItem() instanceof ExoskeletonItem;
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {
        return ItemStack.EMPTY;
    }

    private static class ExoskeletonSlot extends Slot {

        public ExoskeletonSlot(
                Inventory inventory,
                int index,
                int x,
                int y
        ) {
            super(
                    inventory,
                    index,
                    x,
                    y
            );
        }

        @Override
        public boolean mayPickup(
                Player player
        ) {
            return false;
        }

        @Override
        public boolean mayPlace(
                ItemStack stack
        ) {
            return false;
        }
    }
}
