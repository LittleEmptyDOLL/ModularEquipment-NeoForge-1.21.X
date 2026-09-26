package com.github.littleemptydoll.exoequipment.module;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatusProtectionPropertiesTest {
    private static final double EPSILON = 1.0E-9D;

    private static final ResourceLocation POISON =
            ResourceLocation.parse("minecraft:poison");
    private static final ResourceLocation WEAKNESS =
            ResourceLocation.parse("minecraft:weakness");
    private static final ResourceLocation SPEED =
            ResourceLocation.parse("minecraft:speed");

    @Test
    void generalProtectionAppliesOnlyWhenEffectIsHarmful() {
        StatusProtectionProperties properties =
                new StatusProtectionProperties(
                        0.40D,
                        Map.of()
                );

        assertEquals(
                0.40D,
                properties.protection(WEAKNESS, true),
                EPSILON
        );
        assertEquals(
                0.0D,
                properties.protection(SPEED, false),
                EPSILON
        );
    }

    @Test
    void explicitProtectionOverridesGeneralProtection() {
        StatusProtectionProperties properties =
                new StatusProtectionProperties(
                        0.40D,
                        Map.of(POISON, 0.75D)
                );

        assertEquals(
                0.75D,
                properties.protection(POISON, true),
                EPSILON
        );
    }

    @Test
    void harmfulProtectionMustBeNormalized() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StatusProtectionProperties(
                        -0.01D,
                        Map.of()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new StatusProtectionProperties(
                        1.01D,
                        Map.of()
                )
        );
    }
}
