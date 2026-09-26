package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.GenerationProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.StorageProperties;
import com.github.littleemptydoll.exoequipment.module.ThermalProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

final class ModEnergyModules {
    private static final ModuleSize BATTERY_SIZE = new ModuleSize(2, 3);
    private static final ModuleSize GENERATOR_SIZE = new ModuleSize(3, 3);

    private ModEnergyModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerBattery(
                registry,
                "civilian_battery",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                25_000,
                250,
                250
        );
        registerBattery(
                registry,
                "engineering_battery",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                100_000,
                1_000,
                750
        );
        registerBattery(
                registry,
                "military_battery",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                75_000,
                750,
                1_000
        );
        registerBattery(
                registry,
                "experimental_battery",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                250_000,
                2_500,
                2_500
        );

        registerGenerator(
                registry,
                "civilian_generator",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                5,
                2
        );
        registerGenerator(
                registry,
                "engineering_generator",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                15,
                5
        );
        registerGenerator(
                registry,
                "military_generator",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                10,
                6
        );
        registerGenerator(
                registry,
                "experimental_generator",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                25,
                10
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
