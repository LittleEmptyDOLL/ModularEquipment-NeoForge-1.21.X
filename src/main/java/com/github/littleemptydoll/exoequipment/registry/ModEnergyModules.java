package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

import java.util.Optional;

final class ModEnergyModules {
    private static final ModuleSize BATTERY_SIZE = new ModuleSize(2, 3);
    private static final ModuleSize GENERATOR_SIZE = new ModuleSize(3, 3);

    private ModEnergyModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerBattery(
                registry,
                "basic_battery",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                250_000,
                100,
                100
        );
        registerBattery(
                registry,
                "high_rate_battery",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                650_000,
                600,
                500
        );
        registerBattery(
                registry,
                "high_capacity_battery",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                1_000_000,
                300,
                400
        );
        registerBattery(
                registry,
                "discharge_battery",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                500_000,
                250,
                800
        );
        registerBattery(
                registry,
                "experimental_battery",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                3_000_000,
                2_000,
                2_000
        );

        registerGenerator(
                registry,
                "compact_generator",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                40,
                5
        );
        registerGenerator(
                registry,
                "advanced_generator",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                130,
                10
        );
        registerGenerator(
                registry,
                "high_output_generator",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                180,
                20
        );
        registerGenerator(
                registry,
                "experimental_generator",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                500,
                30
        );
        registerGenerator(
                registry,
                "creative_generator",
                EquipmentTier.CREATIVE,
                Rarity.EPIC,
                100000,
                0
        );
    }

    private static void registerBattery(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int capacity,
            int maxInput,
            int maxOutput
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.ENERGY,
                                        BATTERY_SIZE
                                )
                                .storage(new StorageProperties(
                                        capacity,
                                        maxInput,
                                        maxOutput
                                ))
                                .build()
        );
    }

    private static void registerGenerator(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int generation,
            int heatGeneration
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.ENERGY,
                                        GENERATOR_SIZE
                                )
                                .generation(new GenerationProperties(generation))
                                .thermal(new ThermalProperties(
                                        heatGeneration,
                                        0
                                ))
                                .build()
        );
    }
}
