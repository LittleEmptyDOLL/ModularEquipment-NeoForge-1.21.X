package com.github.littleemptydoll.exoequipment.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class EquipmentEntry<
        D extends EquipmentDefinition,
        I extends EquipmentItem<D>
        > {
    private final DeferredHolder<D, D> definition;
    private final DeferredHolder<Item, I> item;

    public EquipmentEntry(
            DeferredHolder<D, D> definition,
            DeferredHolder<Item, I> item
    ) {
        this.definition = definition;
        this.item = item;
    }

    public DeferredHolder<D, D> definition() {
        return definition;
    }

    public DeferredHolder<Item, I> item() {
        return item;
    }

    public D getDefinition() {
        return definition.get();
    }

    public I getItem() {
        return item.get();
    }
}
