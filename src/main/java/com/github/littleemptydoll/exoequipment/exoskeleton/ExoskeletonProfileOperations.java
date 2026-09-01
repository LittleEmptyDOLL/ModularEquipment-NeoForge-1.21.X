package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;

import java.util.ArrayList;
import java.util.List;

public final class ExoskeletonProfileOperations {
    private ExoskeletonProfileOperations() {}

    public static ExoskeletonData createProfile(
            ExoskeletonData data
    ) {
        ControllerDefinition definition = ExoskeletonValidation.getControllerDefinition(data);

        if (data.profiles().size() >= definition.maxProfiles()) {
            throw new IllegalStateException(
                    "Maximum number of profiles reached"
            );
        }

        List<ExoskeletonProfile> profiles = new ArrayList<>(data.profiles());

        profiles.add(new ExoskeletonProfile(List.of()));

        return data.withProfiles(
                profiles
        );
    }

    public static ExoskeletonData removeProfile(
            ExoskeletonData data,
            int profile
    ) {
        if (data.profiles().size() <= 1) {
            throw new IllegalStateException(
                    "Cannot remove the last profile"
            );
        }

        ExoskeletonValidation.validateProfileIndex(data, profile);

        List<ExoskeletonProfile> profiles = new ArrayList<>(data.profiles());

        profiles.remove(profile);

        int activeProfile = data.activeProfile();

        if (profile < activeProfile) {
            activeProfile--;
        } else if (profile == activeProfile) {
            activeProfile = Math.min(
                    activeProfile,
                    profiles.size() - 1
            );
        }

        return data.withProfiles(
                profiles,
                activeProfile
        );
    }

    public static ExoskeletonData setProfileMatrices(
            ExoskeletonData data,
            int profile,
            List<Integer> matrices
    ) {
        ExoskeletonValidation.validateProfileIndex(data, profile);
        ExoskeletonValidation.validateProfileMatrices(data, matrices);

        return data.withProfiles(
                profile,
                new ExoskeletonProfile(
                        List.copyOf(matrices)
                )
        );
    }

    public static ExoskeletonData activateProfile(
            ExoskeletonData data,
            int profile
    ) {
        ExoskeletonValidation.validateProfileIndex(data, profile);

        return data.withActiveProfile(profile);
    }
}
