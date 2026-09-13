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
 * Performs one simulation step of the exoskeleton energy bus.
 *
 * <p>The energy system is the main bus. Every amount of energy entering the
 * bus is limited by {@code maxInput}, regardless of whether it comes from a
 * built-in generator, an external source, or a battery. Every amount leaving
 * the bus is limited by {@code maxOutput}, including energy used to charge a
 * battery.</p>
 *
 * <p>Source priority is:</p>
 * <ol>
 *     <li>built-in generators,</li>
 *     <li>external energy,</li>
 *     <li>batteries.</li>
 * </ol>
 *
 * <p>Battery charging is the lowest-priority consumer of the bus. Generator
 * surplus is used before external energy, and battery storage itself applies
 * its own {@code maxInput} limit.</p>
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
     * Simulates a tick with an external energy provider.
     *
     * <p>The provider is responsible for the actual extraction. This keeps
     * the energy bus independent from NeoForge Energy and allows integrations
     * to adapt any external energy API to {@link ExternalEnergyProvider}.</p>
     */
    public static EnergyTickResult tick(
            ExoskeletonData data,
            ExternalEnergyProvider externalProvider
    ) {
        if (externalProvider == null) {
            return tick(data, 0);
        }

        int externalAvailable = externalProvider.availableEnergy();
        if (externalAvailable < 0) {
            throw new IllegalArgumentException("External available energy cannot be negative");
        }

        return tick(
                data,
                externalAvailable,
                amount -> externalProvider.extractEnergy(amount, false)
        );
    }

    /**
     * Simulates a tick with an amount of external energy available to the
     * exoskeleton during this tick.
     *
     * <p>This overload is retained for callers that already have a numeric
     * amount rather than an external storage object. No external storage is
     * mutated by this overload.</p>
     */
    public static EnergyTickResult tick(ExoskeletonData data, int externalAvailable) {
        if (externalAvailable < 0) {
            throw new IllegalArgumentException("External available energy cannot be negative");
        }

        return tick(data, externalAvailable, amount -> amount);
    }

    private static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable,
            ExternalEnergyExtractor externalExtractor
    ) {
        if (data.energySystem().isEmpty()) {
            return new EnergyTickResult(data, 0, 0, 0, 0, 0, 0, 0);
        }

        ResourceLocation energySystemId = data.energySystem().get().definitionId();
        var energySystem = ModEnergySystems.getDefinition(energySystemId);
        var state = ExoskeletonState.calculateState(data);

        int remainingInput = energySystem.maxInput();
        int remainingOutput = energySystem.maxOutput();
        int remainingDemand = state.energyConsumption();

        int generated = state.energyGeneration();
        int externalInput = 0;
        int consumed = 0;
        int discharged = 0;
        int charged = 0;
        int generatedCharged = 0;

        ExoskeletonData updatedData = data;

        // 1. Built-in generators have the highest priority. Only the amount
        // that actually enters the bus consumes maxInput; excess generation
        // never reaches the bus and is wasted.
        int generatedIntoBus = Math.min(generated, remainingInput);
        generated = generatedIntoBus;
        remainingInput -= generatedIntoBus;

        int generatedToConsumers = Math.min(
                generated,
                Math.min(remainingDemand, remainingOutput)
        );
        generated -= generatedToConsumers;
        consumed += generatedToConsumers;
        remainingDemand -= generatedToConsumers;
        remainingOutput -= generatedToConsumers;

        // 2. External energy is the second-priority source. It can only use
        // input capacity left by higher-priority internal generation.
        int externalToConsumersRequested = Math.min(
                externalAvailable,
                Math.min(remainingDemand, Math.min(remainingInput, remainingOutput))
        );
        int externalToConsumers = externalExtractor.extract(externalToConsumersRequested);
        externalToConsumers = Math.min(externalToConsumers, externalToConsumersRequested);
        externalInput += externalToConsumers;
        consumed += externalToConsumers;
        remainingDemand -= externalToConsumers;
        externalAvailable -= externalToConsumers;
        remainingInput -= externalToConsumers;
        remainingOutput -= externalToConsumers;

        // 3. Batteries are the lowest-priority source. Their discharge enters
        // the bus, so it consumes the same maxInput budget as every other
        // source. The battery itself additionally applies maxOutput.
        if (remainingDemand > 0 && remainingInput > 0 && remainingOutput > 0) {
            int discharge = Math.min(
                    remainingDemand,
                    Math.min(
                            remainingInput,
                            Math.min(remainingOutput, state.energyStorageOutput())
                    )
            );
            if (discharge > 0) {
                StorageTransferResult result = discharge(updatedData, discharge);
                updatedData = result.data();
                discharged = result.amount();
                consumed += discharged;
                remainingDemand -= discharged;
                remainingInput -= discharged;
                remainingOutput -= discharged;
            }
        }

        // The remaining generator energy is already inside the bus and can be
        // sent to a battery. Charging is a bus output, therefore it consumes
        // maxOutput. The battery also applies its own maxInput.
        if (generated > 0 && remainingOutput > 0) {
            int charge = Math.min(
                    generated,
                    Math.min(remainingOutput, state.energyStorageInput())
            );
            if (charge > 0) {
                StorageTransferResult result = charge(updatedData, charge);
                updatedData = result.data();
                charged += result.amount();
                generatedCharged = result.amount();
                generated -= result.amount();
                remainingOutput -= result.amount();
            }
        }

        // External energy can also charge batteries, but only after the
        // higher-priority generator surplus has been handled. It still enters
        // the bus, so the remaining maxInput budget applies, and charging is
        // subject to maxOutput and the battery's own maxInput.
        if (externalAvailable > 0 && remainingInput > 0 && remainingOutput > 0) {
            int chargeRequested = Math.min(
                    externalAvailable,
                    Math.min(
                            remainingInput,
                            Math.min(remainingOutput, state.energyStorageInput())
                    )
            );
            int externalCharge = externalExtractor.extract(chargeRequested);
            externalCharge = Math.min(externalCharge, chargeRequested);
            if (externalCharge > 0) {
                StorageTransferResult result = charge(updatedData, externalCharge);
                updatedData = result.data();
                charged += result.amount();
                externalInput += result.amount();
                externalAvailable -= result.amount();
                remainingInput -= result.amount();
                remainingOutput -= result.amount();
            }
        }

        int deficit = remainingDemand;
        int wasted = state.energyGeneration() - generatedToConsumers - generatedCharged;

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

    @FunctionalInterface
    private interface ExternalEnergyExtractor {
        int extract(int amount);
    }

    private record StorageTransferResult(ExoskeletonData data, int amount) {}
}
