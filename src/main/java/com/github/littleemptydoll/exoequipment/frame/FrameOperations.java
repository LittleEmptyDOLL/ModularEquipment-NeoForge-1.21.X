package com.github.littleemptydoll.exoequipment.frame;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.registry.ModFrames;

public final class FrameOperations {
    private FrameOperations() {}

    public static boolean isModuleSupported(ExoskeletonData data, InstalledModule module) {
        if (data.frame().isEmpty()) {
            return false;
        }

        FrameDefinition definition = ModFrames.getDefinition(data.frame().get().definitionId());
        ModuleSize moduleSize = MatrixOperations.getRotatedSize(
                com.github.littleemptydoll.exoequipment.registry.ModModules
                        .getDefinition(module.id())
                        .size(),
                module.rotation()
        );

        ModuleSize maxSize = definition.maxModuleSize();

        return moduleSize.width() <= maxSize.width()
                && moduleSize.height() <= maxSize.height();
    }

    public static boolean hasOversizedModule(ExoskeletonData data) {
        if (data.frame().isEmpty()) {
            return false;
        }

        return data.matrices().stream()
                .flatMap(slot -> slot.matrix().stream())
                .flatMap(matrix -> matrix.modules().stream())
                .anyMatch(module -> !isModuleSupported(data, module));
    }
}
