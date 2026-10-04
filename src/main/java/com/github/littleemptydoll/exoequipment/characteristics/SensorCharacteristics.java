package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.EntityDetectionProperties;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

final class SensorCharacteristics {
    private SensorCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        double entityRange = 0.0D;
        boolean players = false;
        boolean mobs = false;
        boolean hostile = false;
        boolean entityActive = false;
        int entityConsumption = 0;
        double blockRange = 0.0D;
        boolean blockActive = false;
        int blockConsumption = 0;
        Set<Integer> blockColors = new TreeSet<>();
        boolean thermalInstalled = false;
        int thermalConsumption = 0;
        boolean thermalActive = false;

        for (var activeModule
                : CharacteristicsSupport
                .installedModules(context)) {

            var definition =
                    activeModule.definition();

            EntityDetectionProperties detection =
                    definition.entityDetection()
                            .orElse(null);

            if (detection != null) {
                entityRange =
                        Math.max(
                                entityRange,
                                detection.range()
                        );
                players |= detection.players();
                mobs |= detection.mobs();
                hostile |= detection.hostile();
                entityActive |= activeModule.module().active();
                entityConsumption += detection.activeConsumption();
            }

            if (definition.blockScanner().isPresent()) {
                blockActive |= activeModule.module().active();
                blockConsumption += definition.blockScanner().get().activeConsumption();
                blockColors.add(definition.blockScanner().get().color());
                blockRange =
                        Math.max(
                                blockRange,
                                definition.blockScanner()
                                        .get()
                                        .range()
                        );
            }

            if (definition.thermalVision().isPresent()) {
                var thermal = definition.thermalVision().orElseThrow();
                thermalInstalled = true;
                thermalConsumption += thermal.activeConsumption();
                thermalActive |= activeModule.module().active();
            }
        }

        if (entityRange > 0.0D) {
            addState(result, "entity_detection.active", entityActive);
            result.add(new Characteristic(
                    CharacteristicCategory.SENSOR,
                    "entity_detection.active_consumption",
                    CharacteristicType.STATIC,
                    entityConsumption
            ));
            result.add(
                    new Characteristic(
                            CharacteristicCategory.SENSOR,
                            "entity_detection.range",
                            CharacteristicType.STATIC,
                            entityRange
                    )
            );

            addEnabledFlag(
                    result,
                    "entity_detection.players",
                    players
            );
            addEnabledFlag(
                    result,
                    "entity_detection.mobs",
                    mobs
            );
            addEnabledFlag(
                    result,
                    "entity_detection.hostile",
                    hostile
            );
        }

        if (blockRange > 0.0D) {
            for (int color : blockColors) {
                result.add(new Characteristic(CharacteristicCategory.SENSOR,
                        "block_scanner.color", CharacteristicType.STATIC, color));
            }
            addState(result, "block_scanner.active", blockActive);
            result.add(new Characteristic(
                    CharacteristicCategory.SENSOR,
                    "block_scanner.active_consumption",
                    CharacteristicType.STATIC,
                    blockConsumption
            ));
            result.add(
                    new Characteristic(
                            CharacteristicCategory.SENSOR,
                            "block_scanner.range",
                            CharacteristicType.STATIC,
                            blockRange
                    )
            );
        }

        if (thermalInstalled) {
            addState(result, "thermal_vision.active", thermalActive);
            result.add(new Characteristic(
                    CharacteristicCategory.SENSOR, "thermal_vision.active_consumption",
                    CharacteristicType.STATIC, thermalConsumption));
        }
    }

    private static void addEnabledFlag(
            List<Characteristic> result,
            String key,
            boolean enabled
    ) {
        if (!enabled) {
            return;
        }

        result.add(
                new Characteristic(
                        CharacteristicCategory.SENSOR,
                        key,
                        CharacteristicType.STATE,
                        1.0D
                )
        );
    }

    private static void addState(List<Characteristic> result, String key, boolean active) {
        result.add(new Characteristic(
                CharacteristicCategory.SENSOR, key,
                CharacteristicType.STATE, active ? 1.0D : 0.0D));
    }
}
