package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;

import java.util.ArrayList;
import java.util.List;

public final class ExoskeletonState {
    private ExoskeletonState() {}

    public static boolean hasActiveProfile(ExoskeletonData data) {
        return data.activeProfile() >= 0
                && data.activeProfile() < data.profiles().size();
    }

    public static ExoskeletonProfile activeProfile(ExoskeletonData data) {
        if (!hasActiveProfile(data)) {
            throw new IllegalStateException(
                    "Exoskeleton does not have an active profile"
            );
        }

        return data.profiles().get(data.activeProfile());
    }

    public static List<Integer> activeMatrixSlots(ExoskeletonData data) {
        if (!hasActiveProfile(data)) {
            return List.of();
        }

        return List.copyOf(activeProfile(data).activeMatrices());
    }

    public static boolean canActivate(ExoskeletonData data) {
        if (!hasActiveProfile(data)) {
            return false;
        }

        return ExoskeletonValidation.canActivateProfile(
                data,
                data.activeProfile()
        );
    }

    public static List<MatrixData> activeMatrices(ExoskeletonData data) {
        if (!canActivate(data)) {
            return List.of();
        }

        List<MatrixData> result = new ArrayList<>();

        for (int slot : activeMatrixSlots(data)) {
            data.matrices()
                    .get(slot)
                    .matrix()
                    .ifPresent(result::add);
        }

        return List.copyOf(result);
    }

    public static int activeMatrixCount(ExoskeletonData data) {
        return activeMatrices(data).size();
    }

    public static boolean isMatrixActive(ExoskeletonData data, int slot) {
        if (!canActivate(data)) {
            return false;
        }

        return activeMatrixSlots(data).contains(slot)
                && data.matrices()
                .get(slot)
                .matrix()
                .isPresent();
    }

    public static MatrixState calculateState(ExoskeletonData data) {
        int energyConsumption = 0;
        int energyGeneration = 0;
        int energyStorageCapacity = 0;
        int energyStorageInput = 0;
        int energyStorageOutput = 0;
        int heatGeneration = 0;
        int cooling = 0;

        for (MatrixData matrix : activeMatrices(data)) {
            MatrixState state = MatrixOperations.calculateState(
                    matrix,
                    module -> FrameOperations.isModuleSupported(data, module),
                    data.temperature()
            );

            energyConsumption += state.energyConsumption();
            energyGeneration += state.energyGeneration();
            energyStorageCapacity += state.energyStorageCapacity();
            energyStorageInput += state.energyStorageInput();
            energyStorageOutput += state.energyStorageOutput();
            heatGeneration += state.heatGeneration();
            cooling += state.cooling();
        }

        return new MatrixState(
                energyConsumption,
                energyGeneration,
                energyStorageCapacity,
                energyStorageInput,
                energyStorageOutput,
                heatGeneration,
                cooling
        );
    }

    public static double calculateThermalBalance(ExoskeletonData data) {
        return calculateThermalBalance(data, data.temperature());
    }

    public static double calculateThermalBalance(
            ExoskeletonData data,
            double temperature
    ) {
        int heatGeneration = 0;
        int cooling = 0;

        for (MatrixData matrix : activeMatrices(data)) {
            heatGeneration += MatrixOperations.calculateHeatGeneration(
                    matrix,
                    module -> FrameOperations.isModuleSupported(data, module),
                    temperature
            );

            cooling += MatrixOperations.calculateCooling(
                    matrix,
                    module -> FrameOperations.isModuleSupported(data, module),
                    temperature
            );
        }

        return heatGeneration - cooling;
    }

    public static ExoskeletonData normalizeStoredEnergy(ExoskeletonData data) {
        ExoskeletonData updated = data;

        for (int slot = 0; slot < data.matrices().size(); slot++) {
            MatrixData matrix = updated.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            List<com.github.littleemptydoll.exoequipment.module.InstalledModule> modules =
                    new ArrayList<>(matrix.modules());
            boolean changed = false;

            for (int i = 0; i < modules.size(); i++) {
                var module = modules.get(i);
                var storage = com.github.littleemptydoll.exoequipment.registry.ModModules
                        .getDefinition(module.id())
                        .storage()
                        .orElse(null);

                if (storage == null) {
                    continue;
                }

                int effectiveCapacity = MatrixOperations.calculateEnergyStorageCapacity(
                        new MatrixData(matrix.id(), List.of(module)),
                        ignored -> true,
                        data.temperature()
                );
                int clamped = Math.max(0, Math.min(module.storedEnergy(), effectiveCapacity));

                if (clamped != module.storedEnergy()) {
                    modules.set(i, module.withStoredEnergy(clamped));
                    changed = true;
                }
            }

            if (changed) {
                updated = updated.withMatrix(slot, new MatrixData(matrix.id(), modules));
            }
        }

        return updated;
    }
}
