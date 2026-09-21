package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonProfile;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;

public record CharacteristicsContext(
        ExoskeletonData data,
        Player player,
        Set<InstalledModuleReference> poweredModules,
        Integer matrixSlot
) {
    public CharacteristicsContext {
        if (data == null) {
            throw new IllegalArgumentException("Exoskeleton data cannot be null");
        }

        poweredModules = poweredModules == null
                ? Set.of()
                : Set.copyOf(poweredModules);

        if (matrixSlot != null && matrixSlot < 0) {
            throw new IllegalArgumentException("Matrix slot cannot be negative");
        }
    }

    public static CharacteristicsContext exoskeleton(
            ExoskeletonData data,
            Player player,
            Set<InstalledModuleReference> poweredModules
    ) {
        return new CharacteristicsContext(data, player, poweredModules, null);
    }

    public static CharacteristicsContext matrix(
            ExoskeletonData data,
            Player player,
            Set<InstalledModuleReference> poweredModules,
            int matrixSlot
    ) {
        if (matrixSlot >= data.matrices().size()) {
            throw new IllegalArgumentException("Matrix slot is out of range: " + matrixSlot);
        }

        return new CharacteristicsContext(
                scopedData(data, matrixSlot),
                player,
                poweredModules,
                matrixSlot
        );
    }

    public boolean isMatrixScope() {
        return matrixSlot != null;
    }

    private static ExoskeletonData scopedData(
            ExoskeletonData data,
            int matrixSlot
    ) {
        if (data.profiles().isEmpty()) {
            return data;
        }

        List<ExoskeletonProfile> profiles = data.profiles().stream()
                .map(profile -> new ExoskeletonProfile(
                        profile.name(),
                        profile.activeMatrices().contains(matrixSlot)
                                ? List.of(matrixSlot)
                                : List.of()
                ))
                .toList();

        int activeProfile = data.activeProfile();
        if (activeProfile < 0 || activeProfile >= profiles.size()) {
            return data;
        }

        return data.withProfiles(profiles, activeProfile);
    }
}
