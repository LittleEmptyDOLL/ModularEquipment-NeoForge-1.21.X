package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;

/**
 * Result of one energy-network simulation step.
 *
 * <p>The returned {@link ExoskeletonData} contains the updated charge of
 * storage modules. The counters describe what happened during this step.</p>
 */
public record EnergyTickResult(
        ExoskeletonData data,
        int generated,
        int externalInput,
        int consumed,
        int charged,
        int discharged,
        int deficit,
        int wasted
) {
    public EnergyTickResult {
        if (generated < 0) throw new IllegalArgumentException("Generated energy cannot be negative");
        if (externalInput < 0) throw new IllegalArgumentException("External input cannot be negative");
        if (consumed < 0) throw new IllegalArgumentException("Consumed energy cannot be negative");
        if (charged < 0) throw new IllegalArgumentException("Charged energy cannot be negative");
        if (discharged < 0) throw new IllegalArgumentException("Discharged energy cannot be negative");
        if (deficit < 0) throw new IllegalArgumentException("Energy deficit cannot be negative");
        if (wasted < 0) throw new IllegalArgumentException("Wasted energy cannot be negative");
    }
}
