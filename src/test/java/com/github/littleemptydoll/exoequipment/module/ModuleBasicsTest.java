package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.matrix.MatrixOperations;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleBasicsTest {

    @Test
    void normalizesRotation() {
        assertEquals(
                270,
                MatrixOperations.normalizeRotation(-90)
        );
        assertEquals(
                90,
                MatrixOperations.normalizeRotation(450)
        );
    }

    @Test
    void rejectsInvalidRotation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MatrixOperations
                        .normalizeRotation(45)
        );
    }

    @Test
    void rotatesModuleSize() {
        ModuleSize size =
                new ModuleSize(2, 3);

        assertEquals(
                new ModuleSize(2, 3),
                MatrixOperations.getRotatedSize(
                        size,
                        180
                )
        );
        assertEquals(
                new ModuleSize(3, 2),
                MatrixOperations.getRotatedSize(
                        size,
                        90
                )
        );
    }

    @Test
    void rejectsNegativeModuleReference() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new InstalledModuleReference(
                        -1,
                        0
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new InstalledModuleReference(
                        0,
                        -1
                )
        );
    }
}
