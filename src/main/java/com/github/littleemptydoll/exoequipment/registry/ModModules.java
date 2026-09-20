package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModModules {
    private ModModules() {}

    public static final DeferredRegister<ModuleDefinition> MODULES =
            DeferredRegister.create(
                    ModRegistryKeys.MODULE_REGISTRY,
                    ExoEquipment.MODID
            );

    private static final EquipmentRegistry<
            ModuleDefinition,
            ModuleItem
    > REGISTRY =
            new EquipmentRegistry<>(
                    MODULES,
                    ModItems.ITEMS,
                    "_module",
                    ModuleItem::new
            );

    public static ModuleDefinition getDefinition(
            ResourceLocation id
    ) {
        return REGISTRY.getDefinition(id);
    }

    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > find(
            ResourceLocation id
    ) {
        return REGISTRY.find(id);
    }

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_NIGHT_VISION = REGISTRY.register(
            "test_night_vision",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.SENSOR,
                                    new ModuleSize(1, 1)
                            )
                            .energy(new EnergyProperties(5))
                            .nightVision(new NightVisionProperties())
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_ENTITY_DETECTION = REGISTRY.register(
            "test_entity_detection",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.SENSOR,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10))
                            .entityDetection(new EntityDetectionProperties(
                                    16.0D,
                                    true,
                                    true,
                                    true
                            ))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST = REGISTRY.register(
            "test",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                            id,
                            properties,
                            ModuleCategory.UTILITY,
                            new ModuleSize(1, 2)
                    ).build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST_CONSUMER = REGISTRY.register(
            "test_consumer",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
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
                                    Optional.of(new TemperatureBonus(20, 40, 0.5))))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST_GENERATOR = REGISTRY.register(
            "test_generator",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
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
                                    Optional.of(new TemperatureBonus(60, 80, 1))))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
    > TEST_BATTERY = REGISTRY.register(
            "test_battery",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
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
                                    Optional.empty()))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_SHIELD = REGISTRY.register(
            "test_shield",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.DEFENSE,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(5, 1))
                            .shield(new ShieldProperties(
                                    50,
                                    1,
                                    100
                            ))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_STATUS_PROTECTION = REGISTRY.register(
            "test_status_protection",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.DEFENSE,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10, 1))
                            .statusProtection(new StatusProtectionProperties(
                                    java.util.Map.of(
                                            ResourceLocation.fromNamespaceAndPath(
                                                    "minecraft",
                                                    "poison"
                                            ),
                                            0.75D,
                                            ResourceLocation.fromNamespaceAndPath(
                                                    "minecraft",
                                                    "wither"
                                            ),
                                            1.0D
                                    )
                            ))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_DAMAGE_REDUCTION = REGISTRY.register(
            "test_damage_reduction",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.DEFENSE,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10, 1))
                            .damageReduction(new DamageReductionProperties(
                                    java.util.Map.of(
                                            ResourceLocation.fromNamespaceAndPath(
                                                    "minecraft",
                                                    "arrow"
                                            ),
                                            0.25D,
                                            ResourceLocation.fromNamespaceAndPath(
                                                    "minecraft",
                                                    "explosion"
                                            ),
                                            1.0D
                                    )
                            ))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_MOBILITY = REGISTRY.register(
            "test_mobility",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.MOBILITY,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(5))
                            .attributes(new AttributeProperties(Map.ofEntries(
                                    Map.entry(ResourceLocation.parse("minecraft:generic.movement_speed"),
                                            new AttributeModifierProperties(0.15D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.water_movement_efficiency"),
                                            new AttributeModifierProperties(0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.jump_strength"),
                                            new AttributeModifierProperties(0.20D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.step_height"),
                                            new AttributeModifierProperties(0.50D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE))
                            )))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_COMBAT = REGISTRY.register(
            "test_combat",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.COMBAT,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10))
                            .attributes(new AttributeProperties(Map.ofEntries(
                                    Map.entry(ResourceLocation.parse("minecraft:generic.attack_damage"),
                                            new AttributeModifierProperties(4.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.attack_speed"),
                                            new AttributeModifierProperties(1.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.attack_knockback"),
                                            new AttributeModifierProperties(0.5D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.entity_interaction_range"),
                                            new AttributeModifierProperties(4.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:crit_chance"),
                                            new AttributeModifierProperties(0.10D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:crit_damage"),
                                            new AttributeModifierProperties(0.50D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:armor_pierce"),
                                            new AttributeModifierProperties(2.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:armor_shred"),
                                            new AttributeModifierProperties(0.10D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:prot_pierce"),
                                            new AttributeModifierProperties(1.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:prot_shred"),
                                            new AttributeModifierProperties(0.10D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:current_hp_damage"),
                                            new AttributeModifierProperties(0.05D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:life_steal"),
                                            new AttributeModifierProperties(0.10D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:overheal"),
                                            new AttributeModifierProperties(0.05D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:fire_damage"),
                                            new AttributeModifierProperties(1.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:cold_damage"),
                                            new AttributeModifierProperties(1.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:projectile_damage"),
                                            new AttributeModifierProperties(0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:arrow_damage"),
                                            new AttributeModifierProperties(0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:arrow_velocity"),
                                            new AttributeModifierProperties(0.10D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:draw_speed"),
                                            new AttributeModifierProperties(0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE))
                            )))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_UTILITY = REGISTRY.register(
            "test_utility",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.UTILITY,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10))
                            .attributes(new AttributeProperties(Map.ofEntries(
                                    Map.entry(ResourceLocation.parse("minecraft:generic.block_break_speed"),
                                            new AttributeModifierProperties(0.50D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.block_interaction_range"),
                                            new AttributeModifierProperties(1.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.burning_time"),
                                            new AttributeModifierProperties(0.50D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.luck"),
                                            new AttributeModifierProperties(2.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.mining_efficiency"),
                                            new AttributeModifierProperties(1.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.submerged_mining_speed"),
                                            new AttributeModifierProperties(0.50D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.movement_efficiency"),
                                            new AttributeModifierProperties(0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.water_movement_efficiency"),
                                            new AttributeModifierProperties(0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.oxygen_bonus"),
                                            new AttributeModifierProperties(2.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.safe_fall_distance"),
                                            new AttributeModifierProperties(2.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("minecraft:generic.step_height"),
                                            new AttributeModifierProperties(0.50D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:experience_gained"),
                                            new AttributeModifierProperties(0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:healing_received"),
                                            new AttributeModifierProperties(0.25D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)),
                                    Map.entry(ResourceLocation.parse("apothic_attributes:dodge_chance"),
                                            new AttributeModifierProperties(0.10D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE))
                            )))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_PICKUP_MAGNET = REGISTRY.register(
            "test_pickup_magnet",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
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

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_COOLER = REGISTRY.register(
            "test_cooler",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.THERMAL,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10))
                            .thermal(new ThermalProperties(0, 20))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_HEATER = REGISTRY.register(
            "test_heater",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.THERMAL,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10))
                            .thermal(new ThermalProperties(20, 0))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_HEALTH = REGISTRY.register(
            "test_health",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.SURVIVAL,
                                    new ModuleSize(1, 2)
                            )
                            .energy(new EnergyProperties(5))
                            .attributes(new AttributeProperties(Map.of(
                                    ResourceLocation.parse("minecraft:generic.max_health"),
                                    new AttributeModifierProperties(4.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)
                            )))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_REVIVAL = REGISTRY.register(
            "test_revival",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.SURVIVAL,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(5, 1))
                            .revival(new RevivalProperties(
                                    4.0D,
                                    600,
                                    40
                            ))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_HUNGER = REGISTRY.register(
            "test_hunger",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.SURVIVAL,
                                    new ModuleSize(1, 2)
                            )
                            .energy(new EnergyProperties(5))
                            .hunger(new HungerProperties(0.50D))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_REGENERATION = REGISTRY.register(
            "test_regeneration",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.SURVIVAL,
                                    new ModuleSize(1, 2)
                            )
                            .energy(new EnergyProperties(5))
                            .regeneration(new RegenerationProperties(1.0D))
                            .build()
    );

    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_FALL_PROTECTION = REGISTRY.register(
            "test_fall_protection",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.SURVIVAL,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(5))
                            .fallProtection(new FallProtectionProperties(0.50D))
                            .build()
    );


    public static final EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > TEST_BODY_DAMAGE_PROTECTION = REGISTRY.register(
            "test_body_damage_protection",
            new EquipmentProperties(
                    EquipmentTier.BASIC,
                    Rarity.EPIC
            ),
            (id, properties) ->
                    ModuleDefinition.builder(
                                    id,
                                    properties,
                                    ModuleCategory.DEFENSE,
                                    new ModuleSize(2, 2)
                            )
                            .energy(new EnergyProperties(10, 1))
                            .bodyDamageProtection(new BodyDamageProtectionProperties(
                                    0.30D,
                                    0.50D,
                                    Set.of(
                                            BodyPart.HEAD,
                                            BodyPart.CHEST
                                    )
                            ))
                            .build()
    );

    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > registerLsoTestBodyDamageRegeneration() {
        return REGISTRY.register(
                "test_body_damage_regeneration",
                new EquipmentProperties(
                        EquipmentTier.BASIC,
                        Rarity.EPIC
                ),
                (id, properties) ->
                        ModuleDefinition.builder(
                                        id,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        new ModuleSize(2, 2)
                                )
                                .energy(new EnergyProperties(5))
                                .bodyDamageRegeneration(new BodyDamageRegenerationProperties(1.0D))
                                .build()
        );
    }

    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > registerLsoTestTemperatureHotModifier() {
        return REGISTRY.register(
                "test_lso_temperature_hot_modifier",
                new EquipmentProperties(
                        EquipmentTier.BASIC,
                        Rarity.EPIC
                ),
                (id, properties) ->
                        ModuleDefinition.builder(
                                        id,
                                        properties,
                                        ModuleCategory.THERMAL,
                                        new ModuleSize(2, 2)
                                )
                                .energy(new EnergyProperties(5))
                                .temperatureModifier(new TemperatureModifierProperties(
                                        10.0D,
                                        0.25D,
                                        0.25D,
                                        0.25D
                                ))
                                .build()
        );
    }

    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > registerLsoTestTemperatureColdModifier() {
        return REGISTRY.register(
                "test_lso_temperature_cold_modifier",
                new EquipmentProperties(
                        EquipmentTier.BASIC,
                        Rarity.EPIC
                ),
                (id, properties) ->
                        ModuleDefinition.builder(
                                        id,
                                        properties,
                                        ModuleCategory.THERMAL,
                                        new ModuleSize(2, 2)
                                )
                                .energy(new EnergyProperties(5))
                                .temperatureModifier(new TemperatureModifierProperties(
                                        -10.0D,
                                        0.25D,
                                        0.25D,
                                        0.25D
                                ))
                                .build()
        );
    }


    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > registerLsoTestTemperatureImpact() {
        return REGISTRY.register(
                "test_lso_temperature_impact",
                new EquipmentProperties(
                        EquipmentTier.BASIC,
                        Rarity.EPIC
                ),
                (id, properties) ->
                        ModuleDefinition.builder(
                                        id,
                                        properties,
                                        ModuleCategory.THERMAL,
                                        new ModuleSize(2, 2)
                                )
                                .energy(new EnergyProperties(5))
                                .temperatureImpact(new TemperatureImpactProperties(0.50D))
                                .build()
        );
    }

    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > registerLsoTestThirst() {
        return REGISTRY.register(
                "test_lso_thirst",
                new EquipmentProperties(
                        EquipmentTier.BASIC,
                        Rarity.EPIC
                ),
                (id, properties) ->
                        ModuleDefinition.builder(
                                        id,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        new ModuleSize(1, 2)
                                )
                                .energy(new EnergyProperties(5))
                                .thirst(new ThirstProperties(0.50D))
                                .build()
        );
    }


    public static EquipmentEntry<
            ModuleDefinition,
            ModuleItem
            > registerLsoTestPainkiller() {
        return REGISTRY.register(
                "test_lso_painkiller",
                new EquipmentProperties(
                        EquipmentTier.BASIC,
                        Rarity.EPIC
                ),
                (id, properties) ->
                        ModuleDefinition.builder(
                                        id,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        new ModuleSize(2, 2)
                                )
                                .energy(new EnergyProperties(5))
                                .painkiller(new PainkillerProperties())
                                .build()
        );
    }

}
