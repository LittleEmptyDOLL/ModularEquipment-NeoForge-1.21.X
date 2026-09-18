package com.github.littleemptydoll.exoequipment.exoskeleton;

public record ExoskeletonTemperatureState(
        double temperature,
        double thermalBalance
) {
    public static final double INITIAL_TEMPERATURE = 20.0D;

    public static ExoskeletonTemperatureState calculate(
            ExoskeletonData data
    ) {
        double thermalBalance = ExoskeletonState.calculateThermalBalance(data, data.temperature());
        double temperature = INITIAL_TEMPERATURE + thermalBalance;

        return new ExoskeletonTemperatureState(
                temperature,
                thermalBalance
        );
    }

    public static ExoskeletonData tick(
            ExoskeletonData data
    ) {
        ExoskeletonTemperatureState state = calculate(data);

        if (Double.compare(data.temperature(), state.temperature()) == 0) {
            return data;
        }

        ExoskeletonData updated = data.withTemperature(state.temperature());
        return ExoskeletonState.normalizeStoredEnergy(updated);
    }
}
