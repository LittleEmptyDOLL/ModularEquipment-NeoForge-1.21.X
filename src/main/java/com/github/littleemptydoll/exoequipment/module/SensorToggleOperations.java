package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;

import java.util.ArrayList;
import java.util.List;

public final class SensorToggleOperations {
    private SensorToggleOperations() {}

    public enum Kind { ENTITY, BLOCK, THERMAL }

    public static ExoskeletonData toggle(ExoskeletonData data, Kind kind) {
        List<ExoskeletonModules.ActiveModule> targets = new ArrayList<>();
        for (int slot = 0; slot < data.matrices().size(); slot++) {
            for (var module : ExoskeletonModules.supportedInMatrix(data, slot)) {
                if (matches(module.definition(), kind)) {
                    targets.add(module);
                }
            }
        }

        if (targets.isEmpty()) {
            return data;
        }

        boolean enable = targets.stream().noneMatch(module -> module.module().active());
        ExoskeletonData updated = data;
        for (var target : targets) {
            updated = ExoskeletonModules.update(
                    updated,
                    target.reference(),
                    target.module().withActive(enable)
            );
        }
        return updated;
    }

    public static boolean isActive(ExoskeletonData data, Kind kind) {
        for (int slot = 0; slot < data.matrices().size(); slot++) {
            for (var module : ExoskeletonModules.supportedInMatrix(data, slot)) {
                if (matches(module.definition(), kind) && module.module().active()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matches(ModuleDefinition definition, Kind kind) {
        return switch (kind) {
            case ENTITY -> definition.entityDetection().isPresent();
            case BLOCK -> definition.blockScanner().isPresent();
            case THERMAL -> definition.thermalVision().isPresent();
        };
    }
}
