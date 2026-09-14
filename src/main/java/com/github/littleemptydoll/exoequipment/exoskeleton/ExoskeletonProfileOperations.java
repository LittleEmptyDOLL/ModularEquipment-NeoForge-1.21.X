package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;

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
        String name = createDefaultProfileName(profiles);

        profiles.add(new ExoskeletonProfile(name, List.of()));

        return data.withProfiles(
                profiles
        );
    }

    private static String createDefaultProfileName(
            List<ExoskeletonProfile> profiles
    ) {
        int number = 1;

        while (profiles.stream().anyMatch(profile ->
                profile.name().equals("Profile " + number))) {
            number++;
        }

        return "Profile " + number;
    }

    public static ExoskeletonData renameProfile(
            ExoskeletonData data,
            int profile,
            String name
    ) {
        ExoskeletonValidation.validateProfileIndex(data, profile);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Profile name cannot be blank"
            );
        }

        return data.withProfiles(
                profile,
                data.profiles()
                        .get(profile)
                        .withName(name)
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
                        data.profiles().get(profile).name(),
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

    public static ExoskeletonProfile getActiveProfile(
            ExoskeletonData data
    ) {
        ExoskeletonValidation.validateProfile(
                data,
                data.activeProfile()
        );

        return data.profiles().get(data.activeProfile());
    }

    public static List<MatrixData> getActiveMatrices(
            ExoskeletonData data
    ) {
        ExoskeletonProfile profile = getActiveProfile(data);

        List<MatrixData> matrices = new ArrayList<>();

        for (int slot : profile.activeMatrices()) {
            data.matrices()
                    .get(slot)
                    .matrix()
                    .ifPresent(matrices::add);
        }

        return List.copyOf(matrices);
    }
}
