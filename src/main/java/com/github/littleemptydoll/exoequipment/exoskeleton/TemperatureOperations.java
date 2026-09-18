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

        if (temperature <= bonus.minTemperature()) {
            return 1.0D;
        }

        if (temperature >= bonus.maxTemperature()) {
            return 1.0D + bonus.maximumBonus();
        }

        double range = bonus.maxTemperature() - bonus.minTemperature();
        if (range <= 0.0D) {
            return 1.0D + bonus.maximumBonus();
        }

        double progress =
                (temperature - bonus.minTemperature()) / range;

        return 1.0D + bonus.maximumBonus() * progress;
    }
}
