package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

public interface EquipmentDefinition {
    ResourceLocation id();
    EquipmentTier tier();
    Rarity rarity();
}
