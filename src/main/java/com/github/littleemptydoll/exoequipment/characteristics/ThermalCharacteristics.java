package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;

import java.util.List;

final class ThermalCharacteristics {
    private ThermalCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        MatrixState state;

        if (context.isMatrixScope()) {
            MatrixData matrix =
                    CharacteristicsSupport
                            .selectedMatrix(context);

            if (matrix == null) {
                return;
            }

            state =
                    CharacteristicsSupport
                            .matrixState(
                                    context,
                                    matrix
                            );
        } else {
            state =
                    ExoskeletonState.calculateState(
                            context.data()
                    );
        }

        addCurrent(
                result,
                "heat_generation",
                state.heatGeneration()
        );
        addCurrent(
                result,
                "cooling",
                state.cooling()
        );
        addCurrent(
                result,
                "thermal_balance",
                state.thermalBalance()
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
                        CharacteristicCategory.THERMAL,
                        key,
                        CharacteristicType.CURRENT,
                        value
                )
        );
    }
}
