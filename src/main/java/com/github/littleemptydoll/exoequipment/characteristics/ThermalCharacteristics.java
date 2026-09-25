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

        result.add(
                new Characteristic(
                        CharacteristicCategory.THERMAL,
                        "heat_generation",
                        CharacteristicType.CURRENT,
                        state.heatGeneration()
                )
        );
        result.add(
                new Characteristic(
                        CharacteristicCategory.THERMAL,
                        "cooling",
                        CharacteristicType.CURRENT,
                        state.cooling()
                )
        );
        result.add(
                new Characteristic(
                        CharacteristicCategory.THERMAL,
                        "thermal_balance",
                        CharacteristicType.CURRENT,
                        state.thermalBalance()
                )
        );
    }
}
