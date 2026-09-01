package com.github.littleemptydoll.exoequipment.registry;

import net.neoforged.neoforge.registries.DeferredRegister;

public final class EquipmentRegistry {
    private final DeferredRegister<? extends EquipmentDefinition> definitions;
    private final DeferredRegister.Items items;

    public EquipmentRegistry(
            DeferredRegister<? extends EquipmentDefinition> definitions,
            DeferredRegister.Items items
    ) {
        this.definitions = definitions;
        this.items = items;
    }
}
