package com.github.littleemptydoll.exoequipment.gui;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ExoskeletonComponentSlot extends Slot {

    private final Class<? extends Item> itemClass;

    public ExoskeletonComponentSlot(
            Container container,
            int slot,
            int x,
            int y,
            Class<? extends Item> itemClass
    ) {
        super(container, slot, x, y);
        this.itemClass = itemClass;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return itemClass.isInstance(stack.getItem());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
