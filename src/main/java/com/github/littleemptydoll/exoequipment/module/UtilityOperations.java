package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import java.util.Set;

public final class UtilityOperations {
    private UtilityOperations() {}

    public static UtilityValues calculate(ExoskeletonData data, Set<InstalledModuleReference> poweredModules) {
        double blockBreakSpeed=0, blockInteractionRange=0, burningTime=0, luck=0, miningEfficiency=0;
        double submergedMiningSpeed=0, movementEfficiency=0, waterMovementEfficiency=0, oxygenBonus=0;
        double safeFallDistance=0, stepHeight=0, experienceGained=0, healingReceived=0, dodgeChance=0;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix=data.matrices().get(slot).matrix().orElse(null);
            if (matrix==null) continue;
            for (int moduleIndex=0; moduleIndex<matrix.modules().size(); moduleIndex++) {
                InstalledModule module=matrix.modules().get(moduleIndex);
                if (!FrameOperations.isModuleSupported(data,module)) continue;
                var definition=ModModules.getDefinition(module.id());
                if (poweredModules!=null && definition.energy().filter(e->e.consumption()>0).isPresent()
                        && !poweredModules.contains(new InstalledModuleReference(slot,moduleIndex))) continue;

                var utility=definition.utility().orElse(null);
                if (utility!=null) {
                    blockBreakSpeed+=utility.blockBreakSpeed();
                    blockInteractionRange+=utility.blockInteractionRange();
                    burningTime+=utility.burningTime();
                    luck+=utility.luck();
                    miningEfficiency+=utility.miningEfficiency();
                    submergedMiningSpeed+=utility.submergedMiningSpeed();
                    movementEfficiency+=utility.movementEfficiency();
                    waterMovementEfficiency+=utility.waterMovementEfficiency();
                    oxygenBonus+=utility.oxygenBonus();
                    safeFallDistance+=utility.safeFallDistance();
                    stepHeight+=utility.stepHeight();
                }
                var apothic=definition.apothicUtility().orElse(null);
                if (apothic!=null) {
                    experienceGained+=apothic.experienceGained();
                    healingReceived+=apothic.healingReceived();
                    dodgeChance+=apothic.dodgeChance();
                }
            }
        }
        return new UtilityValues(blockBreakSpeed,blockInteractionRange,burningTime,luck,miningEfficiency,
                submergedMiningSpeed,movementEfficiency,waterMovementEfficiency,oxygenBonus,safeFallDistance,
                stepHeight,experienceGained,healingReceived,dodgeChance);
    }

    public record UtilityValues(
            double blockBreakSpeed,double blockInteractionRange,double burningTime,double luck,
            double miningEfficiency,double submergedMiningSpeed,double movementEfficiency,
            double waterMovementEfficiency,double oxygenBonus,double safeFallDistance,double stepHeight,
            double experienceGained,double healingReceived,double dodgeChance) {
        public UtilityValues {
            for (double value : new double[]{blockBreakSpeed,blockInteractionRange,burningTime,luck,miningEfficiency,
                    submergedMiningSpeed,movementEfficiency,waterMovementEfficiency,oxygenBonus,safeFallDistance,
                    stepHeight,experienceGained,healingReceived,dodgeChance}) {
                if (!Double.isFinite(value)) throw new IllegalArgumentException("Utility values must be finite");
            }
        }
    }
}
