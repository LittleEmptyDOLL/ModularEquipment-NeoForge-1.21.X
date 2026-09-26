package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public final class DefenseOperations {
    private DefenseOperations() {}

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            ResourceLocation damageType
    ) {
        return calculateDamageMultiplier(
                data,
                damageType,
                null
        );
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            ResourceLocation damageType,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateDamageMultiplier(
                data,
                ExoskeletonModules.activeSupported(data),
                poweredModules,
                properties -> properties.reduction(damageType)
        );
    }

    public static double calculateDamageSourceMultiplier(
            ExoskeletonData data,
            DamageSource source,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateDamageMultiplier(
                data,
                ExoskeletonModules.activeSupported(data),
                poweredModules,
                properties -> properties.reductionForSource(source)
        );
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            int matrixSlot,
            ResourceLocation damageType,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateDamageMultiplier(
                data,
                ExoskeletonModules.supportedInMatrix(
                        data,
                        matrixSlot
                ),
                poweredModules,
                properties -> properties.reduction(damageType)
        );
    }

    public static double calculateDamageTagMultiplier(
            ExoskeletonData data,
            ResourceLocation damageTag,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateDamageMultiplier(
                data,
                ExoskeletonModules.activeSupported(data),
                poweredModules,
                properties -> properties.reduction(
                        null,
                        tag -> tag.equals(damageTag)
                )
        );
    }

    public static double calculateDamageTagMultiplier(
            ExoskeletonData data,
            int matrixSlot,
            ResourceLocation damageTag,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateDamageMultiplier(
                data,
                ExoskeletonModules.supportedInMatrix(
                        data,
                        matrixSlot
                ),
                poweredModules,
                properties -> properties.reduction(
                        null,
                        tag -> tag.equals(damageTag)
                )
        );
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

    public static double applyDamageReduction(
            double damage,
            ExoskeletonData data,
            DamageSource source,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (damage <= 0.0D) {
            return 0.0D;
        }

        return damage * calculateDamageSourceMultiplier(
                data,
                source,
                poweredModules
        );
    }

    private static double calculateDamageMultiplier(
            ExoskeletonData data,
            List<ExoskeletonModules.ActiveModule> modules,
            Set<InstalledModuleReference> poweredModules,
            ToDoubleFunction<DamageReductionProperties> reductionResolver
    ) {
        double multiplier = 1.0D;

        for (ExoskeletonModules.ActiveModule activeModule : modules) {
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

            double reduction = Math.min(
                    1.0D,
                    Math.max(
                            0.0D,
                            reductionResolver.applyAsDouble(properties)
                                    * efficiency
                    )
            );

            multiplier *= 1.0D - reduction;

            if (multiplier <= 0.0D) {
                return 0.0D;
            }
        }

        return Math.max(0.0D, multiplier);
    }
}
