package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.MatrixSlot;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.registry.ModFrames;
import com.github.littleemptydoll.exoequipment.registry.TestModules;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacteristicsProviderTest {
    private static final double EPSILON = 0.000001D;

    @Test
    void matrixScopeShowsCharacteristicsFromThatMatrix() {
        ExoskeletonData data = data(
                matrix(
                        "matrix_a",
                        module(TestModules.TEST_NIGHT_VISION.getDefinition().id()),
                        module(TestModules.TEST_STATUS_PROTECTION.getDefinition().id()),
                        module(TestModules.TEST_HUNGER.getDefinition().id()),
                        module(TestModules.TEST_PICKUP_MAGNET.getDefinition().id())
                ),
                matrix(
                        "matrix_b",
                        module(TestModules.TEST_REGENERATION.getDefinition().id())
                )
        );

        List<Characteristic> characteristics =
                CharacteristicsProvider.collect(
                        CharacteristicsContext.matrix(
                                data,
                                null,
                                Set.of(),
                                0
                        )
                );

        Map<String, Characteristic> byKey =
                characteristics.stream()
                        .collect(Collectors.toMap(
                                Characteristic::key,
                                Function.identity()
                        ));

        assertEquals(
                1.0D,
                byKey.get("effect.minecraft:night_vision").value(),
                EPSILON
        );
        assertEquals(
                0.75D,
                byKey.get(
                        "status_protection.minecraft:poison.reduction"
                ).value(),
                EPSILON
        );
        assertEquals(
                1.0D,
                byKey.get(
                        "status_protection.minecraft:wither.reduction"
                ).value(),
                EPSILON
        );
        assertEquals(
                0.50D,
                byKey.get("hunger.exhaustion_reduction").value(),
                EPSILON
        );
        assertEquals(
                8.0D,
                byKey.get("pickup_magnet.radius").value(),
                EPSILON
        );
        assertTrue(byKey.containsKey("pickup_magnet.items"));
        assertTrue(byKey.containsKey("pickup_magnet.experience"));
        assertFalse(byKey.containsKey("health_per_second"));
    }

    @Test
    void matrixScopeDoesNotDependOnActiveProfileOrRuntimePowerSet() {
        ExoskeletonData data = data(
                matrix(
                        "matrix_a",
                        module(TestModules.TEST_HUNGER.getDefinition().id())
                ),
                null
        );

        List<Characteristic> characteristics =
                CharacteristicsProvider.collect(
                        CharacteristicsContext.matrix(
                                data,
                                null,
                                Set.of(),
                                0
                        )
                );

        assertTrue(
                characteristics.stream().anyMatch(characteristic ->
                        characteristic.key().equals(
                                "hunger.exhaustion_reduction"
                        )
                )
        );
    }

    private static ExoskeletonData data(
            MatrixData first,
            MatrixData second
    ) {
        return new ExoskeletonData(
                Optional.of(new Frame(
                        ModFrames.EXPERIMENTAL
                                .getDefinition()
                                .id()
                )),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        slot(first),
                        slot(second),
                        MatrixSlot.empty(),
                        MatrixSlot.empty()
                ),
                List.of(),
                -1,
                20.0D
        );
    }

    private static MatrixSlot slot(MatrixData matrix) {
        return matrix == null
                ? MatrixSlot.empty()
                : new MatrixSlot(Optional.of(matrix));
    }

    private static MatrixData matrix(
            String path,
            InstalledModule... modules
    ) {
        return new MatrixData(
                ResourceLocation.fromNamespaceAndPath(
                        "exoequipment",
                        path
                ),
                List.of(modules)
        );
    }

    private static InstalledModule module(ResourceLocation id) {
        return new InstalledModule(id, 0, 0, 0);
    }
}
