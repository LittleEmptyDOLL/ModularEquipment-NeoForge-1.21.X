package com.github.littleemptydoll.exoequipment.characteristics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CharacteristicsProvider {
    private CharacteristicsProvider() {}

    public static List<Characteristic> collect(
            CharacteristicsContext context
    ) {
        List<Characteristic> result =
                new ArrayList<>();

        EnergyCharacteristics.add(result, context);
        ThermalCharacteristics.add(result, context);
        DefenseCharacteristics.add(result, context);
        StatusProtectionCharacteristics.add(result, context);
        SurvivalCharacteristics.add(result, context);
        AttributeCharacteristics.add(result, context);
        EffectsCharacteristics.add(result, context);
        MobilityCharacteristics.add(result, context);
        SensorCharacteristics.add(result, context);
        UtilityCharacteristics.add(result, context);

        result.sort(
                Comparator
                        .comparing(
                                Characteristic::category
                        )
                        .thenComparing(
                                Characteristic::key
                        )
        );

        return List.copyOf(result);
    }
}
