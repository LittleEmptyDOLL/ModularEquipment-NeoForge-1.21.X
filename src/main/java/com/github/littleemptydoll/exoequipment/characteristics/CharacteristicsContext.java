package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import net.minecraft.world.entity.player.Player;

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

        if (matrixSlot != null) {
            if (matrixSlot < 0 || matrixSlot >= data.matrices().size()) {
                throw new IllegalArgumentException(
                        "Matrix slot is out of range: " + matrixSlot
                );
            }
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
        return new CharacteristicsContext(
                data,
                player,
                poweredModules,
                matrixSlot
        );
    }

    public boolean isMatrixScope() {
        return matrixSlot != null;
    }
}
