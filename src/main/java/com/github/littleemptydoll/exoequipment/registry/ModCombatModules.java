package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class ModCombatModules {
    private static final ModuleSize SMALL_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize MEDIUM_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize LARGE_SIZE = new ModuleSize(2, 3);
    private static final int COMBAT_PRIORITY = 4;

    private static final ResourceLocation ATTACK_DAMAGE =
            ResourceLocation.parse("minecraft:generic.attack_damage");
    private static final ResourceLocation ATTACK_SPEED =
            ResourceLocation.parse("minecraft:generic.attack_speed");
    private static final ResourceLocation ATTACK_KNOCKBACK =
            ResourceLocation.parse("minecraft:generic.attack_knockback");
    private static final ResourceLocation ENTITY_INTERACTION_RANGE =
            ResourceLocation.parse("minecraft:generic.entity_interaction_range");
    private static final ResourceLocation CRIT_CHANCE =
            ResourceLocation.parse("apothic_attributes:crit_chance");
    private static final ResourceLocation CRIT_DAMAGE =
            ResourceLocation.parse("apothic_attributes:crit_damage");
    private static final ResourceLocation ARMOR_PIERCE =
            ResourceLocation.parse("apothic_attributes:armor_pierce");
    private static final ResourceLocation ARMOR_SHRED =
            ResourceLocation.parse("apothic_attributes:armor_shred");
    private static final ResourceLocation PROTECTION_PIERCE =
            ResourceLocation.parse("apothic_attributes:protection_pierce");
    private static final ResourceLocation PROTECTION_SHRED =
            ResourceLocation.parse("apothic_attributes:protection_shred");
    private static final ResourceLocation ARROW_DAMAGE =
            ResourceLocation.parse("apothic_attributes:arrow_damage");
    private static final ResourceLocation ARROW_VELOCITY =
            ResourceLocation.parse("apothic_attributes:arrow_velocity");
    private static final ResourceLocation DRAW_SPEED =
            ResourceLocation.parse("apothic_attributes:draw_speed");
    private static final ResourceLocation COLD_DAMAGE =
            ResourceLocation.parse("apothic_attributes:cold_damage");
    private static final ResourceLocation FIRE_DAMAGE =
            ResourceLocation.parse("apothic_attributes:fire_damage");
    private static final ResourceLocation CURRENT_HP_DAMAGE =
            ResourceLocation.parse("apothic_attributes:current_hp_damage");
    private static final ResourceLocation LIFE_STEAL =
            ResourceLocation.parse("apothic_attributes:life_steal");
    private static final ResourceLocation OVERHEAL =
            ResourceLocation.parse("apothic_attributes:overheal");

    private ModCombatModules() {}

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerAttributeModule(
                registry,
                "damage_actuator",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                SMALL_SIZE,
                15,
                Map.ofEntries(Map.entry(ATTACK_DAMAGE, multipliedBase(0.10D)))
        );
        registerAttributeModule(
                registry,
                "attack_servo",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                SMALL_SIZE,
                10,
                Map.ofEntries(Map.entry(ATTACK_SPEED, multipliedBase(0.10D)))
        );
        registerAttributeModule(
                registry,
                "combat_actuator",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                MEDIUM_SIZE,
                20,
                Map.ofEntries(
                        Map.entry(ATTACK_DAMAGE, multipliedBase(0.075D)),
                        Map.entry(ATTACK_SPEED, multipliedBase(0.075D))
                )
        );
        registerAttributeModule(
                registry,
                "impact_amplifier",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                SMALL_SIZE,
                10,
                Map.ofEntries(Map.entry(ATTACK_KNOCKBACK, multipliedBase(0.10D)))
        );

        registerAttributeModule(
                registry,
                "high_power_actuator",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                SMALL_SIZE,
                30,
                Map.ofEntries(Map.entry(ATTACK_DAMAGE, multipliedBase(0.25D)))
        );

        registerAttributeModule(
                registry,
                "advanced_attack_servo",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                SMALL_SIZE,
                25,
                Map.ofEntries(Map.entry(ATTACK_SPEED, multipliedBase(0.20D)))
        );
        registerAttributeModule(
                registry,
                "combat_processor",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                MEDIUM_SIZE,
                35,
                Map.ofEntries(
                        Map.entry(ATTACK_DAMAGE, multipliedBase(0.15D)),
                        Map.entry(ATTACK_SPEED, multipliedBase(0.15D))
                )
        );

        registerAttributeModule(
                registry,
                "experimental_damage_actuator",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                SMALL_SIZE,
                60,
                Map.ofEntries(Map.entry(ATTACK_DAMAGE, multipliedBase(0.40D)))
        );
        registerAttributeModule(
                registry,
                "experimental_attack_servo",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                SMALL_SIZE,
                55,
                Map.ofEntries(Map.entry(ATTACK_SPEED, multipliedBase(0.35D)))
        );
        registerAttributeModule(
                registry,
                "long_sword",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                SMALL_SIZE,
                40,
                Map.ofEntries(Map.entry(ENTITY_INTERACTION_RANGE, addValue(1.0D)))
        );

        if (ModList.get() != null && ModList.get().isLoaded("apothic_attributes")) {
            registerAttributeModule(
                    registry,
                    "critical_targeting",
                    EquipmentTier.MILITARY,
                    Rarity.RARE,
                    SMALL_SIZE,
                    30,
                    Map.ofEntries(Map.entry(CRIT_CHANCE, multipliedBase(0.15D)))
            );
            registerAttributeModule(
                    registry,
                    "critical_amplifier",
                    EquipmentTier.MILITARY,
                    Rarity.RARE,
                    SMALL_SIZE,
                    35,
                    Map.ofEntries(Map.entry(CRIT_DAMAGE, multipliedBase(0.30D)))
            );
            registerAttributeModule(
                    registry,
                    "heavy_target_system",
                    EquipmentTier.MILITARY,
                    Rarity.RARE,
                    MEDIUM_SIZE,
                    45,
                    Map.ofEntries(Map.entry(CURRENT_HP_DAMAGE, multipliedBase(0.05D)))
            );
            registerAttributeModule(
                    registry,
                    "combat_recovery",
                    EquipmentTier.MILITARY,
                    Rarity.RARE,
                    MEDIUM_SIZE,
                    50,
                    Map.ofEntries(Map.entry(LIFE_STEAL, multipliedBase(0.05D)))
            );
            registerAttributeModule(
                    registry,
                    "armor_penetrator",
                    EquipmentTier.MILITARY,
                    Rarity.RARE,
                    MEDIUM_SIZE,
                    40,
                    Map.ofEntries(
                            Map.entry(ARMOR_PIERCE, multipliedBase(0.20D)),
                            Map.entry(PROTECTION_PIERCE, multipliedBase(0.10D))
                    )
            );

            registerAttributeModule(
                    registry,
                    "defense_disruptor",
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE,
                    MEDIUM_SIZE,
                    40,
                    Map.ofEntries(
                            Map.entry(ARMOR_SHRED, multipliedBase(0.15D)),
                            Map.entry(PROTECTION_SHRED, multipliedBase(0.10D))
                    )
            );
            registerAttributeModule(
                    registry,
                    "marksman_system",
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE,
                    MEDIUM_SIZE,
                    40,
                    Map.ofEntries(
                            Map.entry(ARROW_DAMAGE, multipliedBase(0.15D)),
                            Map.entry(ARROW_VELOCITY, multipliedBase(0.15D))
                    )
            );
            registerAttributeModule(
                    registry,
                    "draw_speed",
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE,
                    SMALL_SIZE,
                    20,
                    Map.ofEntries(Map.entry(DRAW_SPEED, multipliedBase(0.15D)))
            );
            registerAttributeModule(
                    registry,
                    "precision_targeting",
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE,
                    MEDIUM_SIZE,
                    40,
                    Map.ofEntries(
                            Map.entry(CRIT_CHANCE, multipliedBase(0.10D)),
                            Map.entry(CRIT_DAMAGE, multipliedBase(0.20D))
                    )
            );

            registerAttributeModule(
                    registry,
                    "critical_strike_system",
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC,
                    MEDIUM_SIZE,
                    80,
                    Map.ofEntries(
                            Map.entry(CRIT_CHANCE, multipliedBase(0.20D)),
                            Map.entry(CRIT_DAMAGE, multipliedBase(0.40D))
                    )
            );
            registerAttributeModule(
                    registry,
                    "adaptive_penetrator",
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC,
                    MEDIUM_SIZE,
                    90,
                    Map.ofEntries(
                            Map.entry(ARMOR_PIERCE, multipliedBase(0.25D)),
                            Map.entry(PROTECTION_PIERCE, multipliedBase(0.15D)),
                            Map.entry(ARMOR_SHRED, multipliedBase(0.10D)),
                            Map.entry(PROTECTION_SHRED, multipliedBase(0.05D))
                    )
            );
            registerAttributeModule(
                    registry,
                    "predator_system",
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC,
                    LARGE_SIZE,
                    100,
                    Map.ofEntries(
                            Map.entry(LIFE_STEAL, multipliedBase(0.075D)),
                            Map.entry(OVERHEAL, multipliedBase(0.10D))
                    )
            );
            registerAttributeModule(
                    registry,
                    "experimental_combat_processor",
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC,
                    LARGE_SIZE,
                    90,
                    Map.ofEntries(
                            Map.entry(ATTACK_DAMAGE, multipliedBase(0.25D)),
                            Map.entry(ATTACK_SPEED, multipliedBase(0.20D)),
                            Map.entry(CRIT_CHANCE, multipliedBase(0.10D))
                    )
            );

            registerAttributeThermalModule(
                    registry,
                    "incendiary_attack_system",
                    EquipmentTier.MILITARY,
                    Rarity.RARE,
                    MEDIUM_SIZE,
                    40,
                    Map.ofEntries(Map.entry(FIRE_DAMAGE, addValue(4.0D))),
                    new ThermalProperties(5,0)
            );
            registerAttributeThermalModule(
                    registry,
                    "cryogenic_attack_system",
                    EquipmentTier.ENGINEERING,
                    Rarity.RARE,
                    MEDIUM_SIZE,
                    40,
                    Map.ofEntries(Map.entry(COLD_DAMAGE, addValue(4.0D))),
                    new ThermalProperties(0,5)
            );
            registerAttributeModule(
                    registry,
                    "burning_cold_attack_system",
                    EquipmentTier.EXPERIMENTAL,
                    Rarity.EPIC,
                    LARGE_SIZE,
                    100,
                    Map.ofEntries(
                            Map.entry(FIRE_DAMAGE, addValue(4.0D)),
                            Map.entry(COLD_DAMAGE, addValue(4.0D))
                    )
            );
        }

        registerLaser(registry, "rapid_laser_defense", EquipmentTier.ENGINEERING,
                Rarity.RARE, MEDIUM_SIZE, 10, 3, 6, 900);
        registerLaser(registry, "personal_laser_defense", EquipmentTier.MILITARY,
                Rarity.RARE, MEDIUM_SIZE, 12, 5, 12, 1_500);
        registerLaser(registry, "heavy_laser_defense", EquipmentTier.MILITARY,
                Rarity.RARE, LARGE_SIZE, 16, 9, 20, 3_500);
        registerLaser(registry, "experimental_laser_defense", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, LARGE_SIZE, 20, 7, 8, 3_000);

        registerDischarge(registry, "crowd_discharge_system", EquipmentTier.ENGINEERING,
                Rarity.RARE, LARGE_SIZE, 5, 4, 5, 0.70F, 2, 3, 50, 10_000);
        registerDischarge(registry, "discharge_defense", EquipmentTier.MILITARY,
                Rarity.RARE, LARGE_SIZE, 6, 5, 8, 0.75F, 1, 3, 60, 8_000);
        registerDischarge(registry, "experimental_arc_defense", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, new ModuleSize(3, 3), 7, 6, 10, 0.80F, 2, 5, 40, 25_000);

        registerAdrenaline(registry);
    }

    private static void registerLaser(EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
                                      String id, EquipmentTier tier, Rarity rarity, ModuleSize size,
                                      double range, float damage, int cooldown, int energyCost) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (key, properties) -> ModuleDefinition.builder(key, properties, ModuleCategory.COMBAT, size)
                        .laserDefense(new LaserDefenseProperties(range, damage, cooldown, energyCost,
                                false, true, true))
                        .build());
    }

    private static void registerDischarge(EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
                                          String id, EquipmentTier tier, Rarity rarity, ModuleSize size,
                                          double range, double jumpRange, float damage, float falloff,
                                          int targets, int bounces, int cooldown, int energyCost) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (key, properties) -> ModuleDefinition.builder(key, properties, ModuleCategory.COMBAT, size)
                        .dischargeDefense(new DischargeDefenseProperties(range, jumpRange, damage,
                                falloff, targets, bounces, cooldown, energyCost, false, true, true))
                        .build());
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
                                        ModuleCategory.COMBAT,
                                        size
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        COMBAT_PRIORITY
                                ))
                                .attributes(new AttributeProperties(entries))
                                .build()
        );
    }

    private static void registerAttributeThermalModule(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            ModuleSize size,
            int energyConsumption,
            Map<ResourceLocation, AttributeModifierProperties> entries,
            ThermalProperties thermalProperties
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.COMBAT,
                                        size
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        COMBAT_PRIORITY
                                ))
                                .attributes(new AttributeProperties(entries))
                                .thermal(thermalProperties)
                                .build()
        );
    }

    private static void registerAdrenaline(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registry.register("adrenaline",
                new EquipmentProperties(EquipmentTier.MILITARY, Rarity.RARE),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.COMBAT,
                                        MEDIUM_SIZE)
                                .energy(new EnergyProperties(30, 4))
                                .conditionalAttributes(new ConditionalAttributeProperties(
                                        List.of(new AttributeCondition(
                                                AttributeCondition.Type.HEALTH_BELOW, Optional.of(0.30D))),
                                        new AttributeProperties(Map.ofEntries(
                                                Map.entry(ATTACK_DAMAGE, multipliedBase(0.25D)),
                                                Map.entry(ATTACK_SPEED, multipliedBase(0.15D))
                                        )))
                                )
                                .build());
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
