package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixState;

import java.util.ArrayList;
import java.util.List;

public final class ExoskeletonState {
    private ExoskeletonState() {}

    public static boolean hasActiveProfile(
            ExoskeletonData data
    ) {
        return data.activeProfile() >= 0
                && data.activeProfile() < data.profiles().size();
    }

    public static ExoskeletonProfile activeProfile(
            ExoskeletonData data
    ) {
        if (!hasActiveProfile(data)) {
            throw new IllegalStateException(
                    "Exoskeleton does not have an active profile"
            );
        }

        return data.profiles().get(data.activeProfile());
    }

    public static List<Integer> activeMatrixSlots(
            ExoskeletonData data
    ) {
        if (!hasActiveProfile(data)) {
            return List.of();
        }

        return List.copyOf(
                activeProfile(data).activeMatrices()
        );
    }

    public static boolean canActivate(
            ExoskeletonData data
    ) {
        if (!hasActiveProfile(data)) {
            return false;
        }

        return ExoskeletonValidation.canActivateProfile(
                data,
                data.activeProfile()
        );
    }

    public static List<MatrixData> activeMatrices(
            ExoskeletonData data
    ) {
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

    public static int activeMatrixCount(
            ExoskeletonData data
    ) {
        return activeMatrices(data).size();
    }

    public static boolean isMatrixActive(
            ExoskeletonData data,
            int slot
    ) {
        if (!canActivate(data)){
            return false;
        }

        return activeMatrixSlots(data).contains(slot)
                && data.matrices()
                .get(slot)
                .matrix()
                .isPresent();
    }

    public static MatrixState calculateState(
            ExoskeletonData data
    ) {
        int energyConsumption = 0;
        int heatGeneration = 0;
        int cooling = 0;

        for (MatrixData matrix : activeMatrices(data)) {
            MatrixState state = MatrixOperations.calculateState(matrix);

            energyConsumption += state.energyConsumption();
            heatGeneration += state.heatGeneration();
            cooling += state.cooling();
        }

        return new MatrixState(
                energyConsumption,
                heatGeneration,
                cooling
        );
    }
}
