package com.github.littleemptydoll.exoequipment.matrix;

import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.registry.ModMatrices;
import com.github.littleemptydoll.exoequipment.registry.TestModules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatrixOperationsTest {
    @Test
    void normalizesQuarterTurnRotations() {
        assertEquals(0, MatrixOperations.normalizeRotation(0));
        assertEquals(90, MatrixOperations.normalizeRotation(450));
        assertEquals(270, MatrixOperations.normalizeRotation(-90));
        assertThrows(
                IllegalArgumentException.class,
                () -> MatrixOperations.normalizeRotation(45)
        );
    }

    @Test
    void rotatedSizeSwapsDimensionsOnQuarterTurns() {
        ModuleSize size = new ModuleSize(1, 2);

        assertSame(size, MatrixOperations.getRotatedSize(size, 180));
        assertEquals(
                new ModuleSize(2, 1),
                MatrixOperations.getRotatedSize(size, 90)
        );
    }

    @Test
    void placementRejectsBoundsAndOverlap() {
        MatrixDefinition definition =
                ModMatrices.CIVILIAN.getDefinition();
        InstalledModule existing = new InstalledModule(
                TestModules.TEST_CONSUMER
                        .getDefinition()
                        .id(),
                1,
                1,
                0
        );
        MatrixData matrix = new MatrixData(
                definition.id(),
                List.of(existing)
        );

        assertFalse(MatrixOperations.canPlace(
                matrix,
                definition,
                TestModules.TEST_CONSUMER.getDefinition(),
                2,
                2,
                0
        ));
        assertFalse(MatrixOperations.canPlace(
                matrix,
                definition,
                TestModules.TEST_CONSUMER.getDefinition(),
                4,
                4,
                0
        ));
        assertTrue(MatrixOperations.canPlace(
                matrix,
                definition,
                TestModules.TEST_CONSUMER.getDefinition(),
                3,
                3,
                0
        ));
    }

    @Test
    void moveAndRotatePreserveRuntimeModuleState() {
        MatrixDefinition definition =
                ModMatrices.CIVILIAN.getDefinition();
        InstalledModule module = new InstalledModule(
                TestModules.TEST_HEALTH
                        .getDefinition()
                        .id(),
                0,
                0,
                0,
                123,
                12.5D,
                7,
                8,
                9,
                true,
                10,
                true
        );
        MatrixData matrix = new MatrixData(
                definition.id(),
                List.of(module)
        );

        MatrixData moved = MatrixOperations.moveModule(
                matrix,
                definition,
                0,
                0,
                2,
                2
        );
        InstalledModule movedModule =
                moved.modules().getFirst();

        assertEquals(2, movedModule.x());
        assertEquals(2, movedModule.y());
        assertRuntimeState(module, movedModule);

        MatrixData rotated = MatrixOperations.rotateModule(
                moved,
                definition,
                2,
                2
        );
        InstalledModule rotatedModule =
                rotated.modules().getFirst();

        assertEquals(90, rotatedModule.rotation());
        assertRuntimeState(module, rotatedModule);
    }

    @Test
    void stateCalculationHonorsSupportPredicate() {
        MatrixData matrix = new MatrixData(
                ModMatrices.EXPERIMENTAL
                        .getDefinition()
                        .id(),
                List.of(
                        new InstalledModule(
                                TestModules.TEST_CONSUMER
                                        .getDefinition()
                                        .id(),
                                0,
                                0,
                                0
                        ),
                        new InstalledModule(
                                TestModules.TEST_GENERATOR
                                        .getDefinition()
                                        .id(),
                                2,
                                0,
                                0
                        )
                )
        );

        MatrixState all = MatrixOperations.calculateState(matrix);
        MatrixState consumersOnly = MatrixOperations.calculateState(
                matrix,
                module -> module.id().equals(
                        TestModules.TEST_CONSUMER
                                .getDefinition()
                                .id()
                )
        );

        assertEquals(20, all.energyConsumption());
        assertEquals(40, all.energyGeneration());
        assertEquals(20, consumersOnly.energyConsumption());
        assertEquals(0, consumersOnly.energyGeneration());
    }

    private static void assertRuntimeState(
            InstalledModule expected,
            InstalledModule actual
    ) {
        assertEquals(
                expected.storedEnergy(),
                actual.storedEnergy()
        );
        assertEquals(
                expected.shieldEnergy(),
                actual.shieldEnergy()
        );
        assertEquals(
                expected.shieldRechargeCooldown(),
                actual.shieldRechargeCooldown()
        );
        assertEquals(
                expected.revivalCooldown(),
                actual.revivalCooldown()
        );
        assertEquals(
                expected.emergencyShieldCooldown(),
                actual.emergencyShieldCooldown()
        );
        assertEquals(expected.active(), actual.active());
        assertEquals(
                expected.abilityCooldown(),
                actual.abilityCooldown()
        );
        assertEquals(
                expected.flightActive(),
                actual.flightActive()
        );
    }
}
