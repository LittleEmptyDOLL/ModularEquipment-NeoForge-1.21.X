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
            return new ShieldDamageResult(0.0D, runtime);
        }

        List<ShieldTarget> targets = collectShields(data);
        if (targets.isEmpty()) {
            return new ShieldDamageResult(damage, runtime);
        }

        List<ShieldState> states = normalizeStates(targets, runtime.shields());
        double remaining = damage;

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
                    new ShieldState(
                            state.reference(),
                            state.currentEnergy() - absorbed,
                            target.properties().rechargeDelay()
                    )
            );

            remaining -= absorbed;
        }

        return new ShieldDamageResult(
                Math.max(0.0D, remaining),
                runtime.withShields(states)
        );
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

                ModModules.getDefinition(module.id())
                        .shield()
                        .ifPresent(properties ->
                                targets.add(
                                        new ShieldTarget(
                                                new InstalledModuleReference(
                                                        slot,
                                                        moduleIndex
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

    public record ShieldDamageResult(
            double remainingDamage,
            ExoskeletonRuntimeState runtime
    ) {}
}
