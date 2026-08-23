package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.matrix.MatrixData;

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
}
