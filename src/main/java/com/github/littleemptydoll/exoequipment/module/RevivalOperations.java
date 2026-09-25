package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;

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
            InstalledModule module =
                    getRequiredModule(
                            updatedData,
                            target.reference()
                    );

            if (!isPowered(target, poweredModules)
                    || module.revivalCooldown() <= 0) {
                continue;
            }

            updatedData = ExoskeletonModules.update(
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
            InstalledModule module =
                    getRequiredModule(
                            data,
                            target.reference()
                    );

            if (!isPowered(target, poweredModules)
                    || module.revivalCooldown() > 0) {
                continue;
            }

            ExoskeletonData updatedData =
                    ExoskeletonModules.update(
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

    public static int readyCount(ExoskeletonData data) {
        int count = 0;

        for (RevivalTarget target : collect(data)) {
            if (getRequiredModule(
                    data,
                    target.reference()
            ).revivalCooldown() <= 0) {
                count++;
            }
        }

        return count;
    }

    private static List<RevivalTarget> collect(
            ExoskeletonData data
    ) {
        List<RevivalTarget> targets = new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            activeModule.definition()
                    .revival()
                    .ifPresent(properties ->
                            targets.add(
                                    new RevivalTarget(
                                            activeModule.reference(),
                                            activeModule.definition(),
                                            properties
                                    )
                            )
                    );
        }

        targets.sort(
                Comparator.comparingInt(
                                (RevivalTarget target) ->
                                        target.reference()
                                                .matrixSlot()
                        )
                        .thenComparingInt(
                                target ->
                                        target.reference()
                                                .moduleIndex()
                        )
        );

        return targets;
    }

    private static boolean isPowered(
            RevivalTarget target,
            Set<InstalledModuleReference> poweredModules
    ) {
        return ExoskeletonModules.isPowered(
                target.definition(),
                target.reference(),
                poweredModules
        );
    }

    private static InstalledModule getRequiredModule(
            ExoskeletonData data,
            InstalledModuleReference reference
    ) {
        return ExoskeletonModules.get(data, reference)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Module is missing for "
                                        + reference
                        )
                );
    }

    public record RevivalResult(
            boolean revived,
            RevivalProperties properties,
            ExoskeletonData data
    ) {}

    private record RevivalTarget(
            InstalledModuleReference reference,
            ModuleDefinition definition,
            RevivalProperties properties
    ) {}
}
