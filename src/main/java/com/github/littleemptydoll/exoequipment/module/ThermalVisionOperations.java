package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;

import java.util.Set;

public final class ThermalVisionOperations {
    private ThermalVisionOperations() {}

    public static boolean enabled(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        return ExoskeletonModules.activeSupported(data).stream()
                .filter(module -> module.module().active()
                        && ExoskeletonModules.isPowered(module, poweredModules))
                .anyMatch(module -> module.definition().thermalVision().isPresent());
    }
}
