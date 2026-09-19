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

public final class RevivalOperations {
    private RevivalOperations() {}

    public static ExoskeletonData tick(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        ExoskeletonData updatedData = data;

        for (RevivalTarget target : collect(data)) {
            InstalledModule module = getModule(updatedData, target.reference());

            if (!isPowered(target, poweredModules)
                    || module.revivalCooldown() <= 0) {
                continue;
            }

            updatedData = updateModule(
                    updatedData,
                    target.reference(),
                    module.withRevivalCooldown(
                            module.revivalCooldown() - 1
                    )
            );
        }

        return updatedData;
    }

    public static RevivalResult tryRevive(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        for (RevivalTarget target : collect(data)) {
            InstalledModule module = getModule(data, target.reference());

            if (!isPowered(target, poweredModules)
                    || module.revivalCooldown() > 0) {
                continue;
            }

            ExoskeletonData updatedData = updateModule(
                    data,
                    target.reference(),
                    module.withRevivalCooldown(
                            target.properties().cooldown()
                    )
            );

            return new RevivalResult(
                    true,
                    target.properties(),
                    updatedData
            );
        }

        return new RevivalResult(
                false,
                null,
                data
        );
    }

    private static List<RevivalTarget> collect(
            ExoskeletonData data
    ) {
        List<RevivalTarget> targets = new ArrayList<>();

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
                        .revival()
                        .ifPresent(properties ->
                                targets.add(
                                        new RevivalTarget(
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
                        .comparingInt((RevivalTarget target) ->
                                target.reference().matrixSlot())
                        .thenComparingInt(target ->
                                target.reference().moduleIndex())
        );

        return targets;
    }

    private static boolean isPowered(
            RevivalTarget target,
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

    public record RevivalResult(
            boolean revived,
            RevivalProperties properties,
            ExoskeletonData data
    ) {}

    private record RevivalTarget(
            InstalledModuleReference reference,
            net.minecraft.resources.ResourceLocation moduleId,
            RevivalProperties properties
    ) {}
}
