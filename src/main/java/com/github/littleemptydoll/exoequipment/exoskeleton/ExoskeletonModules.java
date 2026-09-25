package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class ExoskeletonModules {
    private ExoskeletonModules() {}

    public static List<ActiveModule> activeSupported(
            ExoskeletonData data
    ) {
        List<ActiveModule> modules = new ArrayList<>();

        for (int matrixSlot : ExoskeletonState.activeMatrixSlots(data)) {
            modules.addAll(supportedInMatrix(data, matrixSlot));
        }

        return List.copyOf(modules);
    }

    public static List<ActiveModule> supportedInMatrix(
            ExoskeletonData data,
            int matrixSlot
    ) {
        MatrixData matrix = data.matrices()
                .get(matrixSlot)
                .matrix()
                .orElse(null);

        if (matrix == null) {
            return List.of();
        }

        List<ActiveModule> modules = new ArrayList<>();

        for (int moduleIndex = 0;
             moduleIndex < matrix.modules().size();
             moduleIndex++) {

            InstalledModule module = matrix.modules().get(moduleIndex);

            if (!FrameOperations.isModuleSupported(data, module)) {
                continue;
            }

            InstalledModuleReference reference =
                    new InstalledModuleReference(
                            matrixSlot,
                            moduleIndex
                    );

            modules.add(new ActiveModule(
                    reference,
                    module,
                    ModModules.getDefinition(module.id())
            ));
        }

        return List.copyOf(modules);
    }

    public static boolean isPowered(
            ActiveModule module,
            Set<InstalledModuleReference> poweredModules
    ) {
        return isPowered(
                module.definition(),
                module.reference(),
                poweredModules
        );
    }

    public static boolean isPowered(
            ModuleDefinition definition,
            InstalledModuleReference reference,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (poweredModules == null) {
            return true;
        }

        var energy = definition.energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(reference);
    }

    public static Optional<InstalledModule> get(
            ExoskeletonData data,
            InstalledModuleReference reference
    ) {
        if (reference.matrixSlot() >= data.matrices().size()) {
            return Optional.empty();
        }

        MatrixData matrix = data.matrices()
                .get(reference.matrixSlot())
                .matrix()
                .orElse(null);

        if (matrix == null
                || reference.moduleIndex() >= matrix.modules().size()) {
            return Optional.empty();
        }

        return Optional.of(
                matrix.modules().get(reference.moduleIndex())
        );
    }

    public static ExoskeletonData update(
            ExoskeletonData data,
            InstalledModuleReference reference,
            InstalledModule module
    ) {
        if (reference.matrixSlot() >= data.matrices().size()) {
            return data;
        }

        MatrixData matrix = data.matrices()
                .get(reference.matrixSlot())
                .matrix()
                .orElse(null);

        if (matrix == null
                || reference.moduleIndex() >= matrix.modules().size()) {
            return data;
        }

        List<InstalledModule> modules =
                new ArrayList<>(matrix.modules());

        modules.set(reference.moduleIndex(), module);

        return data.withMatrix(
                reference.matrixSlot(),
                new MatrixData(matrix.id(), modules)
        );
    }

    public static ExoskeletonData update(
            ExoskeletonData data,
            InstalledModuleReference reference,
            UnaryOperator<InstalledModule> updater
    ) {
        return get(data, reference)
                .map(module ->
                        update(
                                data,
                                reference,
                                updater.apply(module)
                        )
                )
                .orElse(data);
    }

    public record ActiveModule(
            InstalledModuleReference reference,
            InstalledModule module,
            ModuleDefinition definition
    ) {}
}
