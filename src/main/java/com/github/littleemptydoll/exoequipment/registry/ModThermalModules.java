package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.ThermalProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

final class ModThermalModules {
    private static final ModuleSize COOLER_SIZE = new ModuleSize(2, 2);
    private static final int THERMAL_PRIORITY = 6;

    private ModThermalModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerCooler(registry, "civilian_cooler", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, 4);
        registerCooler(registry, "engineering_cooler", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 8);
        registerCooler(registry, "military_cooler", EquipmentTier.MILITARY,
                Rarity.RARE, 8, 12);
        registerCooler(registry, "experimental_cooler", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 12, 20);
    }

    private static void registerCooler(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int energyConsumption, double cooling
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties,
                                ModuleCategory.THERMAL, COOLER_SIZE
                        )
                        .energy(new EnergyProperties(energyConsumption, THERMAL_PRIORITY))
                        .thermal(new ThermalProperties(0, cooling))
                        .build());
    }
}
