package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonProfile;
import com.github.littleemptydoll.exoequipment.exoskeleton.MatrixSlot;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModControllers;
import com.github.littleemptydoll.exoequipment.registry.ModFrames;
import com.github.littleemptydoll.exoequipment.registry.TestModules;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShieldRevivalOperationsTest {
    private static final ResourceLocation MATRIX_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "exoequipment",
                    "defense_test"
            );

    @Test
    void shieldAbsorbsDamageAndStartsRechargeDelay() {
        InstalledModule shield = shield(50.0D, 0);
        ExoskeletonData data = data(shield);
        InstalledModuleReference reference =
                new InstalledModuleReference(0, 0);

        ShieldOperations.ShieldDamageResult result =
                ShieldOperations.absorbDamage(
                        20.0D,
                        data,
                        Set.of(reference)
                );

        assertEquals(0.0D, result.remainingDamage());
        assertEquals(20.0D, result.absorbedDamage());
        assertFalse(result.shieldDestroyed());
        assertEquals(
                30.0D,
                module(result.data(), 0).shieldEnergy()
        );
        assertEquals(
                100,
                module(result.data(), 0)
                        .shieldRechargeCooldown()
        );
    }

    @Test
    void depletedShieldReportsDestruction() {
        ExoskeletonData data = data(shield(10.0D, 0));
        InstalledModuleReference reference =
                new InstalledModuleReference(0, 0);

        ShieldOperations.ShieldDamageResult result =
                ShieldOperations.absorbDamage(
                        15.0D,
                        data,
                        Set.of(reference)
                );

        assertEquals(5.0D, result.remainingDamage());
        assertEquals(10.0D, result.absorbedDamage());
        assertTrue(result.shieldDestroyed());
        assertEquals(
                0.0D,
                module(result.data(), 0).shieldEnergy()
        );
    }

    @Test
    void shieldRechargeWaitsForCooldown() {
        InstalledModuleReference reference =
                new InstalledModuleReference(0, 0);
        ExoskeletonData data = data(shield(30.0D, 1));

        ExoskeletonData cooldownTick = ShieldOperations.tick(
                data,
                Set.of(reference)
        );

        assertEquals(
                30.0D,
                module(cooldownTick, 0).shieldEnergy()
        );
        assertEquals(
                0,
                module(cooldownTick, 0)
                        .shieldRechargeCooldown()
        );

        ExoskeletonData rechargeTick = ShieldOperations.tick(
                cooldownTick,
                Set.of(reference)
        );

        assertEquals(
                31.0D,
                module(rechargeTick, 0).shieldEnergy()
        );
    }

    @Test
    void emergencyShieldUsesStrongestReadyModule() {
        ExoskeletonData data = data(
                shield(0.0D, 0),
                new InstalledModule(
                        TestModules.TEST_EMERGENCY_SHIELD_HIGH
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                ),
                new InstalledModule(
                        TestModules.TEST_EMERGENCY_SHIELD_LOW
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                )
        );

        EmergencyShieldOperations.EmergencyShieldResult result =
                EmergencyShieldOperations.activate(
                        data,
                        Set.of(
                                new InstalledModuleReference(0, 0),
                                new InstalledModuleReference(0, 1),
                                new InstalledModuleReference(0, 2)
                        )
                );

        assertTrue(result.activated());
        assertEquals(
                50.0D,
                module(result.data(), 0).shieldEnergy()
        );
        assertEquals(
                240,
                module(result.data(), 1)
                        .emergencyShieldCooldown()
        );
        assertEquals(
                0,
                module(result.data(), 2)
                        .emergencyShieldCooldown()
        );
        assertEquals(
                1,
                EmergencyShieldOperations.readyCount(
                        result.data()
                )
        );
    }

    @Test
    void revivalSetsAndTicksCooldownOnlyWhilePowered() {
        InstalledModuleReference reference =
                new InstalledModuleReference(0, 0);
        ExoskeletonData data = data(
                new InstalledModule(
                        TestModules.TEST_REVIVAL
                                .getDefinition()
                                .id(),
                        0,
                        0,
                        0
                )
        );

        assertEquals(1, RevivalOperations.readyCount(data));

        RevivalOperations.RevivalResult revival =
                RevivalOperations.tryRevive(
                        data,
                        Set.of(reference)
                );

        assertTrue(revival.revived());
        assertEquals(
                4.0D,
                revival.properties().restoreHealth()
        );
        assertEquals(
                600,
                module(revival.data(), 0)
                        .revivalCooldown()
        );
        assertEquals(
                0,
                RevivalOperations.readyCount(
                        revival.data()
                )
        );

        ExoskeletonData unpoweredTick = RevivalOperations.tick(
                revival.data(),
                Set.of()
        );
        assertEquals(
                600,
                module(unpoweredTick, 0)
                        .revivalCooldown()
        );

        ExoskeletonData poweredTick = RevivalOperations.tick(
                unpoweredTick,
                Set.of(reference)
        );
        assertEquals(
                599,
                module(poweredTick, 0)
                        .revivalCooldown()
        );
    }

    private static InstalledModule shield(
            double energy,
            int rechargeCooldown
    ) {
        return new InstalledModule(
                TestModules.TEST_SHIELD
                        .getDefinition()
                        .id(),
                0,
                0,
                0
        ).withShieldEnergy(energy)
                .withShieldRechargeCooldown(
                        rechargeCooldown
                );
    }

    private static ExoskeletonData data(
            InstalledModule... modules
    ) {
        return new ExoskeletonData(
                Optional.of(new Frame(
                        ModFrames.CIVILIAN
                                .getDefinition()
                                .id()
                )),
                Optional.of(new Controller(
                        ModControllers.CIVILIAN
                                .getDefinition()
                                .id()
                )),
                Optional.empty(),
                List.of(
                        new MatrixSlot(Optional.of(
                                new MatrixData(
                                        MATRIX_ID,
                                        Arrays.asList(modules)
                                )
                        )),
                        MatrixSlot.empty(),
                        MatrixSlot.empty(),
                        MatrixSlot.empty()
                ),
                List.of(new ExoskeletonProfile(
                        "Defense",
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
}
