package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class FlightOperations {
    private FlightOperations() {}

    public static ActivationResult activate(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        for (Target target : collectTargets(data)) {
            if (target.module().flightActive() || !isPowered(target, poweredModules)) {
                continue;
            }

            return new ActivationResult(
                    replaceModule(data, target.reference(), target.module().withFlightActive(true)),
                    true
            );
        }

        return new ActivationResult(data, false);
    }

    public static ExoskeletonData tick(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        ExoskeletonData updated = data;

        for (Target target : collectTargets(updated)) {
            InstalledModule module = getModule(updated, target.reference());
            if (module == null || !module.flightActive()) {
                continue;
            }

            if (!isPowered(target.withModule(module), poweredModules)) {
                updated = replaceModule(
                        updated,
                        target.reference(),
                        module.withFlightActive(false)
                );
            }
        }

        return updated;
    }

    public static ExoskeletonData deactivate(
            ExoskeletonData data,
            InstalledModuleReference reference
    ) {
        InstalledModule module = getModule(data, reference);
        if (module == null || !module.flightActive()) {
            return data;
        }

        return replaceModule(
                data,
                reference,
                module.withFlightActive(false)
        );
    }

    public static boolean hasActive(ExoskeletonData data) {
        return collectTargets(data).stream().anyMatch(target -> target.module().flightActive());
    }

    private static boolean isPowered(
            Target target,
            Set<InstalledModuleReference> poweredModules
    ) {
        var definition = ModModules.getDefinition(target.module().id());

        if (!target.module().flightActive()) {
            return definition.energy()
                    .map(EnergyProperties::consumption)
                    .map(consumption -> consumption <= 0 || poweredModules.contains(target.reference()))
                    .orElse(true);
        }

        int activeConsumption = target.properties().activeConsumption();
        int passiveConsumption = definition.energy()
                .map(EnergyProperties::consumption)
                .orElse(0);

        if (passiveConsumption <= 0 && activeConsumption <= 0) {
            return true;
        }

        return poweredModules.contains(target.reference());
    }

    private static List<Target> collectTargets(ExoskeletonData data) {
        List<Target> targets = new ArrayList<>();

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            for (int index = 0; index < matrix.modules().size(); index++) {
                InstalledModule module = matrix.modules().get(index);
                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                FlightProperties properties = ModModules.getDefinition(module.id())
                        .flight()
                        .orElse(null);
                if (properties == null) {
                    continue;
                }

                targets.add(new Target(
                        new InstalledModuleReference(slot, index),
                        module,
                        properties
                ));
            }
        }

        targets.sort(
                Comparator.comparingInt((Target target) -> target.reference().matrixSlot())
                        .thenComparingInt(target -> target.reference().moduleIndex())
        );
        return targets;
    }

    private static InstalledModule getModule(
            ExoskeletonData data,
            InstalledModuleReference reference
    ) {
        if (reference.matrixSlot() < 0 || reference.matrixSlot() >= data.matrices().size()) {
            return null;
        }

        MatrixData matrix = data.matrices().get(reference.matrixSlot()).matrix().orElse(null);
        if (matrix == null
                || reference.moduleIndex() < 0
                || reference.moduleIndex() >= matrix.modules().size()) {
            return null;
        }

        return matrix.modules().get(reference.moduleIndex());
    }

    private static ExoskeletonData replaceModule(
            ExoskeletonData data,
            InstalledModuleReference reference,
            InstalledModule module
    ) {
        MatrixData matrix = data.matrices().get(reference.matrixSlot()).matrix().orElseThrow();
        List<InstalledModule> modules = new ArrayList<>(matrix.modules());
        modules.set(reference.moduleIndex(), module);
        return data.withMatrix(
                reference.matrixSlot(),
                new MatrixData(matrix.id(), modules)
        );
    }

    public record ActivationResult(
            ExoskeletonData data,
            boolean activated
    ) {}

    private record Target(
            InstalledModuleReference reference,
            InstalledModule module,
            FlightProperties properties
    ) {
        private Target withModule(InstalledModule module) {
            return new Target(reference, module, properties);
        }
    }
}
