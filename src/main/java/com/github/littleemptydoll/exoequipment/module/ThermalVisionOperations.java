package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;

import java.util.Set;

public final class ThermalVisionOperations {
    private ThermalVisionOperations() {}

    public static double range(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        return ExoskeletonModules.activeSupported(data).stream()
                .filter(module -> module.module().active()
                        && ExoskeletonModules.isPowered(module, poweredModules))
                .flatMap(module -> module.definition().thermalVision().stream())
                .mapToDouble(ThermalVisionProperties::range)
                .max()
                .orElse(0.0D);
    }
}
