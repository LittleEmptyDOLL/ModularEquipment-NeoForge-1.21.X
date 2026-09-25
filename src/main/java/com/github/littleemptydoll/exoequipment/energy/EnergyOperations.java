package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
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
    public static EnergyTickResult tick(
            ExoskeletonData data
    ) {
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
        return tick(
                data,
                externalProvider,
                null
        );
    }

    public static EnergyTickResult tick(
            ExoskeletonData data,
            ExternalEnergyProvider externalProvider,
            Player player
    ) {
        if (externalProvider == null) {
            return tick(
                    data,
                    0,
                    amount -> amount,
                    player
            );
        }

        int externalAvailable =
                externalProvider.availableEnergy();

        if (externalAvailable < 0) {
            throw new IllegalArgumentException(
                    "External available energy cannot be negative"
            );
        }

        return tick(
                data,
                externalAvailable,
                amount ->
                        externalProvider.extractEnergy(
                                amount,
                                false
                        ),
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
    public static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable
    ) {
        if (externalAvailable < 0) {
            throw new IllegalArgumentException(
                    "External available energy cannot be negative"
            );
        }

        return tick(
                data,
                externalAvailable,
                amount -> amount,
                null
        );
    }

    private static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable,
            ExternalEnergyExtractor externalExtractor
    ) {
        return tick(
                data,
                externalAvailable,
                externalExtractor,
                null
        );
    }

    private static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable,
            ExternalEnergyExtractor externalExtractor,
            Player player
    ) {
        if (data.energySystem().isEmpty()) {
            return new EnergyTickResult(
                    data,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    Set.of()
            );
        }

        ResourceLocation energySystemId =
                data.energySystem()
                        .get()
                        .definitionId();

        var energySystem =
                ModEnergySystems.getDefinition(
                        energySystemId
                );

        var state =
                ExoskeletonState.calculateState(data);

        StorageLayout storageLayout =
                StorageLayout.create(data);

        int remainingInput =
                energySystem.maxInput();

        int remainingOutput =
                energySystem.maxOutput();

        int generated =
                state.energyGeneration();

        int generatedAvailable =
                Math.min(
                        generated,
                        remainingInput
                );

        remainingInput -= generatedAvailable;

        int externalInput = 0;
        int consumed = 0;
        int discharged = 0;
        int charged = 0;
        int generatedCharged = 0;

        Set<InstalledModuleReference> poweredModules =
                new LinkedHashSet<>();

        List<EnergyConsumer> consumers =
                collectConsumers(data, player);

        consumers.sort(
                Comparator.comparingInt(
                                EnergyConsumer::priority
                        )
                        .reversed()
        );

        for (EnergyConsumer consumer : consumers) {
            int required =
                    consumer.consumption();

            if (required > remainingOutput
                    || required
                    > remainingInput + generatedAvailable) {
                continue;
            }

            int availableExternal =
                    Math.min(
                            externalAvailable,
                            remainingInput
                    );

            int availableBattery =
                    Math.min(
                            remainingInput,
                            Math.min(
                                    remainingOutput,
                                    storageLayout.availableOutput()
                            )
                    );

            if (generatedAvailable
                    + availableExternal
                    + availableBattery
                    < required) {
                continue;
            }

            int fromGenerated =
                    Math.min(
                            required,
                            generatedAvailable
                    );

            int remaining =
                    required - fromGenerated;

            int fromExternal =
                    Math.min(
                            remaining,
                            availableExternal
                    );

            remaining -= fromExternal;

            int fromBattery = remaining;

            if (fromExternal > 0) {
                int extracted =
                        externalExtractor.extract(
                                fromExternal
                        );

                fromExternal =
                        Math.min(
                                extracted,
                                fromExternal
                        );

                externalInput += fromExternal;
                externalAvailable -= fromExternal;
                remainingInput -= fromExternal;
            }

            if (fromBattery > 0) {
                fromBattery =
                        storageLayout.discharge(
                                fromBattery
                        );

                discharged += fromBattery;
                remainingInput -= fromBattery;
            }

            int supplied =
                    fromGenerated
                            + fromExternal
                            + fromBattery;

            if (supplied != required) {
                continue;
            }

            generatedAvailable -= fromGenerated;
            consumed += supplied;
            remainingOutput -= supplied;

            poweredModules.add(
                    consumer.reference()
            );
        }

        if (generatedAvailable > 0
                && remainingOutput > 0) {

            int charge =
                    Math.min(
                            generatedAvailable,
                            Math.min(
                                    remainingOutput,
                                    storageLayout.availableInput()
                            )
                    );

            if (charge > 0) {
                int transferred =
                        storageLayout.charge(charge);

                charged += transferred;
                generatedCharged = transferred;
                generatedAvailable -= transferred;
                remainingOutput -= transferred;
            }
        }

        if (externalAvailable > 0
                && remainingInput > 0
                && remainingOutput > 0) {

            int chargeRequested =
                    Math.min(
                            externalAvailable,
                            Math.min(
                                    remainingInput,
                                    Math.min(
                                            remainingOutput,
                                            storageLayout.availableInput()
                                    )
                            )
                    );

            int externalCharge =
                    externalExtractor.extract(
                            chargeRequested
                    );

            externalCharge =
                    Math.min(
                            externalCharge,
                            chargeRequested
                    );

            if (externalCharge > 0) {
                int transferred =
                        storageLayout.charge(
                                externalCharge
                        );

                charged += transferred;
                externalInput += transferred;
                externalAvailable -= transferred;
                remainingInput -= transferred;
                remainingOutput -= transferred;
            }
        }

        ExoskeletonData updatedData =
                storageLayout.apply(data);

        int deficit =
                Math.max(
                        0,
                        state.energyConsumption()
                                - consumed
                );

        int generatedUsed =
                generated
                        - generatedAvailable
                        - generatedCharged;

        int wasted =
                Math.max(
                        0,
                        generated - generatedUsed
                );

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

    public static int calculateCurrentConsumption(
            ExoskeletonData data,
            Player player,
            Set<InstalledModuleReference> poweredModules
    ) {
        return collectConsumers(data, player)
                .stream()
                .filter(consumer ->
                        poweredModules == null
                                || poweredModules.contains(
                                        consumer.reference()
                                )
                )
                .mapToInt(
                        EnergyConsumer::consumption
                )
                .sum();
    }

    public static EnergyConsumptionResult consumeEnergy(
            ExoskeletonData data,
            int amount
    ) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Energy amount cannot be negative"
            );
        }

        if (amount == 0) {
            return new EnergyConsumptionResult(
                    data,
                    true,
                    0
            );
        }

        if (data.energySystem().isEmpty()) {
            return new EnergyConsumptionResult(
                    data,
                    false,
                    0
            );
        }

        var energySystem =
                ModEnergySystems.getDefinition(
                        data.energySystem()
                                .get()
                                .definitionId()
                );

        int requested =
                Math.min(
                        amount,
                        energySystem.maxOutput()
                );

        if (requested < amount) {
            return new EnergyConsumptionResult(
                    data,
                    false,
                    0
            );
        }

        StorageLayout storageLayout =
                StorageLayout.create(data);

        int transferred =
                storageLayout.discharge(amount);

        return new EnergyConsumptionResult(
                storageLayout.apply(data),
                transferred == amount,
                transferred
        );
    }

    private static List<EnergyConsumer> collectConsumers(
            ExoskeletonData data,
            Player player
    ) {
        List<EnergyConsumer> consumers =
                new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            InstalledModule module =
                    activeModule.module();

            ModuleDefinition definition =
                    activeModule.definition();

            var energy =
                    definition.energy();

            int consumption =
                    energy.map(
                            EnergyProperties::consumption
                    ).orElse(0);

            if (consumption > 0) {
                double efficiency =
                        TemperatureOperations
                                .calculateModuleEfficiency(
                                        definition,
                                        data.temperature()
                                );

                consumption =
                        Math.max(
                                0,
                                (int) Math.round(
                                        consumption * efficiency
                                )
                        );
            }

            if (module.active()) {
                consumption +=
                        definition.cloaking()
                                .map(
                                        CloakingProperties::activeConsumption
                                )
                                .orElse(0);
            }

            if (module.flightActive()) {
                consumption +=
                        definition.flight()
                                .map(
                                        FlightProperties::activeConsumption
                                )
                                .orElse(0);
            }

            boolean jetpackActive =
                    player != null
                            && JetpackInputState
                            .isEnergyActive(player)
                            && definition.jetpack()
                            .isPresent();

            if (jetpackActive) {
                consumption +=
                        definition.jetpack()
                                .map(
                                        JetpackProperties::energyConsumption
                                )
                                .orElse(0);
            }

            if (consumption <= 0
                    && !jetpackActive) {
                continue;
            }

            consumers.add(
                    new EnergyConsumer(
                            activeModule.reference(),
                            consumption,
                            energy.map(
                                    EnergyProperties::priority
                            ).orElse(0)
                    )
            );
        }

        return consumers;
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

    private static final class StorageLayout {
        private final List<StorageEntry> entries;

        private StorageLayout(
                List<StorageEntry> entries
        ) {
            this.entries = entries;
        }

        private static StorageLayout create(
                ExoskeletonData data
        ) {
            List<StorageEntry> entries =
                    new ArrayList<>();

            for (int slot
                    : ExoskeletonState.activeMatrixSlots(data)) {

                var matrix =
                        data.matrices()
                                .get(slot)
                                .matrix()
                                .orElse(null);

                if (matrix == null) {
                    continue;
                }

                for (int moduleIndex = 0;
                     moduleIndex < matrix.modules().size();
                     moduleIndex++) {

                    InstalledModule module =
                            matrix.modules()
                                    .get(moduleIndex);

                    ModuleDefinition definition =
                            ModModules.getDefinition(
                                    module.id()
                            );

                    StorageProperties storage =
                            definition.storage()
                                    .orElse(null);

                    if (storage == null) {
                        continue;
                    }

                    double efficiency =
                            TemperatureOperations
                                    .calculateModuleEfficiency(
                                            definition,
                                            data.temperature()
                                    );

                    int effectiveCapacity =
                            Math.max(
                                    0,
                                    (int) Math.round(
                                            storage.capacity()
                                                    * efficiency
                                    )
                            );

                    entries.add(
                            new StorageEntry(
                                    new InstalledModuleReference(
                                            slot,
                                            moduleIndex
                                    ),
                                    storage,
                                    effectiveCapacity,
                                    module.storedEnergy()
                            )
                    );
                }
            }

            return new StorageLayout(entries);
        }

        private int availableOutput() {
            int available = 0;

            for (StorageEntry entry : entries) {
                available +=
                        Math.min(
                                entry.storage.maxOutput(),
                                entry.stored
                        );
            }

            return available;
        }

        private int availableInput() {
            int available = 0;

            for (StorageEntry entry : entries) {
                available +=
                        Math.min(
                                entry.storage.maxInput(),
                                Math.max(
                                        0,
                                        entry.capacity
                                                - entry.stored
                                )
                        );
            }

            return available;
        }

        private int discharge(int amount) {
            int remaining = amount;
            int transferred = 0;

            for (StorageEntry entry : entries) {
                if (remaining <= 0) {
                    break;
                }

                entry.markClampedIfNeeded();

                int extracted =
                        Math.min(
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

        private int charge(int amount) {
            int remaining = amount;
            int transferred = 0;

            for (StorageEntry entry : entries) {
                if (remaining <= 0) {
                    break;
                }

                entry.markClampedIfNeeded();

                int accepted =
                        Math.min(
                                remaining,
                                Math.min(
                                        entry.storage.maxInput(),
                                        Math.max(
                                                0,
                                                entry.capacity
                                                        - entry.stored
                                        )
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

        private ExoskeletonData apply(
                ExoskeletonData data
        ) {
            ExoskeletonData updated = data;

            for (StorageEntry entry : entries) {
                if (!entry.dirty) {
                    continue;
                }

                int stored = entry.stored;

                updated = ExoskeletonModules.update(
                        updated,
                        entry.reference,
                        module ->
                                module.withStoredEnergy(
                                        stored
                                )
                );
            }

            return updated;
        }
    }

    private static final class StorageEntry {
        private final InstalledModuleReference reference;
        private final StorageProperties storage;
        private final int capacity;
        private final int originalStored;
        private int stored;
        private boolean dirty;

        private StorageEntry(
                InstalledModuleReference reference,
                StorageProperties storage,
                int capacity,
                int stored
        ) {
            this.reference = reference;
            this.storage = storage;
            this.capacity = capacity;
            this.originalStored = stored;
            this.stored =
                    Math.min(
                            stored,
                            capacity
                    );
        }

        private void markClampedIfNeeded() {
            if (stored != originalStored) {
                dirty = true;
            }
        }
    }

    @FunctionalInterface
    private interface ExternalEnergyExtractor {
        int extract(int amount);
    }
}
