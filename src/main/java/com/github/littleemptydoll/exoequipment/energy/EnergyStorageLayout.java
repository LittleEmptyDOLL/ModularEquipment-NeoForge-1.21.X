package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.StorageProperties;

import java.util.ArrayList;
import java.util.List;

final class EnergyStorageLayout {
    private final List<Entry> entries;

    private EnergyStorageLayout(List<Entry> entries) {
        this.entries = entries;
    }

    static EnergyStorageLayout create(ExoskeletonData data) {
        List<Entry> entries = new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {
            StorageProperties storage =
                    activeModule.definition()
                            .storage()
                            .orElse(null);

            if (storage == null) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            int effectiveCapacity = Math.max(
                    0,
                    (int) Math.round(
                            storage.capacity() * efficiency
                    )
            );

            entries.add(new Entry(
                    activeModule.reference(),
                    storage,
                    effectiveCapacity,
                    activeModule.module().storedEnergy()
            ));
        }

        return new EnergyStorageLayout(entries);
    }

    int availableOutput() {
        int available = 0;

        for (Entry entry : entries) {
            available += Math.min(
                    entry.storage.maxOutput(),
                    entry.stored
            );
        }

        return available;
    }

    int availableInput() {
        int available = 0;

        for (Entry entry : entries) {
            available += Math.min(
                    entry.storage.maxInput(),
                    Math.max(0, entry.capacity - entry.stored)
            );
        }

        return available;
    }

    int discharge(int amount) {
        int remaining = amount;
        int transferred = 0;

        for (Entry entry : entries) {
            if (remaining <= 0) {
                break;
            }

            entry.markClampedIfNeeded();

            int extracted = Math.min(
                    remaining,
                    Math.min(
                            entry.storage.maxOutput(),
                            entry.stored
                    )
            );

            if (extracted > 0) {
                entry.stored -= extracted;
                entry.dirty = true;
            }

            remaining -= extracted;
            transferred += extracted;
        }

        return transferred;
    }

    int charge(int amount) {
        int remaining = amount;
        int transferred = 0;

        for (Entry entry : entries) {
            if (remaining <= 0) {
                break;
            }

            entry.markClampedIfNeeded();

            int accepted = Math.min(
                    remaining,
                    Math.min(
                            entry.storage.maxInput(),
                            Math.max(0, entry.capacity - entry.stored)
                    )
            );

            if (accepted > 0) {
                entry.stored += accepted;
                entry.dirty = true;
            }

            remaining -= accepted;
            transferred += accepted;
        }

        return transferred;
    }

    ExoskeletonData apply(ExoskeletonData data) {
        ExoskeletonData updated = data;

        for (Entry entry : entries) {
            if (!entry.dirty) {
                continue;
            }

            int stored = entry.stored;
            updated = ExoskeletonModules.update(
                    updated,
                    entry.reference,
                    module -> module.withStoredEnergy(stored)
            );
        }

        return updated;
    }

    private static final class Entry {
        private final InstalledModuleReference reference;
        private final StorageProperties storage;
        private final int capacity;
        private final int originalStored;
        private int stored;
        private boolean dirty;

        private Entry(
                InstalledModuleReference reference,
                StorageProperties storage,
                int capacity,
                int stored
        ) {
            this.reference = reference;
            this.storage = storage;
            this.capacity = capacity;
            this.originalStored = stored;
            this.stored = Math.min(stored, capacity);
        }

        private void markClampedIfNeeded() {
            if (stored != originalStored) {
                dirty = true;
            }
        }
    }
}
