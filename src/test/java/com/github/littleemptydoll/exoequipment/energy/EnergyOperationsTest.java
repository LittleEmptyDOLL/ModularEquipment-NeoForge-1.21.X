package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonProfile;
import com.github.littleemptydoll.exoequipment.exoskeleton.MatrixSlot;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.registry.ModControllers;
import com.github.littleemptydoll.exoequipment.registry.ModEnergySystems;
import com.github.littleemptydoll.exoequipment.registry.ModFrames;
import com.github.littleemptydoll.exoequipment.registry.TestModules;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

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
