package com.github.littleemptydoll.exoequipment.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleSizeTest {
    @Test
    void rejectsNonPositiveDimensions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleSize(0, 1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleSize(1, 0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ModuleSize(-1, 1)
        );
    }

    @Test
    void calculatesArea() {
        assertEquals(6, new ModuleSize(2, 3).area());
    }

    @Test
    void keepsDimensionsForHalfTurns() {
        ModuleSize size = new ModuleSize(2, 3);

        assertSame(size, size.rotated(0));
        assertSame(size, size.rotated(180));
        assertSame(size, size.rotated(-180));
    }

    @Test
    void swapsDimensionsForQuarterTurns() {
        ModuleSize size = new ModuleSize(2, 3);

        assertEquals(new ModuleSize(3, 2), size.rotated(90));
        assertEquals(new ModuleSize(3, 2), size.rotated(270));
        assertEquals(new ModuleSize(3, 2), size.rotated(-90));
    }
}
