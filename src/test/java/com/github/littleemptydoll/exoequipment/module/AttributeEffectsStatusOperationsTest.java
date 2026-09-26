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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttributeEffectsStatusOperationsTest {
    private static final double EPSILON = 0.000001D;

    private static final ResourceLocation MATRIX_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "exoequipment",
                    "attribute_effect_status_test"
            );

    private static final ResourceLocation MOVEMENT_SPEED =
            ResourceLocation.parse("minecraft:generic.movement_speed");

    private static final ResourceLocation NIGHT_VISION =
            ResourceLocation.parse("minecraft:night_vision");

    private static final ResourceLocation POISON =
            ResourceLocation.parse("minecraft:poison");

    private static final ResourceLocation WITHER =
            ResourceLocation.parse("minecraft:wither");

    @Test
    void attributesOnlyUsePoweredModulesAndStackAdditively() {
        ExoskeletonData data = data(
                mobility(),
                mobility()
        );
        AttributeOperations.AttributeKey movementSpeed =
                new AttributeOperations.AttributeKey(
                        MOVEMENT_SPEED,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );

        assertEquals(
                0.30D,
                AttributeOperations.calculate(
                        null,
                        data,
                        Set.of(
                                new InstalledModuleReference(0, 0),
                                new InstalledModuleReference(0, 1)
                        )
                ).get(movementSpeed),
                EPSILON
        );
        assertEquals(
                0.15D,
                AttributeOperations.calculate(
                        null,
                        data,
                        Set.of(new InstalledModuleReference(0, 1))
                ).get(movementSpeed),
                EPSILON
        );
        assertFalse(
                AttributeOperations.calculate(
                        null,
                        data,
                        Set.of()
                ).containsKey(movementSpeed)
        );
    }

    @Test
    void effectsOnlyCollectPoweredModules() {
        ExoskeletonData data = data(nightVision());
        InstalledModuleReference reference =
                new InstalledModuleReference(0, 0);

        assertEquals(
                Map.of(NIGHT_VISION, 0),
                EffectsOperations.collectEffects(
                        data,
                        Set.of(reference)
                )
        );
        assertEquals(
                Map.of(),
                EffectsOperations.collectEffects(
                        data,
                        Set.of()
                )
        );
    }

    @Test
    void statusProtectionStacksMultiplicativelyAndUsesPowerState() {
        ExoskeletonData data = data(
                statusProtection(),
                statusProtection()
        );

        assertEquals(
                0.9375D,
                StatusProtectionOperations.calculateProtection(
                        data,
                        POISON,
                        Set.of(
                                new InstalledModuleReference(0, 0),
                                new InstalledModuleReference(0, 1)
                        )
                ),
                EPSILON
        );
        assertEquals(
                0.75D,
                StatusProtectionOperations.calculateProtection(
                        data,
                        POISON,
                        Set.of(new InstalledModuleReference(0, 0))
                ),
                EPSILON
        );
        assertEquals(
                0.0D,
                StatusProtectionOperations.calculateProtection(
                        data,
                        POISON,
                        Set.of()
                ),
                EPSILON
        );
        assertEquals(
                1.0D,
                StatusProtectionOperations.calculateProtection(
                        data,
                        WITHER,
                        Set.of(new InstalledModuleReference(0, 0))
                ),
                EPSILON
        );
    }

    @Test
    void statusProtectionShortensDurationAndCanBlockEffect() {
        ExoskeletonData data = data(statusProtection());
        Set<InstalledModuleReference> powered =
                Set.of(new InstalledModuleReference(0, 0));

        assertEquals(
                25,
                StatusProtectionOperations.applyProtection(
                        100,
                        data,
                        POISON,
                        powered
                )
        );
        assertEquals(
                0,
                StatusProtectionOperations.applyProtection(
                        100,
                        data,
                        WITHER,
                        powered
                )
        );
    }

    @Test
    void propertyObjectsRejectInvalidMapsAndNullStatusLookupIsSafe() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AttributeProperties(null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new EffectsProperties(null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new StatusProtectionProperties(null)
        );

        Map<ResourceLocation, Integer> invalidEffects = new HashMap<>();
        invalidEffects.put(NIGHT_VISION, null);
        assertThrows(
                IllegalArgumentException.class,
                () -> new EffectsProperties(invalidEffects)
        );

        StatusProtectionProperties protection =
                new StatusProtectionProperties(Map.of(POISON, 0.75D));
        assertEquals(0.0D, protection.protection(null), EPSILON);
    }

    private static InstalledModule mobility() {
        return module(TestModules.TEST_MOBILITY.getDefinition().id());
    }

    private static InstalledModule nightVision() {
        return module(TestModules.TEST_NIGHT_VISION.getDefinition().id());
    }

    private static InstalledModule statusProtection() {
        return module(TestModules.TEST_STATUS_PROTECTION.getDefinition().id());
    }

    private static InstalledModule module(ResourceLocation id) {
        return new InstalledModule(id, 0, 0, 0);
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
                        "Attributes effects status",
                        List.of(0)
                )),
                0,
                20.0D
        );
    }
}
