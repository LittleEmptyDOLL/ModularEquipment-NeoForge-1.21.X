package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class ModMobilityModules {
    private static final ModuleSize SPEED_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize JUMP_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize SWIM_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize ELYTRA_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize JETPACK_SIZE = new ModuleSize(2, 3);
    private static final ModuleSize BLINK_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize FLIGHT_SIZE = new ModuleSize(3, 3);

    // Mobility should yield to defensive and survival-critical systems when
    // the energy bus cannot power every installed module.
    private static final int MOBILITY_PRIORITY = 5;

    private static final ResourceLocation MOVEMENT_SPEED =
            ResourceLocation.parse("minecraft:generic.movement_speed");
    private static final ResourceLocation WATER_MOVEMENT_EFFICIENCY =
            ResourceLocation.parse("minecraft:generic.water_movement_efficiency");
    private static final ResourceLocation JUMP_STRENGTH =
            ResourceLocation.parse("minecraft:generic.jump_strength");
    private static final ResourceLocation STEP_HEIGHT =
            ResourceLocation.parse("minecraft:generic.step_height");
    private static final ResourceLocation ELYTRA_FLIGHT =
            ResourceLocation.parse("apothic_attributes:elytra_flight");

    private ModMobilityModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerMovementSpeedModules(registry);
        registerJumpModules(registry);
        registerSwimModules(registry);
        registerJetpacks(registry);
        registerBlinkModules(registry);
        registerElytraModule(registry);
        registerFlightModule(registry);
    }

    private static void registerMovementSpeedModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAttributeModule(
                registry,
                "civilian_movement_actuator",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                SPEED_SIZE,
                10,
                Map.ofEntries(Map.entry(MOVEMENT_SPEED, multipliedBase(0.10D)))
        );
        registerAttributeModule(
                registry,
                "military_high_power_actuator",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                SPEED_SIZE,
                25,
                Map.ofEntries(Map.entry(MOVEMENT_SPEED, multipliedBase(0.25D)))
        );
        registerAttributeModule(
                registry,
                "experimental_actuator",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                SPEED_SIZE,
                40,
                Map.ofEntries(Map.entry(MOVEMENT_SPEED, multipliedBase(0.35D)))
        );
    }

    private static void registerJumpModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAttributeModule(
                registry,
                "civilian_leg_assist",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                JUMP_SIZE,
                15,
                Map.ofEntries(
                        Map.entry(JUMP_STRENGTH, multipliedBase(0.10D)),
                        Map.entry(STEP_HEIGHT, addValue(0.25D))
                )
        );
        registerAttributeModule(
                registry,
                "engineering_mobility_control",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                JUMP_SIZE,
                25,
                Map.ofEntries(
                        Map.entry(JUMP_STRENGTH, multipliedBase(0.20D)),
                        Map.entry(STEP_HEIGHT, addValue(0.5D))
                )
        );
        registerAttributeModule(
                registry,
                "experimental_mobility_control",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                JUMP_SIZE,
                40,
                Map.ofEntries(
                        Map.entry(JUMP_STRENGTH, multipliedBase(0.30D)),
                        Map.entry(STEP_HEIGHT, addValue(0.5D))
                )
        );
    }

    private static void registerSwimModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAttributeModule(
                registry,
                "engineering_aquatic_mobility",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                SWIM_SIZE,
                15,
                Map.ofEntries(Map.entry(WATER_MOVEMENT_EFFICIENCY, addValue(0.25D)))
        );
        registerAttributeModule(
                registry,
                "military_swim_assist",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                SWIM_SIZE,
                25,
                Map.ofEntries(Map.entry(WATER_MOVEMENT_EFFICIENCY, addValue(0.50D)))
        );
    }

    private static void registerJetpacks(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerJetpack(
                registry,
                "civilian_jetpack",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                15,
                0.25D,
                0.20D,
                100,
                Optional.empty(),
                new ThermalProperties(5, 0)
        );
        registerJetpack(
                registry,
                "engineering_jetpack",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                25,
                0.35D,
                0.30D,
                180,
                Optional.of(new ElytraBoostProperties(
                        0.025D,
                        1.25D
                )),
                new ThermalProperties(8, 0)
        );
        registerJetpack(
                registry,
                "military_jetpack",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                35,
                0.45D,
                0.45D,
                300,
                Optional.of(new ElytraBoostProperties(
                        0.04D,
                        1.75D
                )),
                new ThermalProperties(10, 0)
        );
        registerJetpack(
                registry,
                "experimental_jetpack",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                50,
                0.60D,
                0.65D,
                600,
                Optional.of(new ElytraBoostProperties(
                        0.06D,
                        2.50D
                )),
                new ThermalProperties(20, 0)
        );
    }

    private static void registerBlinkModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerBlink(
                registry,
                "engineering_blink",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                6.0D,
                1_500,
                80
        );
        registerBlink(
                registry,
                "military_blink",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                8.0D,
                2_000,
                60
        );
        registerBlink(
                registry,
                "experimental_blink",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                14.0D,
                3_000,
                40
        );
    }

    private static void registerElytraModule(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAttributeModule(
                registry,
                "engineering_elytra_system",
                EquipmentTier.ENGINEERING,
                Rarity.EPIC,
                ELYTRA_SIZE,
                30,
                Map.ofEntries(
                        Map.entry(ELYTRA_FLIGHT, addValue(1.0D))
                )
        );
    }

    private static void registerFlightModule(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        // FlightOperations represents unrestricted creative-style flight, so
        // it remains an experimental endgame ability rather than a normal
        // attribute upgrade available at every tier.
        registry.register(
                "experimental_flight",
                new EquipmentProperties(
                        EquipmentTier.EXPERIMENTAL,
                        Rarity.EPIC
                ),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.MOBILITY,
                                        FLIGHT_SIZE
                                )
                                .energy(new EnergyProperties(
                                        40,
                                        MOBILITY_PRIORITY
                                ))
                                .flight(new FlightProperties(500))
                                .build()
        );
    }

    private static void registerAttributeModule(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            ModuleSize size,
            int energyConsumption,
            Map<ResourceLocation, AttributeModifierProperties> entries
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.MOBILITY,
                                        size
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        MOBILITY_PRIORITY
                                ))
                                .attributes(new AttributeProperties(entries))
                                .build()
        );
    }

    private static void registerJetpack(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int passiveConsumption,
            double verticalThrust,
            double horizontalSpeed,
            int activeConsumption,
            Optional<ElytraBoostProperties> elytra,
            ThermalProperties thermalProperties
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.MOBILITY,
                                        JETPACK_SIZE
                                )
                                .energy(new EnergyProperties(
                                        passiveConsumption,
                                        MOBILITY_PRIORITY
                                ))
                                .jetpack(new JetpackProperties(
                                        verticalThrust,
                                        horizontalSpeed,
                                        activeConsumption,
                                        elytra
                                ))
                                .thermal(thermalProperties)
                                .build()
        );
    }

    private static void registerBlink(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            double distance,
            int activationEnergy,
            int cooldown
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.MOBILITY,
                                        BLINK_SIZE
                                )
                                .blink(new BlinkProperties(
                                        distance,
                                        activationEnergy,
                                        cooldown
                                ))
                                .build()
        );
    }

    private static AttributeModifierProperties multipliedBase(
            double amount
    ) {
        return new AttributeModifierProperties(
                amount,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    private static AttributeModifierProperties addValue(
            double amount
    ) {
        return new AttributeModifierProperties(
                amount,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
