package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.StorageProperties;
import com.github.littleemptydoll.exoequipment.registry.ModEnergySystems;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs one simulation step of an exoskeleton energy network.
 *
 * <p>The operation is immutable: it never mutates the supplied data and
 * returns a new snapshot containing updated storage-module charge.</p>
 */
public final class EnergyOperations {
    private EnergyOperations() {}

    public static EnergyTickResult tick(ExoskeletonData data) {
        if (data.energySystem().isEmpty()) {
            return new EnergyTickResult(data, 0, 0, 0, 0, 0, 0);
        }

        ResourceLocation energySystemId = data.energySystem().get().definitionId();
        var energySystem = ModEnergySystems.getDefinition(energySystemId);
        var state = ExoskeletonState.calculateState(data);

        int generated = Math.min(state.energyGeneration(), energySystem.maxInput());
        int remainingOutput = energySystem.maxOutput();
        int remainingInput = energySystem.maxInput() - generated;

        int consumed = Math.min(state.energyConsumption(), Math.min(generated, remainingOutput));
        generated -= consumed;
        remainingOutput -= consumed;

        ExoskeletonData updatedData = data;
        int discharged = 0;
        int charged = 0;

        if (state.energyConsumption() > consumed && remainingOutput > 0 && remainingInput > 0) {
            int required = state.energyConsumption() - consumed;
            int discharge = Math.min(required, remainingOutput);
            discharge = Math.min(discharge, state.energyStorageOutput());
            discharge = Math.min(discharge, remainingInput);

            if (discharge > 0) {
                StorageTransferResult result = discharge(updatedData, discharge);
                updatedData = result.data();
                discharged = result.amount();
                consumed += discharged;
                remainingOutput -= discharged;
                remainingInput -= discharged;
            }
        }

        int deficit = Math.max(0, state.energyConsumption() - consumed);

        if (generated > 0 && remainingOutput > 0) {
            int charge = Math.min(generated, remainingOutput);
            charge = Math.min(charge, state.energyStorageInput());

            if (charge > 0) {
                StorageTransferResult result = charge(updatedData, charge);
                updatedData = result.data();
                charged = result.amount();
                generated -= charged;
                remainingOutput -= charged;
            }
        }

        int wasted = generated;

        return new EnergyTickResult(
                updatedData,
                state.energyGeneration(),
                consumed,
                charged,
                discharged,
                deficit,
                wasted
        );
    }

    private static StorageTransferResult charge(ExoskeletonData data, int amount) {
        int remaining = amount;
        int transferred = 0;
        ExoskeletonData updated = data;

        for (int slot : ExoskeletonState.activeMatrixSlots(updated)) {
            MatrixData matrix = updated.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            List<InstalledModule> modules = new ArrayList<>(matrix.modules());
            boolean changed = false;

            for (int i = 0; i < modules.size() && remaining > 0; i++) {
                InstalledModule module = modules.get(i);
                StorageProperties storage = ModModules.getDefinition(module.id())
                        .storage()
                        .orElse(null);
                if (storage == null) {
                    continue;
                }

                int current = Math.min(module.storedEnergy(), storage.capacity());
                int accepted = Math.min(
                        remaining,
                        Math.min(storage.maxInput(), storage.capacity() - current)
                );
                if (accepted <= 0) {
                    if (current != module.storedEnergy()) {
                        modules.set(i, module.withStoredEnergy(current));
                        changed = true;
                    }
                    continue;
                }

                modules.set(i, module.withStoredEnergy(current + accepted));
                remaining -= accepted;
                transferred += accepted;
                changed = true;
            }

            if (changed) {
                updated = updated.withMatrix(
                        slot,
                        new MatrixData(matrix.id(), modules)
                );
            }
        }

        return new StorageTransferResult(updated, transferred);
    }

    private static StorageTransferResult discharge(ExoskeletonData data, int amount) {
        int remaining = amount;
        int transferred = 0;
        ExoskeletonData updated = data;

        for (int slot : ExoskeletonState.activeMatrixSlots(updated)) {
            MatrixData matrix = updated.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            List<InstalledModule> modules = new ArrayList<>(matrix.modules());
            boolean changed = false;

            for (int i = 0; i < modules.size() && remaining > 0; i++) {
                InstalledModule module = modules.get(i);
                StorageProperties storage = ModModules.getDefinition(module.id())
                        .storage()
                        .orElse(null);
                if (storage == null) {
                    continue;
                }

                int current = Math.min(module.storedEnergy(), storage.capacity());
                int extracted = Math.min(
                        remaining,
                        Math.min(storage.maxOutput(), current)
                );
                if (current != module.storedEnergy()) {
                    modules.set(i, module.withStoredEnergy(current - extracted));
                    changed = true;
                } else if (extracted > 0) {
                    modules.set(i, module.withStoredEnergy(current - extracted));
                    changed = true;
                }

                remaining -= extracted;
                transferred += extracted;
            }

            if (changed) {
                updated = updated.withMatrix(
                        slot,
                        new MatrixData(matrix.id(), modules)
                );
            }
        }

        return new StorageTransferResult(updated, transferred);
    }

    private record StorageTransferResult(ExoskeletonData data, int amount) {}
}
