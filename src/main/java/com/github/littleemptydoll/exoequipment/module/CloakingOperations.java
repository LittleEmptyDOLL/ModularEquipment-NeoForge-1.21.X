package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.energy.EnergyOperations;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class CloakingOperations {
    private CloakingOperations() {}

    public static ActivationResult activate(
            ExoskeletonData data
    ) {
        List<Target> targets = collectTargets(data);

        for (Target target : targets) {
            InstalledModule module = target.module();
            CloakingProperties properties = target.properties();

            if (module.active() || module.abilityCooldown() > 0) {
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

            ExoskeletonData updated = replaceModule(
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
            InstalledModule module = getModule(updated, target.reference());
            if (module == null) {
                continue;
            }

            int cooldown = Math.max(0, module.abilityCooldown() - 1);
            if (cooldown != module.abilityCooldown()) {
                module = module.withAbilityCooldown(cooldown);
            }

            if (module.active() && !poweredModules.contains(target.reference())) {
                module = module
                        .withActive(false)
                        .withAbilityCooldown(target.properties().cooldown());
            }

            if (!module.equals(getModule(updated, target.reference()))) {
                updated = replaceModule(updated, target.reference(), module);
            }
        }

        return updated;
    }

    public static ExoskeletonData deactivate(
            ExoskeletonData data,
            InstalledModuleReference reference
    ) {
        InstalledModule module = getModule(data, reference);
        if (module == null || !module.active()) {
            return data;
        }

        CloakingProperties properties = ModModules.getDefinition(module.id())
                .cloaking()
                .orElse(null);
        if (properties == null) {
            return data;
        }

        return replaceModule(
                data,
                reference,
                module
                        .withActive(false)
                        .withAbilityCooldown(properties.cooldown())
        );
    }

    public static boolean hasActive(ExoskeletonData data) {
        return !collectTargets(data).stream()
                .filter(target -> target.module().active())
                .toList()
                .isEmpty();
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
                if (!com.github.littleemptydoll.exoequipment.frame.FrameOperations
                        .isModuleSupported(data, module)) {
                    continue;
                }

                CloakingProperties properties = ModModules.getDefinition(module.id())
                        .cloaking()
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
        if (reference.matrixSlot() < 0
                || reference.matrixSlot() >= data.matrices().size()) {
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
            CloakingProperties properties
    ) {}
}
