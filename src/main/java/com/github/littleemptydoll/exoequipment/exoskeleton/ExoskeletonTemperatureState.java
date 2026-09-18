package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.matrix.MatrixState;

public record ExoskeletonTemperatureState(
        double temperature,
        double thermalBalance
) {
    public static final double INITIAL_TEMPERATURE = 20.0D;
    public static final double TEMPERATURE_PER_TICK = 0.1D;

    public static ExoskeletonTemperatureState calculate(
            ExoskeletonData data
    ) {
        MatrixState state = ExoskeletonState.calculateState(data);

        return new ExoskeletonTemperatureState(
                data.temperature(),
                state.thermalBalance()
        );
    }

    public static ExoskeletonData tick(
            ExoskeletonData data
    ) {
        ExoskeletonTemperatureState state = calculate(data);

        if (state.thermalBalance() == 0.0D) {
            return data;
        }

        return data.withTemperature(
                state.temperature()
                        + state.thermalBalance() * TEMPERATURE_PER_TICK
        );
    }
}
