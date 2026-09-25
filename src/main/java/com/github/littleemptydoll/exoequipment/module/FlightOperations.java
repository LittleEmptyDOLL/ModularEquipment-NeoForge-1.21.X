package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;

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
            if (target.module().flightActive()
                    || !isPowered(target, poweredModules)) {
                continue;
            }

            return new ActivationResult(
                    ExoskeletonModules.update(
                            data,
                            target.reference(),
                            target.module().withFlightActive(true)
                    ),
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
            InstalledModule module =
                    ExoskeletonModules.get(
                            updated,
                            target.reference()
                    ).orElse(null);

            if (module == null || !module.flightActive()) {
                continue;
            }

            if (!isPowered(
                    target.withModule(module),
                    poweredModules
            )) {
                updated = ExoskeletonModules.update(
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
        InstalledModule module =
                ExoskeletonModules.get(data, reference)
                        .orElse(null);

        if (module == null || !module.flightActive()) {
            return data;
        }

        return ExoskeletonModules.update(
                data,
                reference,
                module.withFlightActive(false)
        );
    }

    public static boolean hasActive(ExoskeletonData data) {
        return collectTargets(data).stream()
                .anyMatch(target ->
                        target.module().flightActive()
                );
    }

    private static boolean isPowered(
            Target target,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (!target.module().flightActive()) {
            return ExoskeletonModules.isPowered(
                    target.definition(),
                    target.reference(),
                    poweredModules
            );
        }

        int activeConsumption =
                target.properties().activeConsumption();

        int passiveConsumption =
                target.definition()
                        .energy()
                        .map(EnergyProperties::consumption)
                        .orElse(0);

        if (passiveConsumption <= 0
                && activeConsumption <= 0) {
            return true;
        }

        return poweredModules.contains(target.reference());
    }

    private static List<Target> collectTargets(
            ExoskeletonData data
    ) {
        List<Target> targets =
                ExoskeletonModules.activeSupported(data)
                        .stream()
                        .filter(activeModule ->
                                activeModule.definition()
                                        .flight()
                                        .isPresent()
                        )
                        .map(activeModule ->
                                new Target(
                                        activeModule.reference(),
                                        activeModule.module(),
                                        activeModule.definition(),
                                        activeModule.definition()
                                                .flight()
                                                .orElseThrow()
                                )
                        )
                        .sorted(
                                Comparator.comparingInt(
                                                (Target target) ->
                                                        target.reference()
                                                                .matrixSlot()
                                        )
                                        .thenComparingInt(
                                                target ->
                                                        target.reference()
                                                                .moduleIndex()
                                        )
                        )
                        .toList();

        return List.copyOf(targets);
    }

    public record ActivationResult(
            ExoskeletonData data,
            boolean activated
    ) {}

    private record Target(
            InstalledModuleReference reference,
            InstalledModule module,
            ModuleDefinition definition,
            FlightProperties properties
    ) {
        private Target withModule(InstalledModule module) {
            return new Target(
                    reference,
                    module,
                    definition,
                    properties
            );
        }
    }
}
