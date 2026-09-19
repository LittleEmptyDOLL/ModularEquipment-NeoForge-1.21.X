package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.Set;

public final class DamageChanceProtectionOperations {
    private DamageChanceProtectionOperations() {}

    public static double calculateChance(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double remainingChance = 1.0D;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                var definition = ModModules.getDefinition(module.id());

                if (definition.energy()
                        .filter(energy -> energy.consumption() > 0)
                        .isPresent()
                        && !poweredModules.contains(reference)) {
                    continue;
                }

                var properties = definition.damageChanceProtection().orElse(null);

                if (properties == null) {
                    continue;
                }

                remainingChance *= 1.0D - properties.chance();

                if (remainingChance <= 0.0D) {
                    return 1.0D;
                }
            }
        }

        return Math.max(0.0D, Math.min(1.0D, 1.0D - remainingChance));
    }

    public static boolean blocksDamage(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double chance = calculateChance(data, poweredModules);
        return chance >= 1.0D || Math.random() < chance;
    }
}
