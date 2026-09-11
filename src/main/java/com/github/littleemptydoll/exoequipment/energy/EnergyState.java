package com.github.littleemptydoll.exoequipment.energy;

/**
 * Runtime energy state of an exoskeleton.
 *
 * <p>This class intentionally contains only runtime values. Static capabilities
 * such as capacity and throughput are provided by the active equipment state.</p>
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

    public int netGeneration() {
        return generation - consumption;
    }

    public int availableEnergy() {
        return storedEnergy + generation;
    }

    public boolean canOperate() {
        return generation > 0 || storedEnergy > 0;
    }
}
