package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;
import com.github.littleemptydoll.exoequipment.module.BlockScannerProperties;
import com.github.littleemptydoll.exoequipment.module.CloakingProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.EntityDetectionProperties;
import com.github.littleemptydoll.exoequipment.module.FlightProperties;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.JetpackInputState;
import com.github.littleemptydoll.exoequipment.module.JetpackProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ThermalVisionProperties;
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
 * <p>Surplus charges the shared buffer first, then batteries. A battery can
 * refill the buffer between bursts within its discharge and bus limits.
 * Bursts spend only the charged buffer and can exceed the per-tick bus output.</p>
 */
public final class EnergyOperations {
    private EnergyOperations() {}

    /** Accepts charger energy into the buffer first, then active batteries. */
    public static EnergyReceiveResult receiveExternalEnergy(
            ExoskeletonData data, int maxReceive, boolean simulate
    ) {
        if (maxReceive <= 0 || data.energySystem().isEmpty()) {
            return new EnergyReceiveResult(data, 0);
        }

        var system = data.energySystem().orElseThrow();
        var definition = ModEnergySystems.getDefinition(system.definitionId());
        int maxInput = definition.maxInput();
        EnergyStorageLayout storage = EnergyStorageLayout.create(data);
        int bufferRoom = Math.max(0, definition.bufferCapacity() - system.bufferStored());
        int accepted = Math.min(maxReceive,
                Math.min(maxInput, bufferRoom + storage.availableInput()));

        if (simulate || accepted == 0) {
            return new EnergyReceiveResult(data, accepted);
        }

        int toBuffer = Math.min(bufferRoom, accepted);
        int toBattery = storage.charge(accepted - toBuffer);
        return new EnergyReceiveResult(storage.apply(data.withEnergySystem(
                system.withBufferStored(system.bufferStored() + toBuffer))), toBuffer + toBattery);
    }

    public record EnergyReceiveResult(ExoskeletonData data, int received) {}

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
        return tick(data, externalProvider, player, 0);
    }

    public static EnergyTickResult tick(
            ExoskeletonData data,
            ExternalEnergyProvider externalProvider,
            Player player,
            int inputAlreadyUsed
    ) {
        if (inputAlreadyUsed < 0) {
            throw new IllegalArgumentException("Input already used cannot be negative");
        }
        if (externalProvider == null) {
            return tick(
                    data,
                    0,
                    (amount, simulate) -> amount,
                    player,
                    inputAlreadyUsed
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
                player,
                inputAlreadyUsed
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
        return tick(data, externalAvailable, 0);
    }

    public static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable,
            int inputAlreadyUsed
    ) {
        if (externalAvailable < 0) {
            throw new IllegalArgumentException(
                    "External available energy cannot be negative"
            );
        }
        if (inputAlreadyUsed < 0) {
            throw new IllegalArgumentException("Input already used cannot be negative");
        }

        return tick(
                data,
                externalAvailable,
                (amount, simulate) -> amount,
                null,
                inputAlreadyUsed
        );
    }

    private static EnergyTickResult tick(
            ExoskeletonData data,
            int externalAvailable,
            ExternalEnergySource externalSource,
            Player player,
            int inputAlreadyUsed
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
        int buffer = Math.min(data.energySystem().orElseThrow().bufferStored(),
                energySystem.bufferCapacity());

        int remainingInput = Math.max(0, energySystem.maxInput() - inputAlreadyUsed);
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
            int availableBuffer = Math.min(buffer, remainingInput);

            if (generatedAvailable + Math.min(remainingInput,
                    availableExternal + availableBattery + availableBuffer) < required) {
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
                fromBattery = storage.discharge(Math.min(remaining,
                        remainingInput - fromExternal));
                remaining -= fromBattery;
            }

            int fromBuffer = Math.min(remaining,
                    Math.min(buffer, remainingInput - fromExternal - fromBattery));
            remaining -= fromBuffer;
            buffer -= fromBuffer;

            if (remaining > 0) {
                // A well-behaved external provider returns the amount it
                // reported during simulation. If it changes concurrently,
                // do not mark the module as powered with partial energy.
                externalInput += fromExternal;
                externalAvailable -= fromExternal;
                remainingInput -= fromExternal + fromBattery + fromBuffer;
                discharged += fromBattery + fromBuffer;
                continue;
            }

            generatedAvailable -= fromGenerated;
            externalInput += fromExternal;
            externalAvailable -= fromExternal;
            remainingInput -= fromExternal + fromBattery + fromBuffer;
            discharged += fromBattery + fromBuffer;
            consumed += required;
            remainingOutput -= required;

            poweredModules.add(consumer.reference());
        }

        // Fill the shared reserve before batteries. All transfers into it
        // happen during ticks; a burst never pulls from a battery directly.
        int bufferRoom = Math.max(0, energySystem.bufferCapacity() - buffer);
        int fromGenerated = Math.min(bufferRoom, Math.min(generatedAvailable, remainingOutput));
        buffer += fromGenerated;
        charged += fromGenerated;
        generatedAvailable -= fromGenerated;
        remainingOutput -= fromGenerated;
        bufferRoom -= fromGenerated;

        int externalRequested = Math.min(bufferRoom,
                Math.min(externalAvailable, Math.min(remainingInput, remainingOutput)));
        int fromExternal = extractExternal(externalSource, externalRequested, false);
        buffer += fromExternal;
        charged += fromExternal;
        externalInput += fromExternal;
        externalAvailable -= fromExternal;
        remainingInput -= fromExternal;
        remainingOutput -= fromExternal;
        bufferRoom -= fromExternal;

        // A battery can refill an empty reserve over multiple ticks. Its
        // output and the energy bus input/output budgets limit that transfer.
        int fromBattery = storage.discharge(Math.min(bufferRoom,
                Math.min(remainingInput, Math.min(remainingOutput, storage.availableOutput()))));
        buffer += fromBattery;
        charged += fromBattery;
        discharged += fromBattery;
        remainingInput -= fromBattery;
        remainingOutput -= fromBattery;

        if (generatedAvailable > 0 && remainingOutput > 0) {
            int requested = Math.min(generatedAvailable,
                    Math.min(remainingOutput, storage.availableInput()));
            int transferred = storage.charge(requested);
            charged += transferred;
            generatedAvailable -= transferred;
            remainingOutput -= transferred;
        }

        if (externalAvailable > 0 && remainingInput > 0 && remainingOutput > 0) {
            int requested = Math.min(externalAvailable,
                    Math.min(remainingInput, Math.min(remainingOutput, storage.availableInput())));
            int extracted = extractExternal(externalSource, requested, false);
            int transferred = storage.charge(extracted);
            charged += transferred;
            externalInput += transferred;
            externalAvailable -= transferred;
            remainingInput -= transferred;
            remainingOutput -= transferred;
        }

        ExoskeletonData updatedData = data.withEnergySystem(
                data.energySystem().orElseThrow().withBufferStored(buffer));
        updatedData = storage.apply(updatedData);

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

        var system = data.energySystem().orElseThrow();
        int available = Math.min(system.bufferStored(),
                ModEnergySystems.getDefinition(system.definitionId()).bufferCapacity());
        if (amount > available) {
            return new EnergyConsumptionResult(data, false, 0);
        }
        return new EnergyConsumptionResult(
                data.withEnergySystem(system.withBufferStored(available - amount)),
                true, amount);

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
            int consumption = moduleConsumption(data, module, definition, player);

            if (consumption <= 0) {
                continue;
            }

            consumers.add(new EnergyConsumer(
                    activeModule.reference(),
                    consumption,
                    definition.energy().map(EnergyProperties::priority)
                            .orElse(0)
            ));
        }

        return consumers;
    }

    public static int calculateMatrixConsumption(
            ExoskeletonData data, int matrixSlot, Player player
    ) {
        return ExoskeletonModules.supportedInMatrix(data, matrixSlot).stream()
                .mapToInt(activeModule -> moduleConsumption(
                        data, activeModule.module(), activeModule.definition(), player
                ))
                .sum();
    }

    private static int moduleConsumption(
            ExoskeletonData data, InstalledModule module,
            ModuleDefinition definition, Player player
    ) {
        int consumption = definition.energy()
                .map(EnergyProperties::consumption).orElse(0);

        if (consumption > 0) {
            double efficiency = TemperatureOperations.calculateModuleEfficiency(
                    definition, data.temperature());
            consumption = Math.max(0, (int) Math.round(consumption * efficiency));
        }

        if (module.active()) {
            consumption += definition.cloaking()
                    .map(CloakingProperties::activeConsumption).orElse(0);
            consumption += definition.entityDetection()
                    .map(EntityDetectionProperties::activeConsumption).orElse(0);
            consumption += definition.blockScanner()
                    .map(BlockScannerProperties::activeConsumption).orElse(0);
            consumption += definition.thermalVision()
                    .map(ThermalVisionProperties::activeConsumption).orElse(0);
        }
        if (module.flightActive()) {
            consumption += definition.flight()
                    .map(FlightProperties::activeConsumption).orElse(0);
        }
        if (player != null && JetpackInputState.isEnergyActive(player)) {
            consumption += definition.jetpack()
                    .map(JetpackProperties::energyConsumption).orElse(0);
        }

        return consumption;
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
