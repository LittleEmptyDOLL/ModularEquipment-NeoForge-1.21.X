package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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
            return new ShieldDamageResult(0.0D, 0.0D, data, false);
        }

        List<ShieldTarget> targets = collectShields(data);

        if (targets.isEmpty()) {
            return new ShieldDamageResult(damage, 0.0D, data, false);
        }

        ExoskeletonData updatedData = data;
        double remaining = damage;
        double absorbedDamage = 0.0D;

        for (ShieldTarget target : targets) {
            if (remaining <= 0.0D) {
                break;
            }

            InstalledModule module = getModule(updatedData, target.reference());

            if (module.shieldEnergy() <= 0.0D
                    || !isPowered(target, poweredModules)) {
                continue;
            }

            double absorbed = Math.min(
                    remaining,
                    module.shieldEnergy()
            );

            updatedData = updateModule(
                    updatedData,
                    target.reference(),
                    module.withShieldEnergy(
                            module.shieldEnergy() - absorbed
                    )
            );

            remaining -= absorbed;
            absorbedDamage += absorbed;
        }

        // Emergency Shield is triggered only when all active shields have
        // been depleted. A single destroyed shield is not enough while
        // another shield still has energy available.
        ExoskeletonData finalUpdatedData = updatedData;
        boolean shieldDestroyed = targets.stream()
                .allMatch(target ->
                        getModule(finalUpdatedData, target.reference()).shieldEnergy() <= 0.0D
                );

        // Any incoming damage resets the recharge cooldown of every active
        // shield, including depleted shields.
        for (ShieldTarget target : targets) {
            InstalledModule module = getModule(updatedData, target.reference());

            updatedData = updateModule(
                    updatedData,
                    target.reference(),
                    module.withShieldRechargeCooldown(
                            target.properties().rechargeDelay()
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

    public static ShieldStatus getStatus(ExoskeletonData data) {
        List<ShieldTarget> targets = collectShields(data);

        if (targets.isEmpty()) {
            return ShieldStatus.empty();
        }

        double currentEnergy = 0.0D;
        int capacity = 0;

        for (ShieldTarget target : targets) {
            InstalledModule module = getModule(data, target.reference());

            currentEnergy += Math.min(
                    module.shieldEnergy(),
                    scaledCapacity(target, data)
            );
            capacity += (int) Math.round(scaledCapacity(target, data));
        }

        return new ShieldStatus(currentEnergy, capacity);
    }

    public static ShieldStatus getStatus(
            ExoskeletonData data,
            int matrixSlot
    ) {
        List<ShieldTarget> targets = collectShields(data, Set.of(matrixSlot));
        if (targets.isEmpty()) return ShieldStatus.empty();

        double currentEnergy = 0.0D;
        int capacity = 0;
        for (ShieldTarget target : targets) {
            InstalledModule module = getModule(data, target.reference());
            currentEnergy += Math.min(module.shieldEnergy(), scaledCapacity(target, data));
            capacity += (int) Math.round(scaledCapacity(target, data));
        }
        return new ShieldStatus(currentEnergy, capacity);
    }

    public static ExoskeletonData tick(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<ShieldTarget> targets = collectShields(data);
        ExoskeletonData updatedData = data;

        for (ShieldTarget target : targets) {
            InstalledModule module = getModule(updatedData, target.reference());

            if (!isPowered(target, poweredModules)) {
                continue;
            }

            if (module.shieldRechargeCooldown() > 0) {
                updatedData = updateModule(
                        updatedData,
                        target.reference(),
                        module.withShieldRechargeCooldown(
                                module.shieldRechargeCooldown() - 1
                        )
                );
                continue;
            }

            double recharged = Math.min(
                    scaledCapacity(target, updatedData),
                    module.shieldEnergy()
                            + target.properties().rechargeRate() * efficiency(target, updatedData)
            );

            if (recharged != module.shieldEnergy()) {
                updatedData = updateModule(
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
                data,
                new HashSet<>(ExoskeletonState.activeMatrixSlots(data))
        );
    }

    private static List<ShieldTarget> collectShields(
            ExoskeletonData data,
            Set<Integer> matrixSlots
    ) {
        List<ShieldTarget> targets = new ArrayList<>();

        for (int slot : matrixSlots) {
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

    private static double efficiency(ShieldTarget target, ExoskeletonData data) {
        return com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations.calculateModuleEfficiency(target.moduleId(), data.temperature());
    }

    private static double scaledCapacity(ShieldTarget target, ExoskeletonData data) {
        return Math.max(0.0D, target.properties().capacity() * efficiency(target, data));
    }

    private static boolean isPowered(
            ShieldTarget target,
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

    public record ShieldStatus(
            double currentEnergy,
            int capacity
    ) {
        public ShieldStatus {
            if (!Double.isFinite(currentEnergy) || currentEnergy < 0.0D) {
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
            net.minecraft.resources.ResourceLocation moduleId,
            ShieldProperties properties
    ) {}
}
