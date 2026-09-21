package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.energy.EnergyState;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;
import com.github.littleemptydoll.exoequipment.module.AttributeOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
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
        if (context.isMatrixScope()) {
            MatrixData matrix = getSelectedMatrix(context);
            if (matrix == null) {
                return;
            }

            MatrixState state = calculateMatrixState(context, matrix);
            int storedEnergy = MatrixOperations.calculateStoredEnergy(
                    matrix,
                    module -> FrameOperations.isModuleSupported(context.data(), module),
                    context.data().temperature()
            );

            addEnergyValues(
                    result,
                    state.energyConsumption(),
                    state.energyGeneration(),
                    state.energyStorageCapacity(),
                    state.energyStorageInput(),
                    state.energyStorageOutput(),
                    storedEnergy
            );
            return;
        }

        EnergyState state = EnergyState.calculate(context.data());

        addEnergyValues(
                result,
                state.consumption(),
                state.generation(),
                state.storageCapacity(),
                state.storageInput(),
                state.storageOutput(),
                state.storedEnergy()
        );

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

    private static void addEnergyValues(
            List<Characteristic> result,
            int consumption,
            int generation,
            int storageCapacity,
            int storageInput,
            int storageOutput,
            int storedEnergy
    ) {
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "consumption",
                CharacteristicType.CURRENT,
                consumption
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "generation",
                CharacteristicType.CURRENT,
                generation
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_capacity",
                CharacteristicType.CURRENT,
                storageCapacity
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_input",
                CharacteristicType.CURRENT,
                storageInput
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "storage_output",
                CharacteristicType.CURRENT,
                storageOutput
        ));
        result.add(new Characteristic(
                CharacteristicCategory.ENERGY,
                "stored_energy",
                CharacteristicType.CURRENT,
                storedEnergy
        ));
    }

    private static void addThermal(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        MatrixState state;

        if (context.isMatrixScope()) {
            MatrixData matrix = getSelectedMatrix(context);
            if (matrix == null) {
                return;
            }
            state = calculateMatrixState(context, matrix);
        } else {
            state = ExoskeletonState.calculateState(context.data());
        }

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
        if (context.player() == null || context.isMatrixScope()) {
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

    private static MatrixData getSelectedMatrix(CharacteristicsContext context) {
        int slot = context.matrixSlot();
        return context.data().matrices().get(slot).matrix().orElse(null);
    }

    private static MatrixState calculateMatrixState(
            CharacteristicsContext context,
            MatrixData matrix
    ) {
        return MatrixOperations.calculateState(
                matrix,
                module -> FrameOperations.isModuleSupported(context.data(), module),
                context.data().temperature()
        );
    }
}
