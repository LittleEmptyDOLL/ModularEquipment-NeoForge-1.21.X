package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.energy.EnergyState;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.module.AttributeOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class CharacteristicsProvider {
    private CharacteristicsProvider() {}

    public static List<Characteristic> collect(CharacteristicsContext context) {
        List<Characteristic> result = new ArrayList<>();

        addEnergy(result, context);
        addThermal(result, context);
        addAttributes(result, context);

        result.sort(
                Comparator.comparing(Characteristic::category)
                        .thenComparing(Characteristic::key)
        );

        return List.copyOf(result);
    }

    private static void addEnergy(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        EnergyState state = EnergyState.calculate(context.data());

        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "consumption",
                CharacteristicType.CURRENT,
                state.consumption()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "generation",
                CharacteristicType.CURRENT,
                state.generation()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_capacity",
                CharacteristicType.CURRENT,
                state.storageCapacity()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_input",
                CharacteristicType.CURRENT,
                state.storageInput()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_output",
                CharacteristicType.CURRENT,
                state.storageOutput()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "stored_energy",
                CharacteristicType.CURRENT,
                state.storedEnergy()
        ));

        if (!context.isMatrixScope()) {
            result.add(new Characteristic(
                    CharacteristicCategory.ENERGY,
                    "max_input",
                    CharacteristicType.STATIC,
                    state.maxInput()
            ));
            result.add(new Characteristic(
                    CharacteristicCategory.ENERGY,
                    "max_output",
                    CharacteristicType.STATIC,
                    state.maxOutput()
            ));
        }
    }

    private static void addThermal(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        var state = ExoskeletonState.calculateState(context.data());

        result.add(new Characteristic(
                CharacteristicCategory.THERMAL,
                "heat_generation",
                CharacteristicType.CURRENT,
                state.heatGeneration()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.THERMAL,
                "cooling",
                CharacteristicType.CURRENT,
                state.cooling()
        ));
        result.add(new Characteristic(
                CharacteristicCategory.THERMAL,
                "thermal_balance",
                CharacteristicType.CURRENT,
                state.thermalBalance()
        ));
    }

    private static void addAttributes(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        if (context.player() == null) {
            return;
        }

        Set<InstalledModuleReference> poweredModules = context.poweredModules();

        AttributeOperations.calculate(
                context.player(),
                context.data(),
                poweredModules
        ).forEach((key, value) -> {
            String operation = switch (key.operation()) {
                case ADD_VALUE -> "add_value";
                case ADD_MULTIPLIED_BASE -> "add_multiplied_base";
                case ADD_MULTIPLIED_TOTAL -> "add_multiplied_total";
            };

            result.add(new Characteristic(
                    CharacteristicCategory.ATTRIBUTES,
                    "attribute." + key.attributeId() + "." + operation,
                    CharacteristicType.CURRENT,
                    value
            ));
        });
    }
}
