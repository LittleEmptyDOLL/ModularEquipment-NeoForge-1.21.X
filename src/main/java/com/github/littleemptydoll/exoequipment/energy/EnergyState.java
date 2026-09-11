package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.registry.ModEnergySystems;
import net.minecraft.resources.ResourceLocation;

/**
 * Runtime snapshot of the energy network of an exoskeleton.
 *
 * <p>Static capabilities are resolved from the installed energy system and
 * active modules. Stored energy is read from the individual installed storage
 * modules, so each battery keeps its own charge.</p>
 */
public record EnergyState(
        int storedEnergy,
        int storageCapacity,
        int maxInput,
        int maxOutput,
        int generation,
        int consumption
) {
    public EnergyState {
        if (storedEnergy < 0) {
            throw new IllegalArgumentException("Stored energy cannot be negative");
        }
        if (storageCapacity < 0) {
            throw new IllegalArgumentException("Storage capacity cannot be negative");
        }
        if (maxInput < 0) {
            throw new IllegalArgumentException("Max input cannot be negative");
        }
        if (maxOutput < 0) {
            throw new IllegalArgumentException("Max output cannot be negative");
        }
        if (generation < 0) {
            throw new IllegalArgumentException("Generation cannot be negative");
        }
        if (consumption < 0) {
            throw new IllegalArgumentException("Consumption cannot be negative");
        }
        if (storedEnergy > storageCapacity) {
            throw new IllegalArgumentException("Stored energy cannot exceed storage capacity");
        }
    }

    public static EnergyState calculate(ExoskeletonData data) {
        if (data.energySystem().isEmpty()) {
            return new EnergyState(0, 0, 0, 0, 0, 0);
        }

        ResourceLocation energySystemId = data.energySystem().get().definitionId();
        var definition = ModEnergySystems.getDefinition(energySystemId);
        var state = ExoskeletonState.calculateState(data);

        int storedEnergy = ExoskeletonState.activeMatrices(data).stream()
                .mapToInt(MatrixOperations::calculateStoredEnergy)
                .sum();

        return new EnergyState(
                storedEnergy,
                state.energyStorageCapacity(),
                Math.min(definition.maxInput(), state.energyStorageInput()),
                Math.min(definition.maxOutput(), state.energyStorageOutput()),
                state.energyGeneration(),
                state.energyConsumption()
        );
    }

    public int netGeneration() {
        return generation - consumption;
    }

    public int availableEnergy() {
        return storedEnergy + generation;
    }

    public int availableInput() {
        return Math.min(maxInput, Math.max(0, storageCapacity - storedEnergy));
    }

    public int availableOutput() {
        return Math.min(maxOutput, storedEnergy);
    }

    public boolean hasEnergySystem() {
        return maxInput > 0 || maxOutput > 0;
    }

    public boolean canOperate() {
        return hasEnergySystem()
                && (generation > 0 || storedEnergy > 0);
    }
}
