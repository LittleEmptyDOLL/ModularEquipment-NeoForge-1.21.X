package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.ModEnergySystems;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        return tick(data, externalProvider, null);
    }

    public static EnergyTickResult tick(
            ExoskeletonData data,
            ExternalEnergyProvider externalProvider,
            Player player
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
                amount -> externalProvider.extractEnergy(amount, false),
                player
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

        return tick(data, externalAvailable, amount -> amount, null);
    }

    private static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable,
            ExternalEnergyExtractor externalExtractor
    ) {
        return tick(data, externalAvailable, externalExtractor, null);
    }

    private static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable,
            ExternalEnergyExtractor externalExtractor,
            Player player
    ) {
        if (data.energySystem().isEmpty()) {
            return new EnergyTickResult(
                    data, 0, 0, 0, 0, 0, 0, 0, Set.of()
            );
        }

        ResourceLocation energySystemId = data.energySystem().get().definitionId();
        var energySystem = ModEnergySystems.getDefinition(energySystemId);
        var state = ExoskeletonState.calculateState(data);

        int remainingInput = energySystem.maxInput();
        int remainingOutput = energySystem.maxOutput();

        int generated = state.energyGeneration();
        int generatedAvailable = Math.min(generated, remainingInput);
        remainingInput -= generatedAvailable;

        int externalInput = 0;
        int consumed = 0;
        int discharged = 0;
        int charged = 0;
        int generatedCharged = 0;

        ExoskeletonData updatedData = data;
        Set<InstalledModuleReference> poweredModules = new LinkedHashSet<>();

        List<EnergyConsumer> consumers = collectConsumers(data, player);
        consumers.sort(Comparator.comparingInt(EnergyConsumer::priority).reversed());

        for (EnergyConsumer consumer : consumers) {
            int required = consumer.consumption();

            if (required > remainingOutput
                    || required > remainingInput + generatedAvailable) {
                continue;
            }

            int availableExternal = Math.min(
                    externalAvailable,
                    remainingInput
            );
            int availableBattery = Math.min(
                    remainingInput,
                    Math.min(
                            remainingOutput,
                            calculateAvailableStorageOutput(updatedData)
                    )
            );

            if (generatedAvailable + availableExternal + availableBattery < required) {
                continue;
            }

            int fromGenerated = Math.min(required, generatedAvailable);
            int remaining = required - fromGenerated;

            int fromExternal = Math.min(remaining, availableExternal);
            remaining -= fromExternal;

            int fromBattery = remaining;

            if (fromExternal > 0) {
                int extracted = externalExtractor.extract(fromExternal);
                fromExternal = Math.min(extracted, fromExternal);
                externalInput += fromExternal;
                externalAvailable -= fromExternal;
                remainingInput -= fromExternal;
            }

            if (fromBattery > 0) {
                StorageTransferResult result = discharge(updatedData, fromBattery);
                updatedData = result.data();
                fromBattery = result.amount();
                discharged += fromBattery;
                remainingInput -= fromBattery;
            }

            int supplied = fromGenerated + fromExternal + fromBattery;
            if (supplied != required) {
                continue;
            }

            generatedAvailable -= fromGenerated;
            consumed += supplied;
            remainingOutput -= supplied;
            poweredModules.add(consumer.reference());
        }

        if (generatedAvailable > 0 && remainingOutput > 0) {
            int charge = Math.min(
                    generatedAvailable,
                    Math.min(
                            remainingOutput,
                            calculateAvailableStorageInput(updatedData)
                    )
            );

            if (charge > 0) {
                StorageTransferResult result = charge(updatedData, charge);
                updatedData = result.data();
                charged += result.amount();
                generatedCharged = result.amount();
                generatedAvailable -= result.amount();
                remainingOutput -= result.amount();
            }
        }

        if (externalAvailable > 0 && remainingInput > 0 && remainingOutput > 0) {
            int chargeRequested = Math.min(
                    externalAvailable,
                    Math.min(
                            remainingInput,
                            Math.min(
                                    remainingOutput,
                                    calculateAvailableStorageInput(updatedData)
                            )
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

        int deficit = Math.max(0, state.energyConsumption() - consumed);
        int generatedUsed = generated - generatedAvailable - generatedCharged;
        int wasted = Math.max(0, generated - generatedUsed);

        return new EnergyTickResult(
                updatedData,
                generated,
                externalInput,
                consumed,
                charged,
                discharged,
                deficit,
                wasted,
                poweredModules
        );
    }

    public static EnergyConsumptionResult consumeEnergy(
            ExoskeletonData data,
            int amount
    ) {
        if (amount < 0) {
            throw new IllegalArgumentException("Energy amount cannot be negative");
        }
        if (amount == 0) {
            return new EnergyConsumptionResult(data, true, 0);
        }
        if (data.energySystem().isEmpty()) {
            return new EnergyConsumptionResult(data, false, 0);
        }

        var energySystem = ModEnergySystems.getDefinition(
                data.energySystem().get().definitionId()
        );
        int requested = Math.min(amount, energySystem.maxOutput());
        if (requested < amount) {
            return new EnergyConsumptionResult(data, false, 0);
        }

        StorageTransferResult result = discharge(data, amount);
        return new EnergyConsumptionResult(
                result.data(),
                result.amount() == amount,
                result.amount()
        );
    }

    private static List<EnergyConsumer> collectConsumers(ExoskeletonData data, Player player) {
        List<EnergyConsumer> consumers = new ArrayList<>();

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!com.github.littleemptydoll.exoequipment.frame.FrameOperations
                        .isModuleSupported(data, module)) {
                    continue;
                }

                var definition = ModModules.getDefinition(module.id());
                var energy = definition.energy();
                int consumption = energy
                        .map(EnergyProperties::consumption)
                        .orElse(0);

                if (consumption > 0) {
                    consumption = MatrixOperations.calculateEnergyConsumption(
                            new MatrixData(matrix.id(), List.of(module)),
                            ignored -> true,
                            data.temperature()
                    );
                }

                if (module.active()) {
                    consumption += ModModules.getDefinition(module.id())
                            .cloaking()
                            .map(CloakingProperties::activeConsumption)
                            .orElse(0);
                }

                if (module.flightActive()) {
                    consumption += ModModules.getDefinition(module.id())
                            .flight()
                            .map(FlightProperties::activeConsumption)
                            .orElse(0);
                }

                if (player != null
                        && JetpackInputState.isThrusting(player)) {
                    consumption += ModModules.getDefinition(module.id())
                            .jetpack()
                            .map(JetpackProperties::energyConsumption)
                            .orElse(0);
                }

                if (consumption <= 0) {
                    continue;
                }

                consumers.add(
                        new EnergyConsumer(
                                new InstalledModuleReference(slot, moduleIndex),
                                consumption,
                                energy.map(EnergyProperties::priority).orElse(0)
                        )
                );
            }
        }

        return consumers;
    }

    private static int calculateAvailableStorageOutput(ExoskeletonData data) {
        int available = 0;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            for (InstalledModule module : matrix.modules()) {
                var storage = ModModules.getDefinition(module.id()).storage().orElse(null);
                if (storage == null) {
                    continue;
                }

                int capacity = MatrixOperations.calculateEnergyStorageCapacity(
                        new MatrixData(matrix.id(), List.of(module)),
                        ignored -> true,
                        data.temperature()
                );

                int stored = Math.min(module.storedEnergy(), capacity);
                available += Math.min(storage.maxOutput(), stored);
            }
        }

        return available;
    }

    private static int calculateAvailableStorageInput(ExoskeletonData data) {
        int available = 0;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);
            if (matrix == null) {
                continue;
            }

            for (InstalledModule module : matrix.modules()) {
                var storage = ModModules.getDefinition(module.id()).storage().orElse(null);
                if (storage == null) {
                    continue;
                }

                int capacity = MatrixOperations.calculateEnergyStorageCapacity(
                        new MatrixData(matrix.id(), List.of(module)),
                        ignored -> true,
                        data.temperature()
                );

                int stored = Math.min(module.storedEnergy(), capacity);
                available += Math.min(
                        storage.maxInput(),
                        Math.max(0, capacity - stored)
                );
            }
        }

        return available;
    }

    public record EnergyConsumptionResult(
            ExoskeletonData data,
            boolean sufficient,
            int consumed
    ) {}

    private record EnergyConsumer(
            InstalledModuleReference reference,
            int consumption,
            int priority
    ) {}

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

                int effectiveCapacity = MatrixOperations.calculateEnergyStorageCapacity(
                        new MatrixData(matrix.id(), List.of(module)),
                        ignored -> true,
                        updated.temperature()
                );
                int current = Math.min(module.storedEnergy(), effectiveCapacity);
                int accepted = Math.min(
                        remaining,
                        Math.min(
                                storage.maxInput(),
                                Math.max(0, effectiveCapacity - current)
                        )
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

                int effectiveCapacity = MatrixOperations.calculateEnergyStorageCapacity(
                        new MatrixData(matrix.id(), List.of(module)),
                        ignored -> true,
                        updated.temperature()
                );
                int current = Math.min(module.storedEnergy(), effectiveCapacity);
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
