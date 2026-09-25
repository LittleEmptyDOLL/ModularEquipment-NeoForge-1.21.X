package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;

import java.util.Set;

public final class BodyDamageRegenerationOperations {
    private BodyDamageRegenerationOperations() {}

    public static double calculateHealthPerSecond(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        double healthPerSecond = 0.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            BodyDamageRegenerationProperties properties =
                    activeModule.definition()
                            .bodyDamageRegeneration()
                            .orElse(null);

            if (properties == null
                    || !BodyPart.applies(
                            properties.bodyParts(),
                            bodyPart
                    )) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            healthPerSecond +=
                    properties.healthPerSecond() * efficiency;
        }

        return Math.max(0.0D, healthPerSecond);
    }
}
