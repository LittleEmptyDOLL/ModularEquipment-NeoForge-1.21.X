package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModControllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ExoskeletonValidation {
    private ExoskeletonValidation() {}

    public static boolean isValidMatrixSlot(
            int slot
    ) {
        return slot >= 0 && slot < ExoskeletonData.MAX_MATRICES;
    }

    public static void validateMatrixSlot(
            int slot
    ) {
        if (!isValidMatrixSlot(slot)) {
            throw new IllegalArgumentException(
                    "Invalid matrix slot: " + slot
            );
        }
    }

    public static boolean isValidProfile(
            ExoskeletonData data,
            int profile
    ) {
        return profile >= 0 && profile < data.profiles().size();
    }

    public static void validateProfile(
            ExoskeletonData data,
            int profile
    ) {
        validateController(data);

        if (!isValidProfile(data, profile)) {
            throw new IllegalArgumentException(
                    "Invalid profile index: " + profile
            );
        }

        ControllerDefinition controller = getControllerDefinition(data);

        if (profile >= controller.maxProfiles()) {
            throw new IllegalStateException(
                    "Profile exceeds controller limit"
            );
        }

        validateProfileMatrices(
                data,
                data.profiles().get(profile).activeMatrices()
        );
    }

    public static void validateController(
            ExoskeletonData data
    ) {
        if (data.controller().isEmpty()) {
            throw new IllegalStateException(
                    "Exoskeleton does not have a controller"
            );
        }
    }

    public static ControllerDefinition getControllerDefinition(
            ExoskeletonData data
    ) {
        Controller controller = data.controller()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Exoskeleton does not have a controller"
                        )
                );

        return ModControllers.getDefinition(controller.definitionId());
    }

    public static void validateProfileIndex(
            ExoskeletonData data,
            int profile
    ) {
        validateProfile(data, profile);
    }

    public static void validateProfileMatrices(
            ExoskeletonData data,
            List<Integer> matrices
    ) {
        ControllerDefinition definition = getControllerDefinition(data);

        if (matrices.size() > definition.maxActiveMatrices()) {
            throw new IllegalArgumentException(
                    "Profile cannot active more than "
                            + definition.maxActiveMatrices()
                            + " matrices"
            );
        }

        if (new HashSet<>(matrices).size() != matrices.size()) {
            throw new IllegalArgumentException(
                    "Duplicate matrix slot"
            );
        }

        for (int slot : matrices) {
            validateMatrixSlot(slot);
        }
    }

    public static void validateProfileActivation(
            ExoskeletonData data,
            int profile
    ) {
        validateProfile(data, profile);

        ExoskeletonProfile profileData = data.profiles().get(profile);

        validateProfileMatrices(
                data,
                profileData.activeMatrices()
        );
    }

    public static boolean canActivateProfile(
            ExoskeletonData data,
            int profile
    ) {
        if (data.controller().isEmpty()) {
            return false;
        }

        if (!isValidProfile(data, profile)) {
            return false;
        }

        ControllerDefinition controller = getControllerDefinition(data);

        ExoskeletonProfile profileData = data.profiles().get(profile);

        if (profileData.activeMatrices().size() > controller.maxActiveMatrices()) {
            return false;
        }

        Set<Integer> unique = new HashSet<>(profileData.activeMatrices());

        if (unique.size() != profileData.activeMatrices().size()) {
            return false;
        }

        for (int slot : profileData.activeMatrices()) {
            if (!isValidMatrixSlot(slot)) {
                return false;
            }
        }

        return true;
    }
}
