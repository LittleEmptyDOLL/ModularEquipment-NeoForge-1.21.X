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

public final class EmergencyShieldOperations {
    private EmergencyShieldOperations() {}

    public static EmergencyShieldResult activate(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<EmergencyShieldTarget> emergencyShields =
                collectEmergencyShields(data);

        emergencyShields.sort(
                Comparator
                        .comparingDouble((EmergencyShieldTarget target) ->
                                target.properties().restore())
                        .reversed()
                        .thenComparingInt(target ->
                                target.reference().matrixSlot())
                        .thenComparingInt(target ->
                                target.reference().moduleIndex())
        );

        EmergencyShieldTarget selected = null;

        for (EmergencyShieldTarget target : emergencyShields) {
            InstalledModule module = getModule(data, target.reference());

            if (module.emergencyShieldCooldown() > 0
                    || !isPowered(target, poweredModules)) {
                continue;
            }

            selected = target;
            break;
        }

        if (selected == null) {
            return new EmergencyShieldResult(data, false);
        }

        double restore = selected.properties().restore();
        ExoskeletonData updatedData = data;

        for (ShieldTarget target : collectShields(data)) {
            InstalledModule module = getModule(updatedData, target.reference());
            double restoredEnergy =
                    target.properties().capacity() * restore;

            updatedData = updateModule(
                    updatedData,
                    target.reference(),
                    module.withShieldEnergy(restoredEnergy)
            );
        }

        InstalledModule emergencyModule =
                getModule(updatedData, selected.reference());

        updatedData = updateModule(
                updatedData,
                selected.reference(),
                emergencyModule.withEmergencyShieldCooldown(
                        selected.properties().cooldown()
                )
        );

        return new EmergencyShieldResult(updatedData, true);
    }

    public static ExoskeletonData tickCooldowns(ExoskeletonData data) {
        ExoskeletonData updatedData = data;

        for (EmergencyShieldTarget target : collectEmergencyShields(data)) {
            InstalledModule module = getModule(updatedData, target.reference());

            if (module.emergencyShieldCooldown() <= 0) {
                continue;
            }

            updatedData = updateModule(
                    updatedData,
                    target.reference(),
                    module.withEmergencyShieldCooldown(
                            module.emergencyShieldCooldown() - 1
                    )
            );
        }

        return updatedData;
    }

    public static int readyCount(ExoskeletonData data) {
        int count = 0;

        for (EmergencyShieldTarget target : collectEmergencyShields(data)) {
            if (getModule(data, target.reference()).emergencyShieldCooldown() <= 0) {
                count++;
            }
        }

        return count;
    }

    private static List<EmergencyShieldTarget> collectEmergencyShields(
            ExoskeletonData data
    ) {
        List<EmergencyShieldTarget> targets = new ArrayList<>();

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

                int finalModuleIndex = moduleIndex;
                ModModules.getDefinition(module.id())
                        .emergencyShield()
                        .ifPresent(properties ->
                                targets.add(
                                        new EmergencyShieldTarget(
                                                new InstalledModuleReference(
                                                        slot,
                                                        finalModuleIndex
                                                ),
                                                module.id(),
                                                properties
                                        )
                                )
                        );
            }
        }

        return targets;
    }

    private static List<ShieldTarget> collectShields(
            ExoskeletonData data
    ) {
        List<ShieldTarget> targets = new ArrayList<>();

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

                int finalModuleIndex = moduleIndex;
                ModModules.getDefinition(module.id())
                        .shield()
                        .ifPresent(properties ->
                                targets.add(
                                        new ShieldTarget(
                                                new InstalledModuleReference(
                                                        slot,
                                                        finalModuleIndex
                                                ),
                                                module.id(),
                                                properties
                                        )
                                )
                        );
            }
        }

        targets.sort(
                Comparator
                        .comparingInt((ShieldTarget target) ->
                                target.reference().matrixSlot())
                        .thenComparingInt(target ->
                                target.reference().moduleIndex())
        );

        return targets;
    }

    private static boolean isPowered(
            EmergencyShieldTarget target,
            Set<InstalledModuleReference> poweredModules
    ) {
        var energy = ModModules.getDefinition(target.moduleId()).energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(target.reference());
    }

    private static InstalledModule getModule(
            ExoskeletonData data,
            InstalledModuleReference reference
    ) {
        MatrixData matrix = data.matrices()
                .get(reference.matrixSlot())
                .matrix()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Matrix is missing for " + reference
                        )
                );

        if (reference.moduleIndex() >= matrix.modules().size()) {
            throw new IllegalStateException(
                    "Module is missing for " + reference
            );
        }

        return matrix.modules().get(reference.moduleIndex());
    }

    private static ExoskeletonData updateModule(
            ExoskeletonData data,
            InstalledModuleReference reference,
            InstalledModule module
    ) {
        MatrixData matrix = data.matrices()
                .get(reference.matrixSlot())
                .matrix()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Matrix is missing for " + reference
                        )
                );

        List<InstalledModule> modules = new ArrayList<>(matrix.modules());
        modules.set(reference.moduleIndex(), module);

        return data.withMatrix(
                reference.matrixSlot(),
                new MatrixData(matrix.id(), modules)
        );
    }

    public record EmergencyShieldResult(
            ExoskeletonData data,
            boolean activated
    ) {}

    private record EmergencyShieldTarget(
            InstalledModuleReference reference,
            net.minecraft.resources.ResourceLocation moduleId,
            EmergencyShieldProperties properties
    ) {}

    private record ShieldTarget(
            InstalledModuleReference reference,
            net.minecraft.resources.ResourceLocation moduleId,
            ShieldProperties properties
    ) {}
}
