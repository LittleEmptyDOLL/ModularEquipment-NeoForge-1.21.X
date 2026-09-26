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
    private static final ModuleSize ASSIST_SIZE = new ModuleSize(2, 2);
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
        registerMobilityAssist(registry);
        registerJetpacks(registry);
        registerBlinkModules(registry);
        registerFlightModule(registry);
    }

    private static void registerMobilityAssist(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerAssist(
                registry,
                "civilian_mobility_assist",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                8,
                0.10D,
                0.15D,
                0.10D,
                0.25D
        );
        registerAssist(
                registry,
                "engineering_mobility_assist",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                12,
                0.15D,
                0.30D,
                0.15D,
                0.50D
        );
        registerAssist(
                registry,
                "military_mobility_assist",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                18,
                0.20D,
                0.35D,
                0.20D,
                0.75D
        );
        registerAssist(
                registry,
                "experimental_mobility_assist",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                25,
                0.30D,
                0.60D,
                0.30D,
                1.00D
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
        // FlightOperations currently represents unrestricted creative-style
        // flight. Keep it as an experimental endgame module rather than
        // duplicating the same ability across every equipment tier.
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

    private static void registerAssist(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int energyConsumption,
            double movementSpeed,
            double waterMovementEfficiency,
            double jumpStrength,
            double stepHeight
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.MOBILITY,
                                        ASSIST_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        MOBILITY_PRIORITY
                                ))
                                .attributes(new AttributeProperties(
                                        Map.of(
                                                MOVEMENT_SPEED,
                                                multipliedBase(movementSpeed),
                                                WATER_MOVEMENT_EFFICIENCY,
                                                addValue(waterMovementEfficiency),
                                                JUMP_STRENGTH,
                                                multipliedBase(jumpStrength),
                                                STEP_HEIGHT,
                                                addValue(stepHeight)
                                        )
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
