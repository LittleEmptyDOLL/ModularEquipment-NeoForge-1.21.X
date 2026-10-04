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

final class ModDefenseModules {
    private static final ModuleSize SMALL_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize MEDIUM_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize LARGE_SIZE = new ModuleSize(2, 3);

    private static final int PROTECTION_PRIORITY = 8;
    private static final int IMMUNITY_PRIORITY = 9;
    private static final int SHIELD_PROTECTION_PRIORITY = 8;
    private static final int TICKS_PER_MINUTE = 20 * 60;

    private static final ResourceLocation IS_PROJECTILE = minecraftTag("is_projectile");
    private static final ResourceLocation IS_EXPLOSION = minecraftTag("is_explosion");
    private static final ResourceLocation IS_FIRE = minecraftTag("is_fire");
    private static final ResourceLocation IS_FALL = minecraftTag("is_fall");
    private static final ResourceLocation IS_DROWNING = minecraftTag("is_drowning");
    private static final ResourceLocation IS_FREEZING = minecraftTag("is_freezing");
    private static final ResourceLocation IS_LIGHTNING = minecraftTag("is_lightning");

    private static final ResourceLocation BYPASSES_EFFECTS = minecraftTag("bypasses_effects");
    private static final ResourceLocation BYPASSES_ENCHANTMENTS = minecraftTag("bypasses_enchantments");
    private static final ResourceLocation BYPASSES_INVULNERABILITY = minecraftTag("bypasses_invulnerability");

    private static final ResourceLocation ARMOR =
            ResourceLocation.parse("minecraft:generic.armor");
    private static final ResourceLocation ARMOR_TOUGHNESS =
            ResourceLocation.parse("minecraft:generic.armor_toughness");
    private static final ResourceLocation SAFE_FALL_DISTANCE =
            ResourceLocation.parse("minecraft:generic.safe_fall_distance");
    private static final ResourceLocation FALL_DAMAGE_MULTIPLIER =
            ResourceLocation.parse("minecraft:generic.fall_damage_multiplier");
    private static final ResourceLocation KNOCKBACK_RESISTANCE =
            ResourceLocation.parse("minecraft:generic.knockback_resistance");
    private static final ResourceLocation EXPLOSION_KNOCKBACK_RESISTANCE =
            ResourceLocation.parse("minecraft:generic.explosion_knockback_resistance");
    private static final ResourceLocation DODGE_CHANCE =
            ResourceLocation.parse("apothic_attributes:dodge_chance");

    private ModDefenseModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerShields(registry);
        registerDamageProtection(registry);
        registerImmunities(registry);
        registerEmergencyShields(registry);
        registerShieldProtection(registry);
        registerAttributeModules(registry);
    }

    private static void registerShields(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerShield(
                registry,
                "civilian_shield",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                4,
                0.025D,
                200,
                80
        );
        registerShield(
                registry,
                "engineering_shield",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                6,
                0.06D,
                120,
                130
        );
        registerShield(
                registry,
                "military_shield",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                10,
                0.035D,
                100,
                180
        );
        registerShield(
                registry,
                "experimental_shield",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                12,
                0.10D,
                80,
                300
        );
    }

    private static void registerDamageProtection(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        // General protection is weaker than specialized protection but applies
        // to almost every ordinary damage source.
        registerGeneralProtection(
                registry,
                "military_protection",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                0.15D,
                100
        );
        registerGeneralProtection(
                registry,
                "experimental_protection",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                0.25D,
                150
        );

        registerSpecializedProtection(
                registry,
                "ballistic_protection",
                EquipmentTier.MILITARY,
                IS_PROJECTILE
        );
        registerSpecializedProtection(
                registry,
                "blast_protection",
                EquipmentTier.MILITARY,
                IS_EXPLOSION
        );
        registerSpecializedProtection(
                registry,
                "fire_suppression",
                EquipmentTier.ENGINEERING,
                IS_FIRE
        );
    }

    private static void registerImmunities(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        // Absolute defenses remain narrow and expensive instead of turning the
        // general protection module into universal immunity.
        registerImmunity(
                registry,
                "experimental_fall_immunity",
                IS_FALL,
                180
        );
        registerImmunity(
                registry,
                "experimental_drowning_immunity",
                IS_DROWNING,
                160
        );
        registerImmunity(
                registry,
                "experimental_freezing_immunity",
                IS_FREEZING,
                160
        );
        registerImmunity(
                registry,
                "experimental_lightning_immunity",
                IS_LIGHTNING,
                200
        );
    }

    private static void registerEmergencyShields(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerEmergencyShield(
                registry,
                "military_emergency_shield",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                0.75D,
                3 * TICKS_PER_MINUTE,
                15
        );
        registerEmergencyShield(
                registry,
                "experimental_emergency_shield",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                1.0D,
                2 * TICKS_PER_MINUTE,
                20
        );
    }

    private static void registerShieldProtection(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerShieldProtection(
                registry,
                "engineering_shield_reinforcement",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                0.40D,
                30
        );
        registerShieldProtection(
                registry,
                "military_shield_reinforcement",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                0.70D,
                50
        );
        registerShieldProtection(
                registry,
                "advanced_shield_reinforcement",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                1.0D,
                80
        );
    }

    private static void registerGeneralProtection(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            double reduction,
            int energyConsumption
    ) {
        DamageReductionProperties damageReduction =
                new DamageReductionProperties(
                        Optional.of(reduction),
                        Map.of(),
                        Map.of(
                                BYPASSES_EFFECTS, 0.0D,
                                BYPASSES_ENCHANTMENTS, 0.0D,
                                BYPASSES_INVULNERABILITY, 0.0D
                        )
                );

        registerDamageReduction(
                registry,
                id,
                tier,
                rarity,
                ModDefenseModules.LARGE_SIZE,
                damageReduction,
                energyConsumption,
                PROTECTION_PRIORITY
        );
    }

    private static void registerSpecializedProtection(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            ResourceLocation damageTag
    ) {
        registerDamageReduction(
                registry,
                id,
                tier,
                Rarity.RARE,
                MEDIUM_SIZE,
                new DamageReductionProperties(
                        Map.of(),
                        Map.of(damageTag, 0.3)
                ),
                60,
                PROTECTION_PRIORITY
        );
    }

    private static void registerImmunity(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            ResourceLocation damageTag,
            int energyConsumption
    ) {
        registerDamageReduction(
                registry,
                id,
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                LARGE_SIZE,
                new DamageReductionProperties(
                        Map.of(),
                        Map.of(damageTag, 1.0D)
                ),
                energyConsumption,
                IMMUNITY_PRIORITY
        );
    }

    private static void registerDamageReduction(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            ModuleSize size,
            DamageReductionProperties damageReduction,
            int energyConsumption,
            int priority
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        size
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        priority
                                ))
                                .damageReduction(damageReduction)
                                .build()
        );
    }

    private static void registerShield(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int capacity,
            double rechargeRate,
            int rechargeDelay,
            int energyConsumption
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        MEDIUM_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        10
                                ))
                                .shield(new ShieldProperties(
                                        capacity,
                                        rechargeRate,
                                        rechargeDelay
                                ))
                                .build()
        );
    }

    private static void registerEmergencyShield(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            double restore,
            int cooldown,
            int energyConsumption
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        MEDIUM_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        9
                                ))
                                .emergencyShield(
                                        new EmergencyShieldProperties(
                                                restore,
                                                cooldown
                                        )
                                )
                                .build()
        );
    }

    private static void registerShieldProtection(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            double transfer,
            int energyConsumption
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        SMALL_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        SHIELD_PROTECTION_PRIORITY
                                ))
                                .shieldProtection(
                                        new ShieldProtectionProperties(
                                                transfer
                                        )
                                )
                                .build()
        );
    }

    static void registerAttributeModules(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerAttributeModule(
                registry,
                "civilian_armor_plating",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                15,
                Map.ofEntries(Map.entry(ARMOR, addValue(3.0D)))
        );
        registerAttributeModule(
                registry,
                "military_reinforced_plating",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                35,
                Map.ofEntries(
                        Map.entry(ARMOR, addValue(6.0D)),
                        Map.entry(ARMOR_TOUGHNESS, addValue(2.0D))
                )
        );

        registry.register("military_reactive_armor",
                new EquipmentProperties(EquipmentTier.MILITARY, Rarity.RARE),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.DEFENSE,
                                        LARGE_SIZE)
                                .energy(new EnergyProperties(30, PROTECTION_PRIORITY))
                                .attributes(new AttributeProperties(Map.of(ARMOR, addValue(2.0D))))
                                .conditionalAttributes(new ConditionalAttributeProperties(
                                        List.of(new AttributeCondition(
                                                AttributeCondition.Type.HEALTH_BELOW, Optional.of(0.30D))),
                                        new AttributeProperties(Map.ofEntries(
                                                Map.entry(ARMOR, addValue(5.0D)),
                                                Map.entry(ARMOR_TOUGHNESS, addValue(3.0D))
                                        )))
                                )
                                .build());

        registerAttributeModule(
                registry,
                "impact_protection_system",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                30,
                Map.ofEntries(
                        Map.entry(SAFE_FALL_DISTANCE, addValue(3.0D)),
                        Map.entry(FALL_DAMAGE_MULTIPLIER, multipliedBase(-0.25D))
                )
        );
        registerAttributeModule(
                registry,
                "kinetic_stabilization",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                30,
                Map.ofEntries(
                        Map.entry(KNOCKBACK_RESISTANCE, multipliedBase(0.25D)),
                        Map.entry(EXPLOSION_KNOCKBACK_RESISTANCE, multipliedBase(0.25D)),
                        Map.entry(FALL_DAMAGE_MULTIPLIER, multipliedBase(-0.5D))
                )
        );

        if (ModList.get() != null && ModList.get().isLoaded("apothic_attributes")) {
            registerAttributeModule(
                    registry,
                    "evasion_system",
                    EquipmentTier.MILITARY,
                    Rarity.RARE,
                    40,
                    Map.ofEntries(
                            Map.entry(DODGE_CHANCE, multipliedBase(0.05D))
                    )
            );
        }
    }

    private static ResourceLocation minecraftTag(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                path
        );
    }

    private static void registerAttributeModule(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
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
                                        ModuleCategory.DEFENSE,
                                        ModDefenseModules.MEDIUM_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        PROTECTION_PRIORITY
                                ))
                                .attributes(new AttributeProperties(entries))
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
