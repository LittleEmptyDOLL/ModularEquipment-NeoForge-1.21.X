package com.github.littleemptydoll.exoequipment.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExoskeletonInventorySlot extends Slot {

    private final ItemStack exoskeleton;

    public ExoskeletonInventorySlot(
            Container container,
            int slot,
            int x,
            int y,
            ItemStack exoskeleton
    ) {
        super(container, slot, x, y);
        this.exoskeleton = exoskeleton;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return getItem() != exoskeleton;
    }

    @Override
    public boolean mayPickup(Player player) {
        return getItem() != exoskeleton;
    }
}
