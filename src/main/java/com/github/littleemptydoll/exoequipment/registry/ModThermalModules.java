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
    private static final ModuleSize HEATER_SIZE = new ModuleSize(2, 2);
    private static final int THERMAL_PRIORITY = 6;

    private ModThermalModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerThermal(registry, "civilian_cooler", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, 0, 4, COOLER_SIZE);
        registerThermal(registry, "engineering_cooler", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 0, 8, COOLER_SIZE);
        registerThermal(registry, "military_cooler", EquipmentTier.MILITARY,
                Rarity.RARE, 8, 0, 12, COOLER_SIZE);
        registerThermal(registry, "experimental_cooler", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 12, 0, 20, COOLER_SIZE);

        registerThermal(registry, "civilian_heater", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, 4, 0, HEATER_SIZE);
        registerThermal(registry, "engineering_heater", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 8, 0, HEATER_SIZE);
        registerThermal(registry, "military_heater", EquipmentTier.MILITARY,
                Rarity.RARE, 8, 12, 0, HEATER_SIZE);
        registerThermal(registry, "experimental_heater", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 12, 20, 0, HEATER_SIZE);
    }

    private static void registerThermal(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int energyConsumption, double heatGeneration,
            double cooling, ModuleSize size
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties,
                                ModuleCategory.THERMAL, size
                        )
                        .energy(new EnergyProperties(energyConsumption, THERMAL_PRIORITY))
                        .thermal(new ThermalProperties(heatGeneration, cooling))
                        .build());
    }
}
