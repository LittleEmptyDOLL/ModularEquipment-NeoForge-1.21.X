package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.energy.EnergyState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;

import java.util.List;

final class EnergyCharacteristics {
    private EnergyCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        if (context.isMatrixScope()) {
            MatrixData matrix =
                    CharacteristicsSupport
                            .selectedMatrix(context);

            if (matrix == null) {
                return;
            }

            MatrixState state =
                    CharacteristicsSupport
                            .matrixState(
                                    context,
                                    matrix
                            );

            int storedEnergy =
                    MatrixOperations
                            .calculateStoredEnergy(
                                    matrix,
                                    module ->
                                            FrameOperations
                                                    .isModuleSupported(
                                                            context.data(),
                                                            module
                                                    ),
                                    context.data()
                                            .temperature()
                            );

            addValues(
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

        EnergyState state =
                EnergyState.calculate(
                        context.data()
                );

        addValues(
                result,
                state.consumption(),
                state.generation(),
                state.storageCapacity(),
                state.storageInput(),
                state.storageOutput(),
                state.storedEnergy()
        );

        addCurrent(
                result,
                "max_input",
                state.maxInput()
        );
        addCurrent(
                result,
                "max_output",
                state.maxOutput()
        );
    }

    private static void addValues(
            List<Characteristic> result,
            int consumption,
            int generation,
            int storageCapacity,
            int storageInput,
            int storageOutput,
            int storedEnergy
    ) {
        addCurrent(
                result,
                "consumption",
                consumption
        );
        addCurrent(
                result,
                "generation",
                generation
        );
        addCurrent(
                result,
                "storage_capacity",
                storageCapacity
        );
        addCurrent(
                result,
                "storage_input",
                storageInput
        );
        addCurrent(
                result,
                "storage_output",
                storageOutput
        );
        addCurrent(
                result,
                "stored_energy",
                storedEnergy
        );
    }

    private static void addCurrent(
            List<Characteristic> result,
            String key,
            double value
    ) {
        if (value == 0.0D) {
            return;
        }

        result.add(
                new Characteristic(
                        CharacteristicCategory.ENERGY,
                        key,
                        CharacteristicType.CURRENT,
                        value
                )
        );
    }
}
