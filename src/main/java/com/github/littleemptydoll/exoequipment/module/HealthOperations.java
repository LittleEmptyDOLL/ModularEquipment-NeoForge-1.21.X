package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class HealthOperations {
    private HealthOperations() {}

    public static double calculateAdditionalHealth(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double additionalHealth = 0.0D;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix = data.matrices()
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

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (!isPowered(module.id(), reference, poweredModules)) {
                    continue;
                }

                var definition = ModModules.getDefinition(module.id());

                additionalHealth += definition.health()
                        .map(HealthProperties::additionalHealth)
                        .orElse(0.0D);
            }
        }

        return additionalHealth;
    }

    private static boolean isPowered(
            ResourceLocation moduleId,
            InstalledModuleReference reference,
            Set<InstalledModuleReference> poweredModules
    ) {
        var energy = ModModules.getDefinition(moduleId).energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(reference);
    }
}
