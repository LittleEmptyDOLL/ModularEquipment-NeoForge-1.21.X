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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefenseOperationsTest {
    private static final double EPSILON = 0.000001D;

    private static final ResourceLocation MATRIX_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "exoequipment",
                    "damage_test"
            );

    private static final ResourceLocation ARROW =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "arrow"
            );

    private static final ResourceLocation EXPLOSION =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "explosion"
            );

    private static final ResourceLocation PROJECTILE_TAG =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "is_projectile"
            );

    private static final ResourceLocation BYPASSES_ARMOR_TAG =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "bypasses_armor"
            );

    @Test
    void nullDamageTypeUsesDefaultReduction() {
        DamageReductionProperties properties =
                TestModules.TEST_DAMAGE_REDUCTION
                        .getDefinition()
                        .damageReduction()
                        .orElseThrow();

        assertEquals(
                0.10D,
                properties.reduction(null),
                EPSILON
        );
        assertEquals(
                0.25D,
                properties.reduction(ARROW),
                EPSILON
        );
        assertEquals(
                1.0D,
                properties.reduction(EXPLOSION),
                EPSILON
        );
    }

    @Test
    void damageTagsUseStrongestMatchAndExactTypeOverridesThem() {
        DamageReductionProperties properties =
                new DamageReductionProperties(
                        0.10D,
                        Map.of(ARROW, 0.25D),
                        Map.of(
                                PROJECTILE_TAG, 0.40D,
                                BYPASSES_ARMOR_TAG, 0.60D
                        )
                );

        assertEquals(
                0.40D,
                properties.reduction(
                        null,
                        PROJECTILE_TAG::equals
                ),
                EPSILON
        );
        assertEquals(
                0.60D,
                properties.reduction(
                        null,
                        tag -> true
                ),
                EPSILON
        );
        assertEquals(
                0.25D,
                properties.reduction(
                        ARROW,
                        tag -> true
                ),
                EPSILON
        );
    }

    @Test
    void damageReductionStacksMultiplicatively() {
        ExoskeletonData data = data(
                damageReduction(),
                damageReduction()
        );
        Set<InstalledModuleReference> powered = Set.of(
                new InstalledModuleReference(0, 0),
                new InstalledModuleReference(0, 1)
        );

        assertEquals(
                0.81D,
                DefenseOperations.calculateDamageMultiplier(
                        data,
                        null,
                        powered
                ),
                EPSILON
        );
        assertEquals(
                0.5625D,
                DefenseOperations.calculateDamageMultiplier(
                        data,
                        ARROW,
                        powered
                ),
                EPSILON
        );
        assertEquals(
                0.0D,
                DefenseOperations.calculateDamageMultiplier(
                        data,
                        EXPLOSION,
                        powered
                ),
                EPSILON
        );
        assertEquals(
                56.25D,
                DefenseOperations.applyDamageReduction(
                        100.0D,
                        data,
                        ARROW,
                        powered
                ),
                EPSILON
        );
    }

    @Test
    void damageReductionOnlyUsesPoweredModules() {
        ExoskeletonData data = data(
                damageReduction(),
                damageReduction()
        );

        assertEquals(
                0.90D,
                DefenseOperations.calculateDamageMultiplier(
                        data,
                        null,
                        Set.of(new InstalledModuleReference(0, 0))
                ),
                EPSILON
        );
        assertEquals(
                1.0D,
                DefenseOperations.calculateDamageMultiplier(
                        data,
                        null,
                        Set.of()
                ),
                EPSILON
        );
    }

    @Test
    void bodyProtectionStacksAndRespectsBodyPart() {
        ExoskeletonData data = data(
                bodyProtection(),
                bodyProtection()
        );
        Set<InstalledModuleReference> powered = Set.of(
                new InstalledModuleReference(0, 0),
                new InstalledModuleReference(0, 1)
        );

        assertEquals(
                0.51D,
                BodyDamageProtectionOperations.calculateChance(
                        data,
                        BodyPart.HEAD,
                        powered
                ),
                EPSILON
        );
        assertEquals(
                0.25D,
                BodyDamageProtectionOperations.calculateDamageMultiplier(
                        data,
                        BodyPart.HEAD,
                        powered
                ),
                EPSILON
        );
        assertEquals(
                0.0D,
                BodyDamageProtectionOperations.calculateChance(
                        data,
                        BodyPart.RIGHT_LEG,
                        powered
                ),
                EPSILON
        );
        assertEquals(
                1.0D,
                BodyDamageProtectionOperations.calculateDamageMultiplier(
                        data,
                        BodyPart.RIGHT_LEG,
                        powered
                ),
                EPSILON
        );
    }

    @Test
    void bodyProtectionOnlyUsesPoweredModules() {
        ExoskeletonData data = data(
                bodyProtection(),
                bodyProtection()
        );
        Set<InstalledModuleReference> powered =
                Set.of(new InstalledModuleReference(0, 1));

        assertEquals(
                0.30D,
                BodyDamageProtectionOperations.calculateChance(
                        data,
                        0,
                        BodyPart.CHEST,
                        powered
                ),
                EPSILON
        );
        assertEquals(
                0.50D,
                BodyDamageProtectionOperations.calculateDamageMultiplier(
                        data,
                        0,
                        BodyPart.CHEST,
                        powered
                ),
                EPSILON
        );
    }

    @Test
    void nonPositiveDamageNeverProducesNegativeResult() {
        ExoskeletonData data = data(damageReduction());

        assertEquals(
                0.0D,
                DefenseOperations.applyDamageReduction(
                        0.0D,
                        data,
                        ARROW
                ),
                EPSILON
        );
        assertEquals(
                0.0D,
                DefenseOperations.applyDamageReduction(
                        -10.0D,
                        data,
                        ARROW
                ),
                EPSILON
        );
    }

    private static InstalledModule damageReduction() {
        return new InstalledModule(
                TestModules.TEST_DAMAGE_REDUCTION
                        .getDefinition()
                        .id(),
                0,
                0,
                0
        );
    }

    private static InstalledModule bodyProtection() {
        return new InstalledModule(
                TestModules.TEST_BODY_DAMAGE_PROTECTION
                        .getDefinition()
                        .id(),
                0,
                0,
                0
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
}
