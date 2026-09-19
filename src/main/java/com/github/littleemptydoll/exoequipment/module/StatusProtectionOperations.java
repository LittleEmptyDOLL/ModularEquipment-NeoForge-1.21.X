package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class StatusProtectionOperations {
    private StatusProtectionOperations() {}

    public static double calculateProtection(
            ExoskeletonData data,
            ResourceLocation effectId
    ) {
        return calculateProtection(data, effectId, null);
    }

    public static double calculateProtection(
            ExoskeletonData data,
            ResourceLocation effectId,
            Set<InstalledModuleReference> poweredModules
    ) {
        double remaining = 1.0D;

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

                var definition = ModModules.getDefinition(module.id());

                if (poweredModules != null
                        && definition.energy()
                        .filter(energy -> energy.consumption() > 0)
                        .isPresent()
                        && !poweredModules.contains(
                                new InstalledModuleReference(slot, moduleIndex)
                        )) {
                    continue;
                }

                StatusProtectionProperties properties =
                        definition.statusProtection().orElse(null);

                if (properties == null) {
                    continue;
                }

                double protection = properties.protection(effectId);
                remaining *= 1.0D - protection;

                if (remaining <= 0.0D) {
                    return 1.0D;
                }
            }
        }

        return Math.max(0.0D, Math.min(1.0D, 1.0D - remaining));
    }

    public static int applyProtection(
            int duration,
            ExoskeletonData data,
            ResourceLocation effectId,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (duration <= 0) {
            return 0;
        }

        double protection = calculateProtection(
                data,
                effectId,
                poweredModules
        );

        if (protection >= 1.0D) {
            return 0;
        }

        return Math.max(
                0,
                (int) Math.floor(duration * (1.0D - protection))
        );
    }
}
