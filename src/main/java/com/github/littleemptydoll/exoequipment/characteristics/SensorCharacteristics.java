package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.EntityDetectionProperties;

import java.util.List;

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
        double blockRange = 0.0D;

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
            }

            if (definition.blockScanner().isPresent()) {
                blockRange =
                        Math.max(
                                blockRange,
                                definition.blockScanner()
                                        .get()
                                        .range()
                        );
            }
        }

        if (entityRange > 0.0D) {
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
            result.add(
                    new Characteristic(
                            CharacteristicCategory.SENSOR,
                            "block_scanner.range",
                            CharacteristicType.STATIC,
                            blockRange
                    )
            );
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
}
