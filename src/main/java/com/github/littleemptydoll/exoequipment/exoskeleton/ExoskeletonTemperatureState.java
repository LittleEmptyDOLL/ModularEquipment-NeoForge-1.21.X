package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.energy.EnergyTickResult;

public record ExoskeletonTemperatureState(
        double temperature,
        double thermalBalance
) {
    public static final double INITIAL_TEMPERATURE = 20.0D;

    /**
     * Player temperature impact per one degree of exoskeleton temperature
     * deviation from INITIAL_TEMPERATURE.
     *
     * 0.1 means that a 10°C deviation of the exoskeleton produces a 1°C
     * temperature modifier for the player before insulation is applied.
     */
    public static final double TEMPERATURE_IMPACT_PER_DEGREE = 0.1D;

    /**
     * Passive heat exchange with the environment.
     * Positive values pull the temperature back toward INITIAL_TEMPERATURE.
     */
    public static final double PASSIVE_THERMAL_EXCHANGE = 1.0D;

    /**
     * Temperature change per one second for one unit of thermal balance.
     */
    public static final double TEMPERATURE_RESPONSE = 0.05D;

    public static ExoskeletonTemperatureState calculate(
            ExoskeletonData data
    ) {
        return calculate(data, null);
    }

    public static ExoskeletonTemperatureState calculate(
            ExoskeletonData data,
            EnergyTickResult energyResult
    ) {
        double thermalBalance = ExoskeletonState.calculateThermalBalance(
                data,
                data.temperature(),
                energyResult == null ? null : energyResult.poweredModules()
        );
        thermalBalance -=
                (data.temperature() - INITIAL_TEMPERATURE)
                        * PASSIVE_THERMAL_EXCHANGE;

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
        return tick(data, null);
    }

    public static ExoskeletonData tick(
            ExoskeletonData data,
            EnergyTickResult energyResult
    ) {
        ExoskeletonTemperatureState state = calculate(data, energyResult);

        if (Double.compare(data.temperature(), state.temperature()) == 0) {
            return data;
        }

        ExoskeletonData updated = data.withTemperature(state.temperature());
        return ExoskeletonState.normalizeStoredEnergy(updated);
    }
}
