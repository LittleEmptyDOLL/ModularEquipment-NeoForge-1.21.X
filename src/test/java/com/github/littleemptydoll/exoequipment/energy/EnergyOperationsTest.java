package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.characteristics.CharacteristicsContext;
import com.github.littleemptydoll.exoequipment.characteristics.CharacteristicsProvider;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonProfile;
import com.github.littleemptydoll.exoequipment.exoskeleton.MatrixSlot;
import com.github.littleemptydoll.exoequipment.exoskeleton.SystemStatus;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.SensorToggleOperations;
import com.github.littleemptydoll.exoequipment.module.ThermalVisionOperations;
import com.github.littleemptydoll.exoequipment.registry.ModControllers;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModEnergySystems;
import com.github.littleemptydoll.exoequipment.registry.ModExoskeletons;
import com.github.littleemptydoll.exoequipment.registry.ModFrames;
import com.github.littleemptydoll.exoequipment.registry.TestModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnergyOperationsTest {
    private static final ResourceLocation MATRIX_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "exoequipment",
                    "energy_test"
            );

    @Test
    void rejectsNegativeExternalEnergy() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EnergyOperations.tick(
                        ExoskeletonData.empty(),
                        -1
                )
        );
    }

    @Test
    void itemCapabilityReceivesEnergyIntoActiveBattery() {
        ExoskeletonData data = data(
                ModFrames.EXPERIMENTAL.getDefinition().id(),
                new InstalledModule(TestModules.TEST_BATTERY.getDefinition().id(),
                        0, 0, 0, 9_950)
        );
        ItemStack stack = new ItemStack(ModExoskeletons.BASIC.getItem());
        stack.set(ModDataComponents.EXOSKELETON_DATA.get(), data);

        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        assertTrue(energy != null && energy.canReceive());
        assertFalse(energy.canExtract());
        assertEquals(10_000, energy.getMaxEnergyStored());
        assertEquals(9_950, energy.getEnergyStored());
        assertEquals(50, energy.receiveEnergy(200, true));
        assertEquals(9_950, energy.getEnergyStored());
        assertEquals(50, energy.receiveEnergy(200, false));
        assertEquals(10_000, energy.getEnergyStored());
        assertEquals(10_000, module(ExoskeletonItem.getData(stack), 0).storedEnergy());
        assertEquals(0, energy.receiveEnergy(200, false));
    }

    @Test
    void itemChargingRespectsStorageInputAndMissingEnergySystem() {
        ExoskeletonData data = data(
                ModFrames.EXPERIMENTAL.getDefinition().id(),
                new InstalledModule(TestModules.TEST_BATTERY.getDefinition().id(),
                        0, 0, 0)
        );
        var simulation = EnergyOperations.receiveExternalEnergy(data, 200, true);
        assertEquals(100, simulation.received());
        assertEquals(0, module(simulation.data(), 0).storedEnergy());

        var actual = EnergyOperations.receiveExternalEnergy(data, 200, false);
        assertEquals(100, actual.received());
        assertEquals(100, module(actual.data(), 0).storedEnergy());
        assertEquals(0, EnergyOperations.receiveExternalEnergy(
                ExoskeletonData.empty(), 200, false).received());
    }

    @Test
    void multipleChargersShareItemInputLimitWithinTick() {
        ExoskeletonData data = data(
                ModFrames.EXPERIMENTAL.getDefinition().id(),
                new InstalledModule(TestModules.TEST_BATTERY.getDefinition().id(),
                        0, 0, 0)
        );
        ItemStack stack = new ItemStack(ModExoskeletons.BASIC.getItem());
        stack.set(ModDataComponents.EXOSKELETON_DATA.get(), data);

        assertEquals(100, new ExoskeletonEnergyStorage(stack)
                .receiveEnergyAtTick(1_000, true, 42));
        assertEquals(0, new ExoskeletonEnergyStorage(stack).getEnergyStored());
        assertEquals(100, new ExoskeletonEnergyStorage(stack)
                .receiveEnergyAtTick(1_000, false, 42));
        assertEquals(100, new ExoskeletonEnergyStorage(stack)
                .receiveEnergyAtTick(1_000, false, 42));
        assertEquals(50, new ExoskeletonEnergyStorage(stack)
                .receiveEnergyAtTick(1_000, false, 42));
        assertEquals(0, new ExoskeletonEnergyStorage(stack)
                .receiveEnergyAtTick(1_000, false, 42));
        assertEquals(250, new ExoskeletonEnergyStorage(stack).getEnergyStored());
        assertEquals(100, new ExoskeletonEnergyStorage(stack)
                .receiveEnergyAtTick(1_000, false, 43));
    }

    @Test
    void internalTickUsesRemainingInputAfterItemCharging() {
        ExoskeletonData data = data(
                ModFrames.EXPERIMENTAL.getDefinition().id(),
                new InstalledModule(TestModules.TEST_NIGHT_VISION.getDefinition().id(),
                        0, 0, 0)
        );

        EnergyTickResult result = EnergyOperations.tick(data, 100, 250);
        assertEquals(0, result.consumed());
        assertEquals(0, result.externalInput());
        assertFalse(result.isPowered(new InstalledModuleReference(0, 0)));
    }

    @Test
    void higherPriorityConsumerIsPoweredFirst() {
        ExoskeletonData data = data(
                ModFrames.CIVILIAN.getDefinition().id(),
                new InstalledModule(
                        TestModules.TEST_NIGHT_VISION
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                ),
                new InstalledModule(
                        TestModules.TEST_JETPACK
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                )
        );

        EnergyTickResult result =
                EnergyOperations.tick(data, 5);

        assertEquals(5, result.consumed());
        assertFalse(
                result.isPowered(
                        new InstalledModuleReference(0, 0)
                )
        );
        assertTrue(
                result.isPowered(
                        new InstalledModuleReference(0, 1)
                )
        );
    }

    @Test
    void generatorSurplusChargedIntoBatteryIsNotWasted() {
        ExoskeletonData data = data(
                ModFrames.EXPERIMENTAL.getDefinition().id(),
                new InstalledModule(
                        TestModules.TEST_GENERATOR
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                ),
                new InstalledModule(
                        TestModules.TEST_BATTERY
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                )
        );

        EnergyTickResult result =
                EnergyOperations.tick(data);

        assertTrue(result.generated() > 0);
        assertEquals(result.generated(), result.charged());
        assertEquals(0, result.wasted());
        assertEquals(
                result.charged(),
                module(result.data(), 1).storedEnergy()
        );
    }

    @Test
    void unsupportedBatteryCannotSupplyEnergy() {
        ExoskeletonData data = data(
                ModFrames.CIVILIAN.getDefinition().id(),
                new InstalledModule(
                        TestModules.TEST_BATTERY
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0,
                        100
                )
        );

        EnergyOperations.EnergyConsumptionResult result =
                EnergyOperations.consumeEnergy(data, 50);

        assertFalse(result.sufficient());
        assertEquals(0, result.consumed());
        assertEquals(
                100,
                module(result.data(), 0).storedEnergy()
        );
    }

    @Test
    void batteryOutputUsesTemperatureScaledLimit() {
        ExoskeletonData data = data(
                ModFrames.EXPERIMENTAL.getDefinition().id(),
                new InstalledModule(
                        TestModules.TEST_BATTERY
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0,
                        1000
                )
        ).withTemperature(150.0D);

        MatrixData matrix = data.matrices()
                .get(0)
                .matrix()
                .orElseThrow();
        int expectedOutput =
                MatrixOperations.calculateEnergyStorageOutput(
                        matrix,
                        module -> true,
                        data.temperature()
                );

        EnergyOperations.EnergyConsumptionResult result =
                EnergyOperations.consumeEnergy(data, 50);

        assertTrue(expectedOutput < 50);
        assertFalse(result.sufficient());
        assertEquals(expectedOutput, result.consumed());
        assertEquals(
                1000 - expectedOutput,
                module(result.data(), 0).storedEnergy()
        );
    }

    @Test
    void externalTransferLimitIsRespectedBeforePoweringConsumer() {
        ExoskeletonData data = data(
                ModFrames.CIVILIAN.getDefinition().id(),
                new InstalledModule(
                        TestModules.TEST_NIGHT_VISION
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                )
        );
        LimitedExternalProvider provider =
                new LimitedExternalProvider(100, 3);

        EnergyTickResult result =
                EnergyOperations.tick(data, provider);

        assertEquals(0, result.consumed());
        assertEquals(0, result.externalInput());
        assertEquals(0, provider.extracted());
        assertFalse(
                result.isPowered(
                        new InstalledModuleReference(0, 0)
                )
        );
    }

    @Test
    void deficitIncludesActiveAbilityConsumption() {
        InstalledModule cloaking =
                new InstalledModule(
                        TestModules.TEST_CLOAKING
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                ).withActive(true);

        EnergyTickResult result = EnergyOperations.tick(
                data(
                        ModFrames.CIVILIAN
                                .getDefinition()
                                .id(),
                        cloaking
                ),
                20
        );

        assertEquals(0, result.consumed());
        assertEquals(50, result.deficit());
    }

    @Test
    void activeCloakingChangesCharacteristicsAndStatusDemand() {
        InstalledModule generator = new InstalledModule(
                TestModules.TEST_GENERATOR.getDefinition().id(), 0, 0, 0);
        InstalledModule cloak = new InstalledModule(
                TestModules.TEST_CLOAKING.getDefinition().id(), 0, 0, 0);
        ExoskeletonData inactive = data(
                ModFrames.EXPERIMENTAL.getDefinition().id(), generator, cloak);
        ExoskeletonData active = data(
                ModFrames.EXPERIMENTAL.getDefinition().id(),
                generator, cloak.withActive(true));

        assertEquals(20, EnergyState.calculate(inactive).consumption());
        assertEquals(50, EnergyState.calculate(active).consumption());
        assertEquals(SystemStatus.GREEN, SystemStatus.calculate(inactive).severity());
        assertEquals(SystemStatus.YELLOW, SystemStatus.calculate(active).severity());

        assertEquals(50.0D, CharacteristicsProvider.collect(
                CharacteristicsContext.exoskeleton(active, null, Set.of()))
                .stream().filter(value -> value.key().equals("consumption"))
                .findFirst().orElseThrow().value());
        assertEquals(50.0D, CharacteristicsProvider.collect(
                CharacteristicsContext.matrix(active, null, Set.of(), 0))
                .stream().filter(value -> value.key().equals("consumption"))
                .findFirst().orElseThrow().value());
    }

    @Test
    void activeFlightAddsItsRunningCostToTheEnergySnapshot() {
        InstalledModule flight = new InstalledModule(
                TestModules.TEST_FLIGHT.getDefinition().id(), 0, 0, 0);
        ExoskeletonData inactive = data(
                ModFrames.CIVILIAN.getDefinition().id(), flight);
        ExoskeletonData active = data(
                ModFrames.CIVILIAN.getDefinition().id(), flight.withFlightActive(true));

        assertEquals(5, EnergyState.calculate(inactive).consumption());
        assertEquals(20, EnergyState.calculate(active).consumption());
        assertEquals(20, EnergyOperations.calculateMatrixConsumption(active, 0, null));
    }

    @Test
    void sensorsAreOffUntilEnabledAndToggleIndependently() {
        ExoskeletonData off = data(
                ModFrames.CIVILIAN.getDefinition().id(),
                new InstalledModule(TestModules.TEST_ENTITY_SENSOR.getDefinition().id(), 0, 0, 0),
                new InstalledModule(TestModules.TEST_BLOCK_SCANNER.getDefinition().id(), 0, 0, 0));

        assertEquals(2, EnergyState.calculate(off).consumption());
        assertEquals(2, EnergyOperations.tick(off, 10).consumed());

        ExoskeletonData entityOn = SensorToggleOperations.toggle(
                off, SensorToggleOperations.Kind.ENTITY);
        assertTrue(module(entityOn, 0).active());
        assertFalse(module(entityOn, 1).active());
        assertEquals(7, EnergyState.calculate(entityOn).consumption());

        ExoskeletonData bothOn = SensorToggleOperations.toggle(
                entityOn, SensorToggleOperations.Kind.BLOCK);
        assertEquals(12, EnergyState.calculate(bothOn).consumption());
        assertEquals(12, EnergyOperations.tick(bothOn, 12).consumed());

        ExoskeletonData entityOff = SensorToggleOperations.toggle(
                bothOn, SensorToggleOperations.Kind.ENTITY);
        assertFalse(module(entityOff, 0).active());
        assertTrue(module(entityOff, 1).active());
        assertEquals(7, EnergyState.calculate(entityOff).consumption());
    }

    @Test
    void thermalVisionNeedsActivationAndPower() {
        ExoskeletonData off = data(
                ModFrames.CIVILIAN.getDefinition().id(),
                new InstalledModule(TestModules.TEST_THERMAL_VISION.getDefinition().id(),
                        0, 0, 0)
        );
        var reference = new InstalledModuleReference(0, 0);

        assertEquals(2, EnergyState.calculate(off).consumption());
        assertEquals(0.0D, ThermalVisionOperations.range(off, Set.of(reference)));

        ExoskeletonData on = SensorToggleOperations.toggle(
                off, SensorToggleOperations.Kind.THERMAL);
        assertTrue(module(on, 0).active());
        assertEquals(7, EnergyState.calculate(on).consumption());
        assertEquals(0.0D, ThermalVisionOperations.range(on, Set.of()));
        assertEquals(12.0D, ThermalVisionOperations.range(on, Set.of(reference)));

        ExoskeletonData offAgain = SensorToggleOperations.toggle(
                on, SensorToggleOperations.Kind.THERMAL);
        assertEquals(2, EnergyState.calculate(offAgain).consumption());
    }

    private static ExoskeletonData data(
            ResourceLocation frameId,
            InstalledModule... modules
    ) {
        return new ExoskeletonData(
                Optional.of(new Frame(frameId)),
                Optional.of(new Controller(
                        ModControllers.CIVILIAN
                                .getDefinition()
                                .id()
                )),
                Optional.of(new EnergySystem(
                        ModEnergySystems.CIVILIAN
                                .getDefinition()
                                .id()
                )),
                List.of(
                        new MatrixSlot(Optional.of(
                                new MatrixData(
                                        MATRIX_ID,
                                        List.of(modules)
                                )
                        )),
                        MatrixSlot.empty(),
                        MatrixSlot.empty(),
                        MatrixSlot.empty()
                ),
                List.of(new ExoskeletonProfile(
                        "Energy",
                        List.of(0)
                )),
                0,
                20.0D
        );
    }

    private static InstalledModule module(
            ExoskeletonData data,
            int index
    ) {
        return data.matrices()
                .get(0)
                .matrix()
                .orElseThrow()
                .modules()
                .get(index);
    }

    private static final class LimitedExternalProvider
            implements ExternalEnergyProvider {
        private final int available;
        private final int transferLimit;
        private int extracted;

        private LimitedExternalProvider(
                int available,
                int transferLimit
        ) {
            this.available = available;
            this.transferLimit = transferLimit;
        }

        @Override
        public int availableEnergy() {
            return available - extracted;
        }

        @Override
        public int extractEnergy(
                int amount,
                boolean simulate
        ) {
            int transferred = Math.min(
                    amount,
                    Math.min(
                            transferLimit,
                            available - extracted
                    )
            );

            if (!simulate) {
                extracted += transferred;
            }

            return transferred;
        }

        private int extracted() {
            return extracted;
        }
    }
}
