package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class CharacteristicsSupport {
    private CharacteristicsSupport() {}

    static Set<InstalledModuleReference> poweredModules(
            CharacteristicsContext context
    ) {
        if (!context.isMatrixScope()) {
            return context.poweredModules();
        }

        MatrixData matrix = selectedMatrix(context);

        if (matrix == null) {
            return Set.of();
        }

        int slot = context.matrixSlot();
        java.util.HashSet<InstalledModuleReference> result =
                new java.util.HashSet<>();

        for (int index = 0;
             index < matrix.modules().size();
             index++) {
            result.add(
                    new InstalledModuleReference(
                            slot,
                            index
                    )
            );
        }

        return Set.copyOf(result);
    }

    static MatrixData selectedMatrix(
            CharacteristicsContext context
    ) {
        if (!context.isMatrixScope()) {
            return null;
        }

        int slot = context.matrixSlot();

        if (slot < 0
                || slot >= context.data().matrices().size()) {
            return null;
        }

        return context.data()
                .matrices()
                .get(slot)
                .matrix()
                .orElse(null);
    }

    static MatrixState matrixState(
            CharacteristicsContext context,
            MatrixData matrix
    ) {
        return MatrixOperations.calculateState(
                matrix,
                module ->
                        FrameOperations.isModuleSupported(
                                context.data(),
                                module
                        ),
                context.data().temperature()
        );
    }

    static List<ExoskeletonModules.ActiveModule> runtimeModules(
            CharacteristicsContext context
    ) {
        if (context.isMatrixScope()) {
            return ExoskeletonModules.supportedInMatrix(
                    context.data(),
                    context.matrixSlot()
            );
        }

        return ExoskeletonModules.activeSupported(
                context.data()
        );
    }

    static List<ExoskeletonModules.ActiveModule> installedModules(
            CharacteristicsContext context
    ) {
        if (context.isMatrixScope()) {
            return ExoskeletonModules.supportedInMatrix(
                    context.data(),
                    context.matrixSlot()
            );
        }

        List<ExoskeletonModules.ActiveModule> result =
                new ArrayList<>();

        for (int slot = 0;
             slot < context.data().matrices().size();
             slot++) {
            result.addAll(
                    ExoskeletonModules.supportedInMatrix(
                            context.data(),
                            slot
                    )
            );
        }

        return List.copyOf(result);
    }
}
