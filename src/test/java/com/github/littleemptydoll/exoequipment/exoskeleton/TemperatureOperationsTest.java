package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.controller.Controller;
import com.github.littleemptydoll.exoequipment.energy.EnergyTickResult;
import com.github.littleemptydoll.exoequipment.frame.Frame;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.TemperatureBonus;
import com.github.littleemptydoll.exoequipment.module.TemperatureImpactProperties;
import com.github.littleemptydoll.exoequipment.module.TemperatureModifierProperties;
import com.github.littleemptydoll.exoequipment.module.TemperatureProperties;
import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.github.littleemptydoll.exoequipment.registry.ModControllers;
import com.github.littleemptydoll.exoequipment.registry.ModFrames;
import com.github.littleemptydoll.exoequipment.registry.TestModules;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemperatureOperationsTest {
    private static final double EPSILON = 0.000001D;

    private static final ResourceLocation MATRIX_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "exoequipment",
                    "temperature_test"
            );

    @Test
    void efficiencyUsesOperatingRangeFalloffAndBonus() {
        ModuleDefinition consumer =
                TestModules.TEST_CONSUMER.getDefinition();

        assertEquals(
                1.0D,
                TemperatureOperations.calculateModuleEfficiency(
                        consumer,
                        20.0D
                ),
                EPSILON
        );
        assertEquals(
                1.5D,
                TemperatureOperations.calculateModuleEfficiency(
                        consumer,
                        30.0D
                ),
                EPSILON
        );
        assertEquals(
                0.775D,
                TemperatureOperations.calculateModuleEfficiency(
                        consumer,
                        -10.0D
                ),
                EPSILON
        );
        assertEquals(
                TemperatureProperties.MIN_EFFICIENCY,
                TemperatureOperations.calculateModuleEfficiency(
                        consumer,
                        -100.0D
                ),
                EPSILON
        );
    }

    @Test
    void generatorTemperatureBonusCanIncreaseEfficiencyAboveOne() {
        assertEquals(
                2.0D,
                TemperatureOperations.calculateModuleEfficiency(
                        TestModules.TEST_GENERATOR.getDefinition(),
                        70.0D
                ),
                EPSILON
        );
    }

    @Test
    void zeroWidthBonusOnlyAppliesAtConfiguredTemperature() {
        ModuleDefinition definition = ModuleDefinition.builder(
                        ResourceLocation.fromNamespaceAndPath(
                                "exoequipment",
                                "point_temperature_bonus_test"
                        ),
                        new EquipmentProperties(
                                EquipmentTier.BASIC,
                                Rarity.COMMON
                        ),
                        ModuleCategory.UTILITY,
                        new ModuleSize(1, 1)
                )
                .temperature(new TemperatureProperties(
                        0.0D,
                        100.0D,
                        Optional.empty(),
                        Optional.of(new TemperatureBonus(
                                50.0D,
                                50.0D,
                                0.5D
                        ))
                ))
                .build();

        assertEquals(
                1.5D,
                TemperatureOperations.calculateModuleEfficiency(
                        definition,
                        50.0D
                ),
                EPSILON
        );
        assertEquals(
                1.0D,
                TemperatureOperations.calculateModuleEfficiency(
                        definition,
                        49.0D
                ),
                EPSILON
        );
    }

    @Test
    void invalidTemperatureConfigurationIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TemperatureProperties(
                        Double.NaN,
                        40.0D,
                        Optional.empty(),
                        Optional.empty()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new TemperatureProperties(
                        0.0D,
                        40.0D,
                        Optional.of(Double.POSITIVE_INFINITY),
                        Optional.empty()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new TemperatureBonus(
                        20.0D,
                        40.0D,
                        Double.NaN
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new TemperatureImpactProperties(Double.NaN)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new TemperatureModifierProperties(
                        0.0D,
                        Double.NaN,
                        0.0D,
                        0.0D
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> TemperatureOperations.calculateModuleEfficiency(
                        TestModules.TEST.getDefinition(),
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void nanTemperatureDisablesEfficiencyScaling() {
        assertEquals(
                1.0D,
                TemperatureOperations.calculateModuleEfficiency(
                        TestModules.TEST_CONSUMER.getDefinition(),
                        Double.NaN
                ),
                EPSILON
        );
    }

    @Test
    void thermalStateOnlyUsesPoweredHeatSources() {
        InstalledModule heater = new InstalledModule(
                TestModules.TEST_HEATER
                        .getDefinition()
                        .id(),
                0,
                0,
                0
        );
        ExoskeletonData data = data(20.0D, heater);
        InstalledModuleReference reference =
                new InstalledModuleReference(0, 0);

        ExoskeletonTemperatureState powered =
                ExoskeletonTemperatureState.calculate(
                        data,
                        energyResult(data, Set.of(reference))
                );
        ExoskeletonTemperatureState unpowered =
                ExoskeletonTemperatureState.calculate(
                        data,
                        energyResult(data, Set.of())
                );

        assertEquals(20.0D, powered.thermalBalance(), EPSILON);
        assertEquals(21.0D, powered.temperature(), EPSILON);
        assertEquals(0.0D, unpowered.thermalBalance(), EPSILON);
        assertEquals(20.0D, unpowered.temperature(), EPSILON);
    }

    @Test
    void passiveExchangeMovesTemperatureTowardBaseline() {
        ExoskeletonTemperatureState state =
                ExoskeletonTemperatureState.calculate(
                        data(30.0D),
                        energyResult(data(30.0D), Set.of())
                );

        assertEquals(-10.0D, state.thermalBalance(), EPSILON);
        assertEquals(29.5D, state.temperature(), EPSILON);
    }

    private static EnergyTickResult energyResult(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        return new EnergyTickResult(
                data,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                poweredModules
        );
    }

    private static ExoskeletonData data(
            double temperature,
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
                        "Temperature",
                        List.of(0)
                )),
                0,
                temperature
        );
    }
}
