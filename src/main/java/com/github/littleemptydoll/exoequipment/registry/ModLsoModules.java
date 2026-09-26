package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.BodyDamageRegenerationProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.TemperatureImpactProperties;
import com.github.littleemptydoll.exoequipment.module.TemperatureModifierProperties;
import com.github.littleemptydoll.exoequipment.module.ThirstProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

final class ModLsoModules {
    private static final ModuleSize THIRST_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize TEMPERATURE_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize REGENERATION_SIZE = new ModuleSize(2, 3);
    private static final int PRIORITY = 7;

    private ModLsoModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerThirst(registry, "civilian_thirst_assist", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 2, 0.10D);
        registerThirst(registry, "engineering_thirst_assist", EquipmentTier.ENGINEERING,
                Rarity.RARE, 3, 0.20D);
        registerThirst(registry, "military_thirst_assist", EquipmentTier.MILITARY,
                Rarity.RARE, 5, 0.30D);
        registerThirst(registry, "experimental_thirst_assist", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 8, 0.45D);

        registerTemperature(registry, "civilian_heat_resistance", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, 0.5D, 0.0D);
        registerTemperature(registry, "engineering_heat_resistance", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 1.0D, 0.0D);
        registerTemperature(registry, "military_heat_resistance", EquipmentTier.MILITARY,
                Rarity.RARE, 7, 1.5D, 0.0D);
        registerTemperature(registry, "experimental_heat_resistance", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 10, 2.5D, 0.0D);

        registerTemperature(registry, "civilian_cold_resistance", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, 0.0D, 0.5D);
        registerTemperature(registry, "engineering_cold_resistance", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 0.0D, 1.0D);
        registerTemperature(registry, "military_cold_resistance", EquipmentTier.MILITARY,
                Rarity.RARE, 7, 0.0D, 1.5D);
        registerTemperature(registry, "experimental_cold_resistance", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 10, 0.0D, 2.5D);

        registerModifier(registry, "civilian_heater", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, 0.5D, 0.0D);
        registerModifier(registry, "engineering_heater", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 1.0D, 0.0D);
        registerModifier(registry, "military_heater", EquipmentTier.MILITARY,
                Rarity.RARE, 7, 1.5D, 0.0D);
        registerModifier(registry, "experimental_heater", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 10, 2.0D, 0.0D);
        registerModifier(registry, "civilian_personal_cooler", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, -0.5D, 0.0D);
        registerModifier(registry, "engineering_personal_cooler", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, -1.0D, 0.0D);
        registerModifier(registry, "military_personal_cooler", EquipmentTier.MILITARY,
                Rarity.RARE, 7, -1.5D, 0.0D);
        registerModifier(registry, "experimental_personal_cooler", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 10, -2.0D, 0.0D);
        registerModifier(registry, "civilian_thermal_resistance", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 3, 0.0D, 0.5D);
        registerModifier(registry, "engineering_thermal_resistance", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 0.0D, 1.0D);
        registerModifier(registry, "military_thermal_resistance", EquipmentTier.MILITARY,
                Rarity.RARE, 7, 0.0D, 1.5D);
        registerModifier(registry, "experimental_thermal_resistance", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 10, 0.0D, 2.5D);

        registerImpact(registry, "engineering_temperature_isolation", EquipmentTier.ENGINEERING,
                Rarity.RARE, 5, 0.25D);
        registerImpact(registry, "military_temperature_isolation", EquipmentTier.MILITARY,
                Rarity.RARE, 8, 0.50D);
        registerImpact(registry, "experimental_temperature_isolation", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 12, 0.75D);

        registerBodyRegeneration(registry, "civilian_body_regeneration", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 6, 0.05D);
        registerBodyRegeneration(registry, "engineering_body_regeneration", EquipmentTier.ENGINEERING,
                Rarity.RARE, 10, 0.10D);
        registerBodyRegeneration(registry, "military_body_regeneration", EquipmentTier.MILITARY,
                Rarity.RARE, 16, 0.20D);
        registerBodyRegeneration(registry, "experimental_body_regeneration", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 25, 0.35D);
    }

    private static void registerThirst(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int consumption, double reduction
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.SURVIVAL, THIRST_SIZE)
                        .energy(new EnergyProperties(consumption, PRIORITY))
                        .thirst(new ThirstProperties(reduction))
                        .build());
    }

    private static void registerTemperature(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity, int consumption,
            double heatResistance, double coldResistance
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.THERMAL, TEMPERATURE_SIZE)
                        .energy(new EnergyProperties(consumption, PRIORITY))
                        .temperatureModifier(new TemperatureModifierProperties(
                                0.0D, heatResistance, coldResistance, 0.0D))
                        .build());
    }

    private static void registerImpact(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int consumption, double resistance
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.THERMAL, TEMPERATURE_SIZE)
                        .energy(new EnergyProperties(consumption, PRIORITY))
                        .temperatureImpact(new TemperatureImpactProperties(resistance))
                        .build());
    }

    private static void registerModifier(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity, int consumption,
            double temperature, double thermalResistance
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.THERMAL, TEMPERATURE_SIZE)
                        .energy(new EnergyProperties(consumption, PRIORITY))
                        .temperatureModifier(new TemperatureModifierProperties(
                                temperature, 0.0D, 0.0D, thermalResistance))
                        .build());
    }

    private static void registerBodyRegeneration(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int consumption, double healthPerSecond
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.SURVIVAL, REGENERATION_SIZE)
                        .energy(new EnergyProperties(consumption, PRIORITY))
                        .bodyDamageRegeneration(new BodyDamageRegenerationProperties(healthPerSecond))
                        .build());
    }
}
