package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonRuntimeState;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RevivalOperations {
    private RevivalOperations() {}

    public static ExoskeletonRuntimeState tick(
            ExoskeletonData data,
            ExoskeletonRuntimeState runtime
    ) {
        Map<InstalledModuleReference, RevivalProperties> active = collect(
                data,
                runtime.poweredModules()
        );

        List<RevivalState> states = new ArrayList<>();

        for (RevivalState state : runtime.revivals()) {
            if (!active.containsKey(state.reference())) {
                continue;
            }

            states.add(state.tick());
        }

        for (InstalledModuleReference reference : active.keySet()) {
            boolean exists = states.stream()
                    .anyMatch(state -> state.reference().equals(reference));

            if (!exists) {
                states.add(new RevivalState(reference, 0));
            }
        }

        states.sort(Comparator
                .comparingInt((RevivalState state) -> state.reference().matrixSlot())
                .thenComparingInt(state -> state.reference().moduleIndex()));

        return runtime.withRevivals(states);
    }

    public static RevivalResult tryRevive(
            ExoskeletonData data,
            ExoskeletonRuntimeState runtime
    ) {
        Map<InstalledModuleReference, RevivalProperties> active = collect(
                data,
                runtime.poweredModules()
        );

        List<RevivalState> states = new ArrayList<>(runtime.revivals());

        states.sort(Comparator
                .comparingInt((RevivalState state) -> state.reference().matrixSlot())
                .thenComparingInt(state -> state.reference().moduleIndex()));

        for (RevivalState state : states) {
            RevivalProperties properties = active.get(state.reference());

            if (properties == null || !state.ready()) {
                continue;
            }

            List<RevivalState> updatedStates = new ArrayList<>(states);
            for (int i = 0; i < updatedStates.size(); i++) {
                RevivalState current = updatedStates.get(i);

                if (current.reference().equals(state.reference())) {
                    updatedStates.set(
                            i,
                            new RevivalState(
                                    current.reference(),
                                    properties.cooldown()
                            )
                    );
                    break;
                }
            }

            return new RevivalResult(
                    true,
                    properties,
                    runtime.withRevivals(updatedStates)
            );
        }

        return new RevivalResult(
                false,
                null,
                runtime
        );
    }

    private static Map<InstalledModuleReference, RevivalProperties> collect(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        Map<InstalledModuleReference, RevivalProperties> result = new HashMap<>();

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix = data.matrices()
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

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (!isPowered(module.id(), reference, poweredModules)) {
                    continue;
                }

                ModModules.getDefinition(module.id())
                        .revival()
                        .ifPresent(properties ->
                                result.put(reference, properties)
                        );
            }
        }

        return result;
    }

    private static boolean isPowered(
            ResourceLocation moduleId,
            InstalledModuleReference reference,
            Set<InstalledModuleReference> poweredModules
    ) {
        var energy = ModModules.getDefinition(moduleId).energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(reference);
    }

    public record RevivalResult(
            boolean revived,
            RevivalProperties properties,
            ExoskeletonRuntimeState runtime
    ) {}
}
