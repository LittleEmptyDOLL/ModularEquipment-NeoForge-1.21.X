package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;

import java.util.ArrayList;
import java.util.List;

public final class CombatToggleOperations {
    public enum Kind { LASER, DISCHARGE }

    private CombatToggleOperations() {}

    public static ExoskeletonData toggle(ExoskeletonData data, Kind kind) {
        List<ExoskeletonModules.ActiveModule> modules = new ArrayList<>();
        for (int slot = 0; slot < data.matrices().size(); slot++) {
            for (var module : ExoskeletonModules.supportedInMatrix(data, slot)) {
                if (matches(module.definition(), kind)) {
                    modules.add(module);
                }
            }
        }
        if (modules.isEmpty()) return data;

        boolean enable = modules.stream().noneMatch(module -> module.module().active());
        ExoskeletonData updated = data;
        for (var module : modules) {
            updated = ExoskeletonModules.update(updated, module.reference(),
                    module.module().withActive(enable));
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
        return kind == Kind.LASER
                ? definition.laserDefense().isPresent()
                : definition.dischargeDefense().isPresent();
    }
}
