package com.github.littleemptydoll.exoequipment.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShieldProtectionOperationsTest {

    @Test
    void transfersConfiguredShareOfDamageReduction() {
        double playerMultiplier = 0.60D;

        assertEquals(
                0.90D,
                ShieldProtectionOperations.calculateShieldDamageMultiplier(
                        playerMultiplier,
                        0.25D
                ),
                1.0E-9D
        );
        assertEquals(
                0.80D,
                ShieldProtectionOperations.calculateShieldDamageMultiplier(
                        playerMultiplier,
                        0.50D
                ),
                1.0E-9D
        );
        assertEquals(
                0.70D,
                ShieldProtectionOperations.calculateShieldDamageMultiplier(
                        playerMultiplier,
                        0.75D
                ),
                1.0E-9D
        );
        assertEquals(
                0.60D,
                ShieldProtectionOperations.calculateShieldDamageMultiplier(
                        playerMultiplier,
                        1.0D
                ),
                1.0E-9D
        );
    }

    @Test
    void zeroTransferLeavesShieldDamageUnchanged() {
        assertEquals(
                1.0D,
                ShieldProtectionOperations.calculateShieldDamageMultiplier(
                        0.25D,
                        0.0D
                ),
                1.0E-9D
        );
    }
}
