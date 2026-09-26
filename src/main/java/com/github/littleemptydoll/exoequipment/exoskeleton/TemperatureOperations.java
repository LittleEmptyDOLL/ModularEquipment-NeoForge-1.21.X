package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.TemperatureBonus;
import com.github.littleemptydoll.exoequipment.module.TemperatureProperties;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

public final class TemperatureOperations {
    private TemperatureOperations() {}

    public static double calculateModuleEfficiency(
            ModuleDefinition definition,
            double temperature
    ) {
        if (Double.isNaN(temperature)) {
            return 1.0D;
        }

        if (!Double.isFinite(temperature)) {
            throw new IllegalArgumentException(
                    "Module temperature must be finite"
            );
        }

        if (definition.temperature().isEmpty()) {
            return 1.0D;
        }

        TemperatureProperties properties = definition.temperature().get();

        double efficiency = calculateBaseEfficiency(properties, temperature);
        efficiency *= calculateTemperatureBonus(properties.bonus(), temperature);

        return Math.max(TemperatureProperties.MIN_EFFICIENCY, efficiency);
    }

    public static double calculateModuleEfficiency(
            net.minecraft.resources.ResourceLocation moduleId,
            double temperature
    ) {
        return calculateModuleEfficiency(
                ModModules.getDefinition(moduleId),
                temperature
        );
    }

    private static double calculateBaseEfficiency(
            TemperatureProperties properties,
            double temperature
    ) {
        if (temperature >= properties.minTemperature()
                && temperature <= properties.maxTemperature()) {
            return 1.0D;
        }

        double distance;
        if (temperature < properties.minTemperature()) {
            distance = properties.minTemperature() - temperature;
        } else {
            distance = temperature - properties.maxTemperature();
        }

        double falloff = properties.efficiencyFalloff()
                .orElseGet(() -> {
                    double range = properties.maxTemperature()
                            - properties.minTemperature();
                    return 0.9D / Math.max(1.0D, range);
                });

        return Math.max(
                TemperatureProperties.MIN_EFFICIENCY,
                1.0D - distance * falloff
        );
    }

    private static double calculateTemperatureBonus(
            java.util.Optional<TemperatureBonus> optionalBonus,
            double temperature
    ) {
        if (optionalBonus.isEmpty()) {
            return 1.0D;
        }

        TemperatureBonus bonus = optionalBonus.get();
        double min = bonus.minTemperature();
        double max = bonus.maxTemperature();
        double range = max - min;

        if (range == 0.0D) {
            return Double.compare(temperature, min) == 0
                    ? 1.0D + bonus.maximumBonus()
                    : 1.0D;
        }

        if (temperature <= min || temperature >= max) {
            return 1.0D;
        }

        double midpoint = (min + max) / 2.0D;
        double distance = Math.abs(temperature - midpoint);
        double progress = 1.0D - (distance / (range / 2.0D));

        return 1.0D
                + bonus.maximumBonus()
                * Math.max(0.0D, progress);
    }
}
