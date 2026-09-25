package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.energy.EnergyOperations;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class CloakingOperations {
    private CloakingOperations() {}

    public static ActivationResult activate(
            ExoskeletonData data
    ) {
        for (Target target : collectTargets(data)) {
            InstalledModule module = target.module();
            CloakingProperties properties =
                    target.properties();

            if (module.active()
                    || module.abilityCooldown() > 0) {
                continue;
            }

            EnergyOperations.EnergyConsumptionResult energyResult =
                    EnergyOperations.consumeEnergy(
                            data,
                            properties.activationEnergy()
                    );

            if (!energyResult.sufficient()) {
                continue;
            }

            ExoskeletonData updated =
                    ExoskeletonModules.update(
                            energyResult.data(),
                            target.reference(),
                            module.withActive(true)
                    );

            return new ActivationResult(updated, true);
        }

        return new ActivationResult(data, false);
    }

    public static ExoskeletonData tick(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        ExoskeletonData updated = data;

        for (Target target : collectTargets(updated)) {
            InstalledModule original =
                    ExoskeletonModules.get(
                            updated,
                            target.reference()
                    ).orElse(null);

            if (original == null) {
                continue;
            }

            InstalledModule module = original;

            int cooldown =
                    Math.max(
                            0,
                            module.abilityCooldown() - 1
                    );

            if (cooldown != module.abilityCooldown()) {
                module =
                        module.withAbilityCooldown(cooldown);
            }

            if (module.active()
                    && !poweredModules.contains(
                            target.reference()
                    )) {
                module = module
                        .withActive(false)
                        .withAbilityCooldown(
                                target.properties().cooldown()
                        );
            }

            if (!module.equals(original)) {
                updated = ExoskeletonModules.update(
                        updated,
                        target.reference(),
                        module
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

        if (module == null || !module.active()) {
            return data;
        }

        CloakingProperties properties =
                ModModules.getDefinition(module.id())
                        .cloaking()
                        .orElse(null);

        if (properties == null) {
            return data;
        }

        return ExoskeletonModules.update(
                data,
                reference,
                module
                        .withActive(false)
                        .withAbilityCooldown(
                                properties.cooldown()
                        )
        );
    }

    public static boolean hasActive(
            ExoskeletonData data
    ) {
        return collectTargets(data)
                .stream()
                .anyMatch(target ->
                        target.module().active()
                );
    }

    private static List<Target> collectTargets(
            ExoskeletonData data
    ) {
        return ExoskeletonModules.activeSupported(data)
                .stream()
                .filter(activeModule ->
                        activeModule.definition()
                                .cloaking()
                                .isPresent()
                )
                .map(activeModule ->
                        new Target(
                                activeModule.reference(),
                                activeModule.module(),
                                activeModule.definition()
                                        .cloaking()
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
    }

    public record ActivationResult(
            ExoskeletonData data,
            boolean activated
    ) {}

    private record Target(
            InstalledModuleReference reference,
            InstalledModule module,
            CloakingProperties properties
    ) {}
}
