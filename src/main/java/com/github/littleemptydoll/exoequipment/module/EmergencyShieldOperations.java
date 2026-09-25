package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;

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
                        .comparingDouble(
                                (EmergencyShieldTarget target) ->
                                        target.properties().restore()
                        )
                        .reversed()
                        .thenComparingInt(
                                target ->
                                        target.reference()
                                                .matrixSlot()
                        )
                        .thenComparingInt(
                                target ->
                                        target.reference()
                                                .moduleIndex()
                        )
        );

        EmergencyShieldTarget selected = null;

        for (EmergencyShieldTarget target : emergencyShields) {
            InstalledModule module =
                    getRequiredModule(
                            data,
                            target.reference()
                    );

            if (module.emergencyShieldCooldown() > 0
                    || !isPowered(
                            target,
                            poweredModules
                    )) {
                continue;
            }

            selected = target;
            break;
        }

        if (selected == null) {
            return new EmergencyShieldResult(
                    data,
                    false
            );
        }

        double restore =
                selected.properties().restore();

        ExoskeletonData updatedData = data;

        for (ShieldTarget target : collectShields(data)) {
            InstalledModule module =
                    getRequiredModule(
                            updatedData,
                            target.reference()
                    );

            double restoredEnergy =
                    target.properties().capacity()
                            * restore;

            updatedData = ExoskeletonModules.update(
                    updatedData,
                    target.reference(),
                    module.withShieldEnergy(restoredEnergy)
            );
        }

        InstalledModule emergencyModule =
                getRequiredModule(
                        updatedData,
                        selected.reference()
                );

        updatedData = ExoskeletonModules.update(
                updatedData,
                selected.reference(),
                emergencyModule
                        .withEmergencyShieldCooldown(
                                selected.properties().cooldown()
                        )
        );

        return new EmergencyShieldResult(
                updatedData,
                true
        );
    }

    public static ExoskeletonData tickCooldowns(
            ExoskeletonData data
    ) {
        ExoskeletonData updatedData = data;

        for (EmergencyShieldTarget target
                : collectEmergencyShields(data)) {

            InstalledModule module =
                    getRequiredModule(
                            updatedData,
                            target.reference()
                    );

            if (module.emergencyShieldCooldown() <= 0) {
                continue;
            }

            updatedData = ExoskeletonModules.update(
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

        for (EmergencyShieldTarget target
                : collectEmergencyShields(data)) {

            if (getRequiredModule(
                    data,
                    target.reference()
            ).emergencyShieldCooldown() <= 0) {
                count++;
            }
        }

        return count;
    }

    private static List<EmergencyShieldTarget>
    collectEmergencyShields(
            ExoskeletonData data
    ) {
        List<EmergencyShieldTarget> targets =
                new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            activeModule.definition()
                    .emergencyShield()
                    .ifPresent(properties ->
                            targets.add(
                                    new EmergencyShieldTarget(
                                            activeModule.reference(),
                                            activeModule.definition(),
                                            properties
                                    )
                            )
                    );
        }

        return targets;
    }

    private static List<ShieldTarget> collectShields(
            ExoskeletonData data
    ) {
        List<ShieldTarget> targets =
                new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            activeModule.definition()
                    .shield()
                    .ifPresent(properties ->
                            targets.add(
                                    new ShieldTarget(
                                            activeModule.reference(),
                                            properties
                                    )
                            )
                    );
        }

        targets.sort(
                Comparator.comparingInt(
                                (ShieldTarget target) ->
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
            EmergencyShieldTarget target,
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

    public record EmergencyShieldResult(
            ExoskeletonData data,
            boolean activated
    ) {}

    private record EmergencyShieldTarget(
            InstalledModuleReference reference,
            ModuleDefinition definition,
            EmergencyShieldProperties properties
    ) {}

    private record ShieldTarget(
            InstalledModuleReference reference,
            ShieldProperties properties
    ) {}
}
