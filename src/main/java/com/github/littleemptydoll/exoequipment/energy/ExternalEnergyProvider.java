package com.github.littleemptydoll.exoequipment.energy;

/**
 * Provides external energy to the exoskeleton energy bus.
 *
 * <p>The core energy system deliberately does not depend on any particular
 * energy API. Integrations adapt their own energy storage to this interface.</p>
 */
public interface ExternalEnergyProvider {
    /**
     * Returns the amount of energy that can currently be extracted.
     */
    int availableEnergy();

    /**
     * Extracts energy from the external source.
     *
     * @param amount maximum amount to extract
     * @param simulate whether the operation should only be simulated
     * @return the amount that can actually be extracted
     */
    int extractEnergy(int amount, boolean simulate);

    default int extractEnergy(int amount) {
        return extractEnergy(amount, false);
    }
}
