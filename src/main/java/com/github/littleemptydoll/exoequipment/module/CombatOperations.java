package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.Set;

public final class CombatOperations {
    private CombatOperations() {}

    public static CombatValues calculate(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double attackDamage = 0.0D;
        double attackSpeed = 0.0D;
        double entityInteractionRange = 0.0D;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix = data.matrices()
                    .get(slot)
                    .matrix()
                    .orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0;
                 moduleIndex < matrix.modules().size();
                 moduleIndex++) {

                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                var definition = ModModules.getDefinition(module.id());

                if (poweredModules != null
                        && definition.energy()
                        .filter(energy -> energy.consumption() > 0)
                        .isPresent()
                        && !poweredModules.contains(
                        new InstalledModuleReference(slot, moduleIndex)
                )) {
                    continue;
                }

                CombatProperties properties =
                        definition.combat().orElse(null);

                if (properties == null) {
                    continue;
                }

                attackDamage += properties.attackDamage();
                attackSpeed += properties.attackSpeed();
                entityInteractionRange += properties.entityInteractionRange();
            }
        }

        return new CombatValues(
                attackDamage,
                attackSpeed,
                entityInteractionRange
        );
    }

    public record CombatValues(
            double attackDamage,
            double attackSpeed,
            double entityInteractionRange
    ) {
        public CombatValues {
            if (!Double.isFinite(attackDamage)
                    || !Double.isFinite(attackSpeed)
                    || !Double.isFinite(entityInteractionRange)) {
                throw new IllegalArgumentException(
                        "Combat values must be finite"
                );
            }
        }
    }
}
