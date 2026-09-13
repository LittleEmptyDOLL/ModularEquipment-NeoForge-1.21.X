package com.github.littleemptydoll.exoequipment.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Adapter from NeoForge's standard {@link IEnergyStorage} API to the
 * exoskeleton's loader-independent external energy provider interface.
 */
public final class NeoForgeEnergyProvider implements ExternalEnergyProvider {
    private final IEnergyStorage storage;

    public NeoForgeEnergyProvider(IEnergyStorage storage) {
        this.storage = storage;
    }

    public IEnergyStorage storage() {
        return storage;
    }

    @Override
    public int availableEnergy() {
        if (!storage.canExtract()) {
            return 0;
        }

        return Math.max(0, storage.getEnergyStored());
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        if (amount <= 0 || !storage.canExtract()) {
            return 0;
        }

        return Math.max(0, storage.extractEnergy(amount, simulate));
    }
}
