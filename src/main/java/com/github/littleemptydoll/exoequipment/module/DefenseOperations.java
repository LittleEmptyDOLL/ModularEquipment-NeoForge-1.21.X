package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class DefenseOperations {
    private DefenseOperations() {}

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            ResourceLocation damageType
    ) {
        return calculateDamageMultiplier(data, damageType, null);
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            ResourceLocation damageType,
            Set<InstalledModuleReference> poweredModules
    ) {
        double multiplier = 1.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            DamageReductionProperties properties =
                    activeModule.definition()
                            .damageReduction()
                            .orElse(null);

            if (properties == null) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            multiplier *= 1.0D
                    - properties.reduction(damageType) * efficiency;

            if (multiplier <= 0.0D) {
                return 0.0D;
            }
        }

        return Math.max(0.0D, multiplier);
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            int matrixSlot,
            ResourceLocation damageType,
            Set<InstalledModuleReference> poweredModules
    ) {
        double multiplier = 1.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.supportedInMatrix(
                        data,
                        matrixSlot
                )) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            DamageReductionProperties properties =
                    activeModule.definition()
                            .damageReduction()
                            .orElse(null);

            if (properties == null) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            multiplier *= 1.0D
                    - properties.reduction(damageType) * efficiency;

            if (multiplier <= 0.0D) {
                return 0.0D;
            }
        }

        return Math.max(0.0D, multiplier);
    }

    public static double applyDamageReduction(
            double damage,
            ExoskeletonData data,
            ResourceLocation damageType
    ) {
        return applyDamageReduction(
                damage,
                data,
                damageType,
                null
        );
    }

    public static double applyDamageReduction(
            double damage,
            ExoskeletonData data,
            ResourceLocation damageType,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (damage <= 0.0D) {
            return 0.0D;
        }

        return damage * calculateDamageMultiplier(
                data,
                damageType,
                poweredModules
        );
    }
}
