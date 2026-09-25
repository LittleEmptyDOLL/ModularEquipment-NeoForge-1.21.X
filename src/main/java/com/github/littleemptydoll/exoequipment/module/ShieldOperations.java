package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ShieldOperations {
    private ShieldOperations() {}

    public static ShieldDamageResult absorbDamage(
            double damage,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (damage <= 0.0D) {
            return new ShieldDamageResult(
                    0.0D,
                    0.0D,
                    data,
                    false
            );
        }

        List<ShieldTarget> targets =
                collectShields(data);

        if (targets.isEmpty()) {
            return new ShieldDamageResult(
                    damage,
                    0.0D,
                    data,
                    false
            );
        }

        ExoskeletonData updatedData = data;
        double remaining = damage;
        double absorbedDamage = 0.0D;

        for (ShieldTarget target : targets) {
            if (remaining <= 0.0D) {
                break;
            }

            InstalledModule module =
                    getRequiredModule(
                            updatedData,
                            target.reference()
                    );

            if (module.shieldEnergy() <= 0.0D
                    || !isPowered(
                            target,
                            poweredModules
                    )) {
                continue;
            }

            double absorbed =
                    Math.min(
                            remaining,
                            module.shieldEnergy()
                    );

            updatedData = ExoskeletonModules.update(
                    updatedData,
                    target.reference(),
                    module.withShieldEnergy(
                            module.shieldEnergy()
                                    - absorbed
                    )
            );

            remaining -= absorbed;
            absorbedDamage += absorbed;
        }

        // Emergency Shield is triggered only when all active shields have
        // been depleted. A single destroyed shield is not enough while
        // another shield still has energy available.
        ExoskeletonData finalUpdatedData =
                updatedData;

        boolean shieldDestroyed =
                targets.stream()
                        .allMatch(target ->
                                getRequiredModule(
                                        finalUpdatedData,
                                        target.reference()
                                ).shieldEnergy() <= 0.0D
                        );

        // Any incoming damage resets the recharge cooldown of every active
        // shield, including depleted shields.
        for (ShieldTarget target : targets) {
            InstalledModule module =
                    getRequiredModule(
                            updatedData,
                            target.reference()
                    );

            updatedData = ExoskeletonModules.update(
                    updatedData,
                    target.reference(),
                    module.withShieldRechargeCooldown(
                            target.properties()
                                    .rechargeDelay()
                    )
            );
        }

        return new ShieldDamageResult(
                Math.max(0.0D, remaining),
                absorbedDamage,
                updatedData,
                shieldDestroyed
        );
    }

    public static ShieldStatus getStatus(
            ExoskeletonData data
    ) {
        return getStatus(
                data,
                collectShields(data)
        );
    }

    public static ShieldStatus getStatus(
            ExoskeletonData data,
            int matrixSlot
    ) {
        return getStatus(
                data,
                collectShields(
                        ExoskeletonModules.supportedInMatrix(
                                data,
                                matrixSlot
                        )
                )
        );
    }

    private static ShieldStatus getStatus(
            ExoskeletonData data,
            List<ShieldTarget> targets
    ) {
        if (targets.isEmpty()) {
            return ShieldStatus.empty();
        }

        double currentEnergy = 0.0D;
        int capacity = 0;

        for (ShieldTarget target : targets) {
            InstalledModule module =
                    getRequiredModule(
                            data,
                            target.reference()
                    );

            double scaledCapacity =
                    scaledCapacity(target, data);

            currentEnergy += Math.min(
                    module.shieldEnergy(),
                    scaledCapacity
            );

            capacity +=
                    (int) Math.round(scaledCapacity);
        }

        return new ShieldStatus(
                currentEnergy,
                capacity
        );
    }

    public static ExoskeletonData tick(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<ShieldTarget> targets =
                collectShields(data);

        ExoskeletonData updatedData = data;

        for (ShieldTarget target : targets) {
            InstalledModule module =
                    getRequiredModule(
                            updatedData,
                            target.reference()
                    );

            if (!isPowered(
                    target,
                    poweredModules
            )) {
                continue;
            }

            if (module.shieldRechargeCooldown() > 0) {
                updatedData = ExoskeletonModules.update(
                        updatedData,
                        target.reference(),
                        module.withShieldRechargeCooldown(
                                module.shieldRechargeCooldown()
                                        - 1
                        )
                );
                continue;
            }

            double recharged = Math.min(
                    scaledCapacity(
                            target,
                            updatedData
                    ),
                    module.shieldEnergy()
                            + target.properties()
                            .rechargeRate()
                            * efficiency(
                                    target,
                                    updatedData
                            )
            );

            if (recharged != module.shieldEnergy()) {
                updatedData = ExoskeletonModules.update(
                        updatedData,
                        target.reference(),
                        module.withShieldEnergy(recharged)
                );
            }
        }

        return updatedData;
    }

    private static List<ShieldTarget> collectShields(
            ExoskeletonData data
    ) {
        return collectShields(
                ExoskeletonModules.activeSupported(data)
        );
    }

    private static List<ShieldTarget> collectShields(
            List<ExoskeletonModules.ActiveModule> modules
    ) {
        List<ShieldTarget> targets =
                new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : modules) {

            activeModule.definition()
                    .shield()
                    .ifPresent(properties ->
                            targets.add(
                                    new ShieldTarget(
                                            activeModule.reference(),
                                            activeModule.definition(),
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

    private static double efficiency(
            ShieldTarget target,
            ExoskeletonData data
    ) {
        return TemperatureOperations
                .calculateModuleEfficiency(
                        target.definition(),
                        data.temperature()
                );
    }

    private static double scaledCapacity(
            ShieldTarget target,
            ExoskeletonData data
    ) {
        return Math.max(
                0.0D,
                target.properties().capacity()
                        * efficiency(target, data)
        );
    }

    private static boolean isPowered(
            ShieldTarget target,
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

    public record ShieldStatus(
            double currentEnergy,
            int capacity
    ) {
        public ShieldStatus {
            if (!Double.isFinite(currentEnergy)
                    || currentEnergy < 0.0D) {
                throw new IllegalArgumentException(
                        "Shield current energy must be finite and non-negative"
                );
            }

            if (capacity < 0) {
                throw new IllegalArgumentException(
                        "Shield capacity cannot be negative"
                );
            }
        }

        public static ShieldStatus empty() {
            return new ShieldStatus(0.0D, 0);
        }

        public boolean hasShields() {
            return capacity > 0;
        }
    }

    public record ShieldDamageResult(
            double remainingDamage,
            double absorbedDamage,
            ExoskeletonData data,
            boolean shieldDestroyed
    ) {}

    private record ShieldTarget(
            InstalledModuleReference reference,
            ModuleDefinition definition,
            ShieldProperties properties
    ) {}
}
