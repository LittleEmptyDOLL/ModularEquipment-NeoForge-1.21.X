package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
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

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices()
                    .get(slot)
                    .matrix()
                    .orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0;
                 moduleIndex < matrix.modules().size();
                 moduleIndex++) {

                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                if (poweredModules != null
                        && ModModules.getDefinition(module.id())
                        .energy()
                        .filter(energy -> energy.consumption() > 0)
                        .isPresent()
                        && !poweredModules.contains(
                                new InstalledModuleReference(
                                        slot,
                                        moduleIndex
                                )
                        )) {
                    continue;
                }

                DamageReductionProperties properties =
                        ModModules.getDefinition(module.id())
                                .damageReduction()
                                .orElse(null);

                if (properties == null) {
                    continue;
                }

                double reduction = properties.reduction(damageType);

                multiplier *= 1.0D - reduction;

                if (multiplier <= 0.0D) {
                    return 0.0D;
                }
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
        MatrixData matrix = data.matrices().get(matrixSlot).matrix().orElse(null);
        if (matrix == null) return 1.0D;

        double multiplier = 1.0D;
        for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
            InstalledModule module = matrix.modules().get(moduleIndex);
            if (!FrameOperations.isModuleSupported(data, module)) continue;

            InstalledModuleReference reference = new InstalledModuleReference(matrixSlot, moduleIndex);
            if (poweredModules != null
                    && ModModules.getDefinition(module.id()).energy()
                    .filter(energy -> energy.consumption() > 0).isPresent()
                    && !poweredModules.contains(reference)) continue;

            DamageReductionProperties properties = ModModules.getDefinition(module.id())
                    .damageReduction().orElse(null);
            if (properties == null) continue;

            multiplier *= 1.0D - properties.reduction(damageType);
            if (multiplier <= 0.0D) return 0.0D;
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
