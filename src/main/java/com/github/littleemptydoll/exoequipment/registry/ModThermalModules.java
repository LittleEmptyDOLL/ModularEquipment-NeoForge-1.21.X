package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.ModList;

final class ModThermalModules {
    private static final ModuleSize SMALL_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize MEDIUM_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize LARGE_SIZE = new ModuleSize(2, 3);
    private static final int THERMAL_PRIORITY = 6;
    private static final int TEMPERATURE_PRIORITY = 7;

    private ModThermalModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerThermal(registry, "basic_cooler", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 15, 0, 5, MEDIUM_SIZE);
        registerThermal(registry, "advanced_cooler", EquipmentTier.ENGINEERING,
                Rarity.RARE, 30, 0, 15, MEDIUM_SIZE);
        registerThermal(registry, "experimental_cooler", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 70, 0, 30, LARGE_SIZE);

        registerThermal(registry, "basic_heater", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 15, 5, 0, MEDIUM_SIZE);
        registerThermal(registry, "advanced_heater", EquipmentTier.ENGINEERING,
                Rarity.RARE, 30, 15, 0, MEDIUM_SIZE);
        registerThermal(registry, "experimental_heater", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 70, 30, 0, LARGE_SIZE);

        if (ModList.get() != null && ModList.get().isLoaded("legendarysurvivaloverhaul")) {
            registerTemperature(registry, "heat_protection", EquipmentTier.CIVILIAN, Rarity.UNCOMMON,
                    10, 0.0D, 1.0D, 0.0D, 0.0D, SMALL_SIZE);
            registerTemperature(registry, "cold_protection", EquipmentTier.CIVILIAN, Rarity.UNCOMMON,
                    10, 0.0D, 0.0D, 1.0D, 0.0D, SMALL_SIZE);
            registerTemperature(registry, "thermal_protection", EquipmentTier.MILITARY, Rarity.RARE,
                    30, 0.0D, 0.0D, 0.0D, 2.0D, SMALL_SIZE);

            registerTemperature(registry, "personal_heater", EquipmentTier.ENGINEERING, Rarity.RARE,
                    20, 5.0D, 0.0D, 0.0D, 0.0D, MEDIUM_SIZE);
            registerTemperature(registry, "personal_cooler", EquipmentTier.ENGINEERING, Rarity.RARE,
                    20, -5.0D, 0.0D, 0.0D, 0.0D, MEDIUM_SIZE);

            registerImpact(registry, "thermal_isolation", EquipmentTier.ENGINEERING,
                    Rarity.RARE, 20, 0.50D);
            registerImpact(registry, "experimental_thermal_isolation", EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC, 40, 0.75D);
        }
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

    private static void registerTemperature(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity, int consumption,
            double temperature, double heatResistance, double coldResistance,
            double thermalResistance, ModuleSize size
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.THERMAL, size)
                        .energy(new EnergyProperties(consumption, TEMPERATURE_PRIORITY))
                        .temperatureModifier(new TemperatureModifierProperties(
                                temperature, heatResistance, coldResistance, thermalResistance))
                        .build());
    }

    private static void registerImpact(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int consumption, double resistance
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.THERMAL, MEDIUM_SIZE)
                        .energy(new EnergyProperties(consumption, TEMPERATURE_PRIORITY))
                        .temperatureImpact(new TemperatureImpactProperties(resistance))
                        .build());
    }
}
