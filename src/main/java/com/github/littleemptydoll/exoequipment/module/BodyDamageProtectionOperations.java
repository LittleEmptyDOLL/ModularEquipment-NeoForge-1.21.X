package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BodyDamageProtectionOperations {
    private BodyDamageProtectionOperations() {}

    public static double calculateChance(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        double remainingChance = 1.0D;

        for (ModuleProtection protection : protections(data, bodyPart, poweredModules)) {
            remainingChance *= 1.0D - protection.properties().chance();

            if (remainingChance <= 0.0D) {
                return 1.0D;
            }
        }

        return Math.max(0.0D, Math.min(1.0D, 1.0D - remainingChance));
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        double multiplier = 1.0D;

        for (ModuleProtection protection : protections(data, bodyPart, poweredModules)) {
            multiplier *= 1.0D - protection.properties().damageReduction();

            if (multiplier <= 0.0D) {
                return 0.0D;
            }
        }

        return Math.max(0.0D, multiplier);
    }

    private static List<ModuleProtection> protections(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<ModuleProtection> result = new ArrayList<>();

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

                BodyDamageProtectionProperties properties =
                        definition.bodyDamageProtection().orElse(null);

                if (properties == null
                        || !BodyPart.applies(properties.bodyParts(), bodyPart)) {
                    continue;
                }

                result.add(new ModuleProtection(reference, properties));
            }
        }

        return result;
    }

    private record ModuleProtection(
            InstalledModuleReference reference,
            BodyDamageProtectionProperties properties
    ) {}
}
