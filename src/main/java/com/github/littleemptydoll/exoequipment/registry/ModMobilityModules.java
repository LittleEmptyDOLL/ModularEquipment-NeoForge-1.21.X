package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;

import java.util.Map;
import java.util.Optional;

final class ModMobilityModules {
    private static final ModuleSize SPEED_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize JUMP_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize SWIM_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize STEP_SIZE = new ModuleSize(1, 1);
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

    private ModMobilityModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerMovementSpeedModules(registry);
        registerJumpModules(registry);
        registerSwimModules(registry);
        registerStepHeightModules(registry);
        registerJetpacks(registry);
        registerBlinkModules(registry);
        registerFlightModule(registry);
    }

    private static void registerMovementSpeedModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAttributeModule(
                registry,
                "civilian_movement_speed",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                SPEED_SIZE,
                5,
                MOVEMENT_SPEED,
                multipliedBase(0.10D)
        );
        registerAttributeModule(
                registry,
                "engineering_movement_speed",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                SPEED_SIZE,
                8,
                MOVEMENT_SPEED,
                multipliedBase(0.15D)
        );
        registerAttributeModule(
                registry,
                "military_movement_speed",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                SPEED_SIZE,
                12,
                MOVEMENT_SPEED,
                multipliedBase(0.20D)
        );
        registerAttributeModule(
                registry,
                "experimental_movement_speed",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                SPEED_SIZE,
                18,
                MOVEMENT_SPEED,
                multipliedBase(0.30D)
        );
    }

    private static void registerJumpModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAttributeModule(
                registry,
                "civilian_jump_assist",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                JUMP_SIZE,
                4,
                JUMP_STRENGTH,
                multipliedBase(0.10D)
        );
        registerAttributeModule(
                registry,
                "engineering_jump_assist",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                JUMP_SIZE,
                7,
                JUMP_STRENGTH,
                multipliedBase(0.15D)
        );
        registerAttributeModule(
                registry,
                "military_jump_assist",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                JUMP_SIZE,
                10,
                JUMP_STRENGTH,
                multipliedBase(0.20D)
        );
        registerAttributeModule(
                registry,
                "experimental_jump_assist",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                JUMP_SIZE,
                15,
                JUMP_STRENGTH,
                multipliedBase(0.30D)
        );
    }

    private static void registerSwimModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAttributeModule(
                registry,
                "civilian_swim_assist",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                SWIM_SIZE,
                4,
                WATER_MOVEMENT_EFFICIENCY,
                addValue(0.15D)
        );
        registerAttributeModule(
                registry,
                "engineering_swim_assist",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                SWIM_SIZE,
                7,
                WATER_MOVEMENT_EFFICIENCY,
                addValue(0.30D)
        );
        registerAttributeModule(
                registry,
                "military_swim_assist",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                SWIM_SIZE,
                10,
                WATER_MOVEMENT_EFFICIENCY,
                addValue(0.45D)
        );
        registerAttributeModule(
                registry,
                "experimental_swim_assist",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                SWIM_SIZE,
                15,
                WATER_MOVEMENT_EFFICIENCY,
                addValue(0.60D)
        );
    }

    private static void registerStepHeightModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAttributeModule(
                registry,
                "civilian_step_assist",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                STEP_SIZE,
                3,
                STEP_HEIGHT,
                addValue(0.25D)
        );
        registerAttributeModule(
                registry,
                "engineering_step_assist",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                STEP_SIZE,
                5,
                STEP_HEIGHT,
                addValue(0.50D)
        );
        registerAttributeModule(
                registry,
                "military_step_assist",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                STEP_SIZE,
                8,
                STEP_HEIGHT,
                addValue(0.75D)
        );
        registerAttributeModule(
                registry,
                "experimental_step_assist",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                STEP_SIZE,
                12,
                STEP_HEIGHT,
                addValue(1.00D)
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
                4,
                0.25D,
                0.20D,
                12,
                Optional.empty()
        );
        registerJetpack(
                registry,
                "engineering_jetpack",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                6,
                0.35D,
                0.30D,
                18,
                Optional.of(new ElytraBoostProperties(
                        0.025D,
                        1.25D
                ))
        );
        registerJetpack(
                registry,
                "military_jetpack",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                8,
                0.45D,
                0.45D,
                25,
                Optional.of(new ElytraBoostProperties(
                        0.04D,
                        1.75D
                ))
        );
        registerJetpack(
                registry,
                "experimental_jetpack",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                12,
                0.60D,
                0.65D,
                40,
                Optional.of(new ElytraBoostProperties(
                        0.06D,
                        2.50D
                ))
        );
    }

    private static void registerBlinkModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerBlink(
                registry,
                "civilian_blink",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                4.0D,
                300,
                100
        );
        registerBlink(
                registry,
                "engineering_blink",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                6.0D,
                500,
                80
        );
        registerBlink(
                registry,
                "military_blink",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                8.0D,
                700,
                60
        );
        registerBlink(
                registry,
                "experimental_blink",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                12.0D,
                1_000,
                40
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
                                        30,
                                        MOBILITY_PRIORITY
                                ))
                                .flight(new FlightProperties(90))
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
            ResourceLocation attribute,
            AttributeModifierProperties modifier
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
                                .attributes(new AttributeProperties(
                                        Map.of(attribute, modifier)
                                ))
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
            Optional<ElytraBoostProperties> elytra
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
