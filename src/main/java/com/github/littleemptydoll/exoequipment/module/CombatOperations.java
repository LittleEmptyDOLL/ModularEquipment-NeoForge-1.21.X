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
        double attackKnockback = 0.0D;
        double entityInteractionRange = 0.0D;
        double critChance = 0.0D;
        double critDamage = 0.0D;
        double armorPierce = 0.0D;
        double armorShred = 0.0D;
        double protPierce = 0.0D;
        double protShred = 0.0D;
        double currentHpDamage = 0.0D;
        double lifeSteal = 0.0D;
        double overheal = 0.0D;
        double fireDamage = 0.0D;
        double coldDamage = 0.0D;
        double projectileDamage = 0.0D;
        double arrowDamage = 0.0D;
        double arrowVelocity = 0.0D;
        double drawSpeed = 0.0D;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) continue;

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) continue;

                var definition = ModModules.getDefinition(module.id());

                if (poweredModules != null
                        && definition.energy()
                        .filter(energy -> energy.consumption() > 0)
                        .isPresent()
                        && !poweredModules.contains(new InstalledModuleReference(slot, moduleIndex))) {
                    continue;
                }

                CombatProperties properties = definition.combat().orElse(null);
                if (properties == null) continue;

                attackDamage += properties.attackDamage();
                attackSpeed += properties.attackSpeed();
                attackKnockback += properties.attackKnockback();
                entityInteractionRange += properties.entityInteractionRange();
                critChance += properties.critChance();
                critDamage += properties.critDamage();
                armorPierce += properties.armorPierce();
                armorShred += properties.armorShred();
                protPierce += properties.protPierce();
                protShred += properties.protShred();
                currentHpDamage += properties.currentHpDamage();
                lifeSteal += properties.lifeSteal();
                overheal += properties.overheal();
                fireDamage += properties.fireDamage();
                coldDamage += properties.coldDamage();
                projectileDamage += properties.projectileDamage();
                arrowDamage += properties.arrowDamage();
                arrowVelocity += properties.arrowVelocity();
                drawSpeed += properties.drawSpeed();
            }
        }

        return new CombatValues(
                attackDamage, attackSpeed, attackKnockback,
                entityInteractionRange, critChance, critDamage,
                armorPierce, armorShred, protPierce, protShred,
                currentHpDamage, lifeSteal, overheal, fireDamage,
                coldDamage, projectileDamage, arrowDamage, arrowVelocity,
                drawSpeed
        );
    }

    public record CombatValues(
            double attackDamage,
            double attackSpeed,
            double attackKnockback,
            double entityInteractionRange,
            double critChance,
            double critDamage,
            double armorPierce,
            double armorShred,
            double protPierce,
            double protShred,
            double currentHpDamage,
            double lifeSteal,
            double overheal,
            double fireDamage,
            double coldDamage,
            double projectileDamage,
            double arrowDamage,
            double arrowVelocity,
            double drawSpeed
    ) {
        public CombatValues {
            if (!Double.isFinite(attackDamage)
                    || !Double.isFinite(attackSpeed)
                    || !Double.isFinite(attackKnockback)
                    || !Double.isFinite(entityInteractionRange)
                    || !Double.isFinite(critChance)
                    || !Double.isFinite(critDamage)
                    || !Double.isFinite(armorPierce)
                    || !Double.isFinite(armorShred)
                    || !Double.isFinite(protPierce)
                    || !Double.isFinite(protShred)
                    || !Double.isFinite(currentHpDamage)
                    || !Double.isFinite(lifeSteal)
                    || !Double.isFinite(overheal)
                    || !Double.isFinite(fireDamage)
                    || !Double.isFinite(coldDamage)
                    || !Double.isFinite(projectileDamage)
                    || !Double.isFinite(arrowDamage)
                    || !Double.isFinite(arrowVelocity)
                    || !Double.isFinite(drawSpeed)) {
                throw new IllegalArgumentException("Combat values must be finite");
            }
        }
    }
}
