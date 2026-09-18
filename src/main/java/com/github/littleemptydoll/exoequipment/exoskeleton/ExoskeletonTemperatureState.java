package com.github.littleemptydoll.exoequipment.exoskeleton;

public record ExoskeletonTemperatureState(
        double temperature,
        double thermalBalance
) {
    public static final double INITIAL_TEMPERATURE = 20.0D;

    /**
     * Temperature change per one second for one unit of thermal balance.
     */
    public static final double TEMPERATURE_RESPONSE = 0.05D;

    public static ExoskeletonTemperatureState calculate(
            ExoskeletonData data
    ) {
        double thermalBalance = ExoskeletonState.calculateThermalBalance(data, data.temperature());
        double temperature =
                data.temperature() + thermalBalance * TEMPERATURE_RESPONSE;

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
