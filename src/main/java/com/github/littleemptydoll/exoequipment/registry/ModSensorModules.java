package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.BlockScannerProperties;
import com.github.littleemptydoll.exoequipment.module.EffectsProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.EntityDetectionProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.ThermalVisionProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

import java.util.List;
import java.util.Map;

final class ModSensorModules {
    private static final ModuleSize NIGHT_VISION_SIZE = new ModuleSize(1, 1);
    private static final ModuleSize ENTITY_SENSOR_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize BLOCK_SCANNER_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize THERMAL_VISION_SIZE = new ModuleSize(2, 3);

    private static final int SENSOR_PRIORITY = 4;

    private static final ResourceLocation NIGHT_VISION =
            ResourceLocation.parse("minecraft:night_vision");
    private static final ResourceLocation ORES_TAG =
            ResourceLocation.parse("c:ores");

    private ModSensorModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerNightVision(registry);
        registerEntitySensors(registry);
        registerBlockScanners(registry);
        registerThermalVision(registry);
    }

    private static void registerNightVision(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registry.register(
                "night_vision",
                new EquipmentProperties(
                        EquipmentTier.CIVILIAN,
                        Rarity.UNCOMMON
                ),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SENSOR,
                                        NIGHT_VISION_SIZE
                                )
                                .energy(new EnergyProperties(
                                        20,
                                        SENSOR_PRIORITY
                                ))
                                .effects(new EffectsProperties(
                                        Map.of(NIGHT_VISION, 0)
                                ))
                                .build()
        );
    }

    private static void registerEntitySensors(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerEntitySensor(
                registry,
                "basic_entity_sensor",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                20,
                30,
                12.0D,
                false,
                true,
                true
        );
        registerEntitySensor(
                registry,
                "lifeform_scanner",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                30,
                40,
                20.0D,
                true,
                true,
                true
        );
        registerEntitySensor(
                registry,
                "threat_sensor",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                30,
                50,
                28.0D,
                true,
                false,
                true
        );
        registerEntitySensor(
                registry,
                "multispectral_sensor",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                40,
                70,
                40.0D,
                true,
                true,
                true
        );
    }

    private static void registerBlockScanners(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerBlockScanner(
                registry,
                "resource_scanner",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                20,
                50,
                16.0D
        );
        registerBlockScanner(
                registry,
                "advanced_resource_scanner",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                30,
                70,
                24.0D
        );
    }

    private static void registerThermalVision(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registry.register(
                "thermal_vision",
                new EquipmentProperties(EquipmentTier.MILITARY, Rarity.RARE),
                (id, properties) -> ModuleDefinition.builder(
                                id, properties, ModuleCategory.SENSOR, THERMAL_VISION_SIZE)
                        .energy(new EnergyProperties(20, SENSOR_PRIORITY))
                        .thermalVision(new ThermalVisionProperties(40))
                        .build()
        );
    }

    private static void registerEntitySensor(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int energyConsumption,
            int activeConsumption,
            double range,
            boolean players,
            boolean mobs,
            boolean hostile
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SENSOR,
                                        ENTITY_SENSOR_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        SENSOR_PRIORITY
                                ))
                                .entityDetection(new EntityDetectionProperties(
                                        range,
                                        players,
                                        mobs,
                                        hostile,
                                        activeConsumption
                                ))
                                .build()
        );
    }

    private static void registerBlockScanner(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int energyConsumption,
            int activeConsumption,
            double range
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SENSOR,
                                        BLOCK_SCANNER_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        SENSOR_PRIORITY
                                ))
                                .blockScanner(new BlockScannerProperties(
                                        range,
                                        List.of(),
                                        List.of(ORES_TAG),
                                        activeConsumption
                                ))
                                .build()
        );
    }
}
