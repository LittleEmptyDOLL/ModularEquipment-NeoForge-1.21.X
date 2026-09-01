package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

public interface EquipmentDefinition {
    ResourceLocation id();

    EquipmentProperties properties();

    default EquipmentTier tier() {
        return properties().tier();
    }

    default Rarity rarity() {
        return properties().rarity();
    }
}