package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ShieldOperations {
    private ShieldOperations() {}

    public static ShieldDamageResult absorbDamage(
            double damage,
            ExoskeletonData data,
            ExoskeletonRuntimeState runtime
    ) {
        if (damage <= 0.0D) {
            return new ShieldDamageResult(0.0D, 0.0D, runtime);
        }

        List<ShieldTarget> targets = collectShields(data);
        if (targets.isEmpty()) {
            return new ShieldDamageResult(damage, 0.0D, runtime);
        }

        List<ShieldState> states = normalizeStates(targets, runtime.shields());
        double remaining = damage;
        double absorbedDamage = 0.0D;

        for (ShieldTarget target : targets) {
            if (remaining <= 0.0D) {
                break;
            }

            ShieldState state = findState(states, target.reference());

            if (state.currentEnergy() <= 0.0D
                    || !isPowered(target, runtime.poweredModules())) {
                continue;
            }

            double absorbed = Math.min(remaining, state.currentEnergy());

            states = replaceState(
                    states,
                    state.withCurrentEnergy(
                            state.currentEnergy() - absorbed
                    )
            );

            remaining -= absorbed;
            absorbedDamage += absorbed;
        }

        // Any incoming damage resets the recharge cooldown of every active
        // shield. This also applies when all shields are already depleted,
        // preventing them from immediately starting to recharge while the
        // player is still taking damage.
        states = resetRechargeCooldowns(states, targets);

        return new ShieldDamageResult(
                Math.max(0.0D, remaining),
                absorbedDamage,
                runtime.withShields(states)
        );
    }

    public static ShieldStatus getStatus(
            ExoskeletonData data,
            ExoskeletonRuntimeState runtime
    ) {
        List<ShieldTarget> targets = collectShields(data);

        if (targets.isEmpty()) {
            return ShieldStatus.empty();
        }

        List<ShieldState> states = normalizeStates(targets, runtime.shields());
        double currentEnergy = 0.0D;
        int capacity = 0;

        for (ShieldTarget target : targets) {
            ShieldState state = findState(states, target.reference());
            currentEnergy += state.currentEnergy();
            capacity += target.properties().capacity();
        }

        return new ShieldStatus(currentEnergy, capacity);
    }

    public static ExoskeletonRuntimeState tick(
            ExoskeletonData data,
            ExoskeletonRuntimeState runtime
    ) {
        List<ShieldTarget> targets = collectShields(data);

        if (targets.isEmpty()) {
            return runtime.withShields(List.of());
        }

        List<ShieldState> states = normalizeStates(targets, runtime.shields());

        for (ShieldTarget target : targets) {
            ShieldState state = findState(states, target.reference());

            if (!isPowered(target, runtime.poweredModules())) {
                continue;
            }

            if (state.rechargeCooldown() > 0) {
                states = replaceState(
                        states,
                        state.withRechargeCooldown(
                                state.rechargeCooldown() - 1
                        )
                );
                continue;
            }

            double recharged = Math.min(
                    target.properties().capacity(),
                    state.currentEnergy() + target.properties().rechargeRate()
            );

            if (recharged != state.currentEnergy()) {
                states = replaceState(
                        states,
                        state.withCurrentEnergy(recharged)
                );
            }
        }

        return runtime.withShields(states);
    }

    private static List<ShieldState> resetRechargeCooldowns(
            List<ShieldState> states,
            List<ShieldTarget> targets
    ) {
        List<ShieldState> result = new ArrayList<>(states);

        for (ShieldTarget target : targets) {
            ShieldState state = findState(result, target.reference());

            result = new ArrayList<>(
                    replaceState(
                            result,
                            state.withRechargeCooldown(
                                    target.properties().rechargeDelay()
                            )
                    )
            );
        }

        return List.copyOf(result);
    }

    private static List<ShieldTarget> collectShields(ExoskeletonData data) {
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
            ShieldTarget target,
            Set<InstalledModuleReference> poweredModules
    ) {
        var energy = ModModules.getDefinition(target.moduleId()).energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(target.reference());
    }

    private static List<ShieldState> normalizeStates(
            List<ShieldTarget> targets,
            List<ShieldState> existing
    ) {
        List<ShieldState> result = new ArrayList<>();

        for (ShieldTarget target : targets) {
            ShieldState current = null;

            for (ShieldState state : existing) {
                if (state.reference().equals(target.reference())) {
                    current = state;
                    break;
                }
            }

            if (current == null) {
                current = new ShieldState(
                        target.reference(),
                        target.properties().capacity(),
                        0
                );
            }

            result.add(
                    new ShieldState(
                            current.reference(),
                            Math.min(
                                    current.currentEnergy(),
                                    target.properties().capacity()
                            ),
                            current.rechargeCooldown()
                    )
            );
        }

        return List.copyOf(result);
    }

    private static ShieldState findState(
            List<ShieldState> states,
            InstalledModuleReference reference
    ) {
        for (ShieldState state : states) {
            if (state.reference().equals(reference)) {
                return state;
            }
        }

        throw new IllegalStateException(
                "Shield state is missing for " + reference
        );
    }

    private static List<ShieldState> replaceState(
            List<ShieldState> states,
            ShieldState replacement
    ) {
        List<ShieldState> result = new ArrayList<>(states);

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).reference().equals(replacement.reference())) {
                result.set(i, replacement);
                return List.copyOf(result);
            }
        }

        result.add(replacement);
        return List.copyOf(result);
    }

    private record ShieldTarget(
            InstalledModuleReference reference,
            net.minecraft.resources.ResourceLocation moduleId,
            ShieldProperties properties
    ) {}

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
            ExoskeletonRuntimeState runtime
    ) {}
}
