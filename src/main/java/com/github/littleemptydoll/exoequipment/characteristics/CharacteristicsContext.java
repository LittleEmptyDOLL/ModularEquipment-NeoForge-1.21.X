package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
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

        if (matrixSlot != null) {
            if (matrixSlot < 0 || matrixSlot >= data.matrices().size()) {
                throw new IllegalArgumentException(
                        "Matrix slot is out of range: " + matrixSlot
                );
            }

            MatrixData selectedMatrix = data.matrices()
                    .get(matrixSlot)
                    .matrix()
                    .orElse(null);

            if (player != null && selectedMatrix != null) {
                var equipped = ExoskeletonAccess.findContext(player)
                        .orElse(null);

                if (equipped != null) {
                    int equippedSlot = findMatrixSlot(
                            equipped.data(),
                            selectedMatrix
                    );

                    if (equippedSlot >= 0) {
                        data = equipped.data()
                                .withMatrix(
                                        equippedSlot,
                                        selectedMatrix
                                );
                        matrixSlot = equippedSlot;
                    } else {
                        data = data.withTemperature(Double.NaN);
                    }
                } else {
                    data = data.withTemperature(Double.NaN);
                }
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

    private static int findMatrixSlot(
            ExoskeletonData equippedData,
            MatrixData selectedMatrix
    ) {
        for (int slot = 0;
             slot < equippedData.matrices().size();
             slot++) {
            MatrixData candidate = equippedData.matrices()
                    .get(slot)
                    .matrix()
                    .orElse(null);

            if (candidate != null
                    && sameLayout(candidate, selectedMatrix)) {
                return slot;
            }
        }

        return -1;
    }

    private static boolean sameLayout(
            MatrixData first,
            MatrixData second
    ) {
        if (!first.id().equals(second.id())) {
            return false;
        }

        List<InstalledModule> firstModules = first.modules();
        List<InstalledModule> secondModules = second.modules();

        if (firstModules.size() != secondModules.size()) {
            return false;
        }

        for (int index = 0;
             index < firstModules.size();
             index++) {
            InstalledModule firstModule = firstModules.get(index);
            InstalledModule secondModule = secondModules.get(index);

            if (!firstModule.id().equals(secondModule.id())
                    || firstModule.x() != secondModule.x()
                    || firstModule.y() != secondModule.y()
                    || firstModule.rotation() != secondModule.rotation()) {
                return false;
            }
        }

        return true;
    }
}
