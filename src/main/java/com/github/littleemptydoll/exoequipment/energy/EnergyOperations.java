package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
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
 * <p>Energy priority is:</p>
 * <ol>
 *     <li>built-in generators,</li>
 *     <li>external energy,</li>
 *     <li>batteries.</li>
 * </ol>
 *
 * <p>The energy-system {@code maxInput} limits energy entering the network
 * from external sources and energy sent into batteries. Built-in generation
 * is internal to the network and does not consume this input budget.
 * {@code maxOutput} limits energy delivered to ordinary consumers.</p>
 */
public final class EnergyOperations {
    private EnergyOperations() {}

    /**
     * Simulates a tick without an external energy source.
     */
    public static EnergyTickResult tick(ExoskeletonData data) {
        return tick(data, 0);
    }

    /**
     * Simulates a tick with an amount of external energy available to the
     * exoskeleton during this tick.
     *
     * <p>The caller/integration is responsible for obtaining this amount
     * from an external energy provider. This core operation only decides how
     * much of it the network actually accepts.</p>
     *
     * @param data exoskeleton state
     * @param externalAvailable external energy available this tick
     */
    public static EnergyTickResult tick(ExoskeletonData data, int externalAvailable) {
        if (externalAvailable < 0) {
            throw new IllegalArgumentException("External available energy cannot be negative");
        }

        if (data.energySystem().isEmpty()) {
            return new EnergyTickResult(data, 0, 0, 0, 0, 0, 0, 0);
        }

        ResourceLocation energySystemId = data.energySystem().get().definitionId();
        var energySystem = ModEnergySystems.getDefinition(energySystemId);
        var state = ExoskeletonState.calculateState(data);

        int remainingOutput = energySystem.maxOutput();
        int remainingInput = energySystem.maxInput();
        int generated = state.energyGeneration();
        int consumed = 0;
        int externalInput = 0;
        int discharged = 0;
        int charged = 0;

        ExoskeletonData updatedData = data;

        // 1. Built-in generators have the highest priority.
        int generatedToConsumers = Math.min(
                generated,
                Math.min(state.energyConsumption(), remainingOutput)
        );
        consumed += generatedToConsumers;
        generated -= generatedToConsumers;
        remainingOutput -= generatedToConsumers;

        // 2. External energy fills the remaining consumer demand.
        int demand = state.energyConsumption() - consumed;
        int externalToConsumers = Math.min(
                externalAvailable,
                Math.min(demand, Math.min(remainingInput, remainingOutput))
        );
        externalInput += externalToConsumers;
        consumed += externalToConsumers;
        remainingInput -= externalToConsumers;
        remainingOutput -= externalToConsumers;

        // 3. Batteries are the lowest-priority source and cover the remaining
        // demand. Battery discharge does not compete with maxInput because
        // energy is leaving the battery and entering the internal network.
        demand = state.energyConsumption() - consumed;
        if (demand > 0 && remainingOutput > 0) {
            int discharge = Math.min(
                    demand,
                    Math.min(remainingOutput, state.energyStorageOutput())
            );
            if (discharge > 0) {
                StorageTransferResult result = discharge(updatedData, discharge);
                updatedData = result.data();
                discharged = result.amount();
                consumed += discharged;
                remainingOutput -= discharged;
            }
        }

        // Surplus from built-in generation is always preferred when charging
        // storage. External energy is accepted for storage only after the
        // generator surplus has been used and only while maxInput remains.
        int surplus = generated;

        int externalRemaining = Math.max(0, externalAvailable - externalToConsumers);
        int availableForExternalStorage = Math.min(externalRemaining, remainingInput);

        if (surplus > 0 && remainingInput > 0) {
            int charge = Math.min(surplus, remainingInput);
            charge = Math.min(charge, state.energyStorageInput());
            if (charge > 0) {
                StorageTransferResult result = charge(updatedData, charge);
                updatedData = result.data();
                charged += result.amount();
                surplus -= result.amount();
                remainingInput -= result.amount();
            }
        }

        // Any remaining input budget can be used to pull external energy into
        // the battery. This keeps external energy second priority while the
        // battery remains the lowest-priority sink.
        if (availableForExternalStorage > 0 && remainingInput > 0) {
            int charge = Math.min(availableForExternalStorage, remainingInput);
            charge = Math.min(charge, state.energyStorageInput());
            if (charge > 0) {
                StorageTransferResult result = charge(updatedData, charge);
                updatedData = result.data();
                charged += result.amount();
                externalInput += result.amount();
                remainingInput -= result.amount();
            }
        }

        int wasted = surplus;
        int deficit = Math.max(0, state.energyConsumption() - consumed);

        return new EnergyTickResult(
                updatedData,
                state.energyGeneration(),
                externalInput,
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
