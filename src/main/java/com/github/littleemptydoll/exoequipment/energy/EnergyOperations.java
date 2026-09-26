package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import com.github.littleemptydoll.exoequipment.module.CloakingProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.FlightProperties;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.JetpackInputState;
import com.github.littleemptydoll.exoequipment.module.JetpackProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.registry.ModEnergySystems;
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

    public static EnergyTickResult tick(ExoskeletonData data) {
        return tick(data, 0);
    }

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
            return tick(
                    data,
                    0,
                    (amount, simulate) -> amount,
                    player
            );
        }

        int reportedAvailable = externalProvider.availableEnergy();

        if (reportedAvailable < 0) {
            throw new IllegalArgumentException(
                    "External available energy cannot be negative"
            );
        }

        ExternalEnergySource source = externalProvider::extractEnergy;
        int externalAvailable = extractExternal(
                source,
                reportedAvailable,
                true
        );

        return tick(
                data,
                externalAvailable,
                source,
                player
        );
    }

    /**
     * Simulates a tick with a numeric amount of external energy available.
     * No external storage is mutated by this overload.
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
                (amount, simulate) -> amount,
                null
        );
    }

    private static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable,
            ExternalEnergySource externalSource,
            Player player
    ) {
        if (data.energySystem().isEmpty()) {
            return emptyResult(data);
        }

        ResourceLocation energySystemId =
                data.energySystem()
                        .get()
                        .definitionId();

        var energySystem =
                ModEnergySystems.getDefinition(energySystemId);

        var state = ExoskeletonState.calculateState(data);
        EnergyStorageLayout storage =
                EnergyStorageLayout.create(data);

        int remainingInput = energySystem.maxInput();
        int remainingOutput = energySystem.maxOutput();

        int generated = state.energyGeneration();
        int generatedAdmitted = Math.min(
                generated,
                remainingInput
        );
        int generatedAvailable = generatedAdmitted;
        remainingInput -= generatedAdmitted;

        int externalInput = 0;
        int consumed = 0;
        int discharged = 0;
        int charged = 0;

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

        int totalDemand = consumers.stream()
                .mapToInt(EnergyConsumer::consumption)
                .sum();

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
                            storage.availableOutput()
                    )
            );

            if (generatedAvailable
                    + availableExternal
                    + availableBattery
                    < required) {
                continue;
            }

            int fromGenerated = Math.min(
                    required,
                    generatedAvailable
            );
            int remaining = required - fromGenerated;

            int externalRequested = Math.min(
                    remaining,
                    availableExternal
            );
            int fromExternal = extractExternal(
                    externalSource,
                    externalRequested,
                    false
            );
            remaining -= fromExternal;

            int fromBattery = 0;
            if (remaining > 0) {
                fromBattery = storage.discharge(remaining);
                remaining -= fromBattery;
            }

            if (remaining > 0) {
                // A well-behaved external provider returns the amount it
                // reported during simulation. If it changes concurrently,
                // do not mark the module as powered with partial energy.
                externalInput += fromExternal;
                externalAvailable -= fromExternal;
                remainingInput -= fromExternal + fromBattery;
                discharged += fromBattery;
                continue;
            }

            generatedAvailable -= fromGenerated;
            externalInput += fromExternal;
            externalAvailable -= fromExternal;
            remainingInput -= fromExternal + fromBattery;
            discharged += fromBattery;
            consumed += required;
            remainingOutput -= required;

            poweredModules.add(consumer.reference());
        }

        if (generatedAvailable > 0
                && remainingOutput > 0) {
            int chargeRequested = Math.min(
                    generatedAvailable,
                    Math.min(
                            remainingOutput,
                            storage.availableInput()
                    )
            );

            int transferred = storage.charge(chargeRequested);
            charged += transferred;
            generatedAvailable -= transferred;
            remainingOutput -= transferred;
        }

        if (externalAvailable > 0
                && remainingInput > 0
                && remainingOutput > 0) {
            int chargeRequested = Math.min(
                    externalAvailable,
                    Math.min(
                            remainingInput,
                            Math.min(
                                    remainingOutput,
                                    storage.availableInput()
                            )
                    )
            );

            int externalCharge = extractExternal(
                    externalSource,
                    chargeRequested,
                    false
            );

            if (externalCharge > 0) {
                int transferred = storage.charge(externalCharge);

                charged += transferred;
                externalInput += transferred;
                externalAvailable -= transferred;
                remainingInput -= transferred;
                remainingOutput -= transferred;
            }
        }

        ExoskeletonData updatedData = storage.apply(data);

        int deficit = Math.max(0, totalDemand - consumed);
        int generatedUsed =
                generatedAdmitted - generatedAvailable;
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

    private static EnergyTickResult emptyResult(
            ExoskeletonData data
    ) {
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
                .mapToInt(EnergyConsumer::consumption)
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

        var energySystem = ModEnergySystems.getDefinition(
                data.energySystem()
                        .get()
                        .definitionId()
        );

        if (amount > energySystem.maxOutput()) {
            return new EnergyConsumptionResult(
                    data,
                    false,
                    0
            );
        }

        EnergyStorageLayout storage =
                EnergyStorageLayout.create(data);
        int transferred = storage.discharge(amount);

        return new EnergyConsumptionResult(
                storage.apply(data),
                transferred == amount,
                transferred
        );
    }

    private static List<EnergyConsumer> collectConsumers(
            ExoskeletonData data,
            Player player
    ) {
        List<EnergyConsumer> consumers = new ArrayList<>();

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {
            InstalledModule module = activeModule.module();
            ModuleDefinition definition = activeModule.definition();
            var energy = definition.energy();

            int consumption = energy.map(
                    EnergyProperties::consumption
            ).orElse(0);

            if (consumption > 0) {
                double efficiency =
                        TemperatureOperations
                                .calculateModuleEfficiency(
                                        definition,
                                        data.temperature()
                                );

                consumption = Math.max(
                        0,
                        (int) Math.round(
                                consumption * efficiency
                        )
                );
            }

            if (module.active()) {
                consumption += definition.cloaking()
                        .map(CloakingProperties::activeConsumption)
                        .orElse(0);
            }

            if (module.flightActive()) {
                consumption += definition.flight()
                        .map(FlightProperties::activeConsumption)
                        .orElse(0);
            }

            boolean jetpackActive =
                    player != null
                            && JetpackInputState.isEnergyActive(player)
                            && definition.jetpack().isPresent();

            if (jetpackActive) {
                consumption += definition.jetpack()
                        .map(JetpackProperties::energyConsumption)
                        .orElse(0);
            }

            if (consumption <= 0 && !jetpackActive) {
                continue;
            }

            consumers.add(new EnergyConsumer(
                    activeModule.reference(),
                    consumption,
                    energy.map(EnergyProperties::priority)
                            .orElse(0)
            ));
        }

        return consumers;
    }

    private static int extractExternal(
            ExternalEnergySource source,
            int requested,
            boolean simulate
    ) {
        if (requested <= 0) {
            return 0;
        }

        return Math.max(
                0,
                Math.min(
                        requested,
                        source.extract(requested, simulate)
                )
        );
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

    @FunctionalInterface
    private interface ExternalEnergySource {
        int extract(int amount, boolean simulate);
    }
}
