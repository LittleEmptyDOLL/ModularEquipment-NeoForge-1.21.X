package com.github.littleemptydoll.exoequipment.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public abstract class EquipmentItem<D extends EquipmentDefinition> extends Item {

    private final DeferredHolder<D, D> definition;

    protected EquipmentItem(
            DeferredHolder<D, D> definition,
            Properties properties
    ) {
        super(properties);
        this.definition = definition;
    }

    public D getDefinition() {
        return definition.get();
    }

    public DeferredHolder<D, D> getDefinitionHolder() {
        return definition;
    }

    protected static Properties equipmentProperties(
            EquipmentProperties properties
    ) {
        return new Properties().rarity(properties.rarity());
    }
}
