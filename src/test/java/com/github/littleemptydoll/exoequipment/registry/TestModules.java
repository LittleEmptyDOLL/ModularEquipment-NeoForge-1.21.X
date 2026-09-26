package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TestModules {
    private static final EquipmentRegistry<ModuleDefinition, ModuleItem> REGISTRY =
            ModModules.registry();

    private TestModules() {}

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST = REGISTRY.register(
            "test",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                    id,
                    properties,
                    ModuleCategory.UTILITY,
                    new ModuleSize(1, 2)
            ).build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_NIGHT_VISION = REGISTRY.register(
            "test_night_vision",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.SENSOR,
                            new ModuleSize(1, 1)
                    )
                    .energy(new EnergyProperties(5))
                    .effects(new EffectsProperties(Map.of(
                            ResourceLocation.fromNamespaceAndPath("minecraft", "night_vision"),
                            0
                    )))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_JETPACK = REGISTRY.register(
            "test_jetpack",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.MOBILITY,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(5, 1))
                    .jetpack(new JetpackProperties(
                            0.5D,
                            0.5D,
                            10,
                            Optional.of(new ElytraBoostProperties(
                                    0.08D,
                                    2.5D
                            ))
                    ))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_CLOAKING = REGISTRY.register(
            "test_cloaking",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(20))
                    .cloaking(new CloakingProperties(
                            200,
                            30,
                            200
                    ))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_CONSUMER = REGISTRY.register(
            "test_consumer",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(20))
                    .thermal(new ThermalProperties(20, 0))
                    .temperature(new TemperatureProperties(
                            0,
                            40,
                            Optional.empty(),
                            Optional.of(new TemperatureBonus(20, 40, 0.5))
                    ))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_GENERATOR = REGISTRY.register(
            "test_generator",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.ENERGY,
                            new ModuleSize(3, 3)
                    )
                    .generation(new GenerationProperties(40))
                    .thermal(new ThermalProperties(10, 0))
                    .temperature(new TemperatureProperties(
                            40,
                            80,
                            Optional.empty(),
                            Optional.of(new TemperatureBonus(60, 80, 1))
                    ))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_BATTERY = REGISTRY.register(
            "test_battery",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.ENERGY,
                            new ModuleSize(2, 3)
                    )
                    .storage(new StorageProperties(10_000, 100, 200))
                    .temperature(new TemperatureProperties(
                            -20,
                            50,
                            Optional.empty(),
                            Optional.empty()
                    ))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_SHIELD = REGISTRY.register(
            "test_shield",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.DEFENSE,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(5, 1))
                    .shield(new ShieldProperties(50, 1, 100))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_EMERGENCY_SHIELD_HIGH = REGISTRY.register(
            "test_emergency_shield_high",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.DEFENSE,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(5, 1))
                    .emergencyShield(new EmergencyShieldProperties(1.0D, 240))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_EMERGENCY_SHIELD_LOW = REGISTRY.register(
            "test_emergency_shield_low",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.DEFENSE,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(5, 1))
                    .emergencyShield(new EmergencyShieldProperties(0.5D, 120))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_STATUS_PROTECTION = REGISTRY.register(
            "test_status_protection",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.DEFENSE,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(10, 1))
                    .statusProtection(new StatusProtectionProperties(Map.of(
                            ResourceLocation.fromNamespaceAndPath("minecraft", "poison"), 0.75D,
                            ResourceLocation.fromNamespaceAndPath("minecraft", "wither"), 1.0D
                    )))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_DAMAGE_REDUCTION = REGISTRY.register(
            "test_damage_reduction",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.DEFENSE,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(10, 1))
                    .damageReduction(new DamageReductionProperties(
                            0.10D,
                            Map.of(
                                    ResourceLocation.fromNamespaceAndPath("minecraft", "arrow"), 0.25D,
                                    ResourceLocation.fromNamespaceAndPath("minecraft", "explosion"), 1.0D
                            )
                    ))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_BODY_DAMAGE_PROTECTION = REGISTRY.register(
            "test_body_damage_protection",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.DEFENSE,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(10, 1))
                    .bodyDamageProtection(new BodyDamageProtectionProperties(
                            0.30D,
                            0.50D,
                            Set.of(BodyPart.HEAD, BodyPart.CHEST)
                    ))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_MOBILITY = REGISTRY.register(
            "test_mobility",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.MOBILITY,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(5))
                    .attributes(new AttributeProperties(Map.ofEntries(
                            Map.entry(
                                    ResourceLocation.parse("minecraft:generic.movement_speed"),
                                    new AttributeModifierProperties(
                                            0.15D,
                                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                                    )
                            ),
                            Map.entry(
                                    ResourceLocation.parse("minecraft:generic.water_movement_efficiency"),
                                    new AttributeModifierProperties(
                                            0.25D,
                                            AttributeModifier.Operation.ADD_VALUE
                                    )
                            ),
                            Map.entry(
                                    ResourceLocation.parse("minecraft:generic.jump_strength"),
                                    new AttributeModifierProperties(
                                            0.20D,
                                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                                    )
                            ),
                            Map.entry(
                                    ResourceLocation.parse("minecraft:generic.step_height"),
                                    new AttributeModifierProperties(
                                            0.50D,
                                            AttributeModifier.Operation.ADD_VALUE
                                    )
                            )
                    )))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_PICKUP_MAGNET = REGISTRY.register(
            "test_pickup_magnet",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(5))
                    .pickupMagnet(new PickupMagnetProperties(
                            8.0D,
                            PickupMagnetProperties.Mode.BOTH
                    ))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_HEATER = REGISTRY.register(
            "test_heater",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.THERMAL,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(10))
                    .thermal(new ThermalProperties(20, 0))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_HEALTH = REGISTRY.register(
            "test_health",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.SURVIVAL,
                            new ModuleSize(1, 2)
                    )
                    .energy(new EnergyProperties(5))
                    .attributes(new AttributeProperties(Map.of(
                            ResourceLocation.parse("minecraft:generic.max_health"),
                            new AttributeModifierProperties(
                                    4.0D,
                                    AttributeModifier.Operation.ADD_VALUE
                            )
                    )))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_REVIVAL = REGISTRY.register(
            "test_revival",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.SURVIVAL,
                            new ModuleSize(2, 2)
                    )
                    .energy(new EnergyProperties(5, 1))
                    .revival(new RevivalProperties(4.0D, 600, 40))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_HUNGER = REGISTRY.register(
            "test_hunger",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.SURVIVAL,
                            new ModuleSize(1, 2)
                    )
                    .energy(new EnergyProperties(5))
                    .hunger(new HungerProperties(0.50D))
                    .build()
    );

    public static final EquipmentEntry<ModuleDefinition, ModuleItem> TEST_REGENERATION = REGISTRY.register(
            "test_regeneration",
            properties(),
            (id, properties) -> ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.SURVIVAL,
                            new ModuleSize(1, 2)
                    )
                    .energy(new EnergyProperties(5))
                    .regeneration(new RegenerationProperties(1.0D))
                    .build()
    );

    private static EquipmentProperties properties() {
        return new EquipmentProperties(
                EquipmentTier.BASIC,
                Rarity.EPIC
        );
    }
}
