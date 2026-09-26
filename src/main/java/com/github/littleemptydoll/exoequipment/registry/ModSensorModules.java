package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.BlockScannerProperties;
import com.github.littleemptydoll.exoequipment.module.EffectsProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.EntityDetectionProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

import java.util.List;
import java.util.Map;

final class ModSensorModules {
    private static final ModuleSize NIGHT_VISION_SIZE = new ModuleSize(1, 1);
    private static final ModuleSize ENTITY_SENSOR_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize BLOCK_SCANNER_SIZE = new ModuleSize(2, 2);

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
    }

    private static void registerNightVision(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registry.register(
                "civilian_night_vision",
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
                                        2,
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
                "civilian_entity_sensor",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                4,
                12.0D,
                false,
                true,
                false
        );
        registerEntitySensor(
                registry,
                "engineering_entity_sensor",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                6,
                16.0D,
                false,
                true,
                true
        );
        registerEntitySensor(
                registry,
                "military_entity_sensor",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                9,
                24.0D,
                true,
                true,
                true
        );
        registerEntitySensor(
                registry,
                "experimental_entity_sensor",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                14,
                32.0D,
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
                "civilian_ore_scanner",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                5,
                8.0D
        );
        registerBlockScanner(
                registry,
                "engineering_ore_scanner",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                8,
                12.0D
        );
        registerBlockScanner(
                registry,
                "military_ore_scanner",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                12,
                16.0D
        );
        registerBlockScanner(
                registry,
                "experimental_ore_scanner",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                18,
                24.0D
        );
    }

    private static void registerEntitySensor(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int energyConsumption,
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
                                        hostile
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
                                        List.of(ORES_TAG)
                                ))
                                .build()
        );
    }
}
