package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.DamageReductionProperties;
import com.github.littleemptydoll.exoequipment.module.EmergencyShieldProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.RevivalProperties;
import com.github.littleemptydoll.exoequipment.module.ShieldProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

import java.util.Map;
import java.util.Optional;

final class ModDefenseModules {
    private static final ModuleSize SHIELD_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize EMERGENCY_SHIELD_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize REVIVAL_SIZE = new ModuleSize(2, 3);
    private static final ModuleSize SPECIALIZED_PROTECTION_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize GENERAL_PROTECTION_SIZE = new ModuleSize(2, 3);
    private static final ModuleSize IMMUNITY_SIZE = new ModuleSize(2, 3);

    private static final int PROTECTION_PRIORITY = 8;
    private static final int IMMUNITY_PRIORITY = 9;
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

    private ModDefenseModules() {}

    static void register(
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
                20
        );
        registerShield(
                registry,
                "engineering_shield",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                6,
                0.05D,
                120,
                30
        );
        registerShield(
                registry,
                "military_shield",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                8,
                0.04D,
                100,
                35
        );
        registerShield(
                registry,
                "experimental_shield",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                12,
                0.10D,
                80,
                50
        );

        // General protection follows the same role as the vanilla Protection
        // enchantment: weaker than a specialized defense, but useful against
        // almost every ordinary damage source. Damage that explicitly bypasses
        // enchantment-like protection is intentionally excluded.
        registerGeneralProtection(
                registry,
                "civilian_protection",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                0.05D,
                30
        );
        registerGeneralProtection(
                registry,
                "engineering_protection",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                0.10D,
                60
        );
        registerGeneralProtection(
                registry,
                "military_protection",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                0.15D,
                90
        );
        registerGeneralProtection(
                registry,
                "experimental_protection",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                0.20D,
                150
        );

        // Specialized protection is deliberately about twice as effective as
        // general protection of the same tier, mirroring the trade-off used by
        // the vanilla protection enchantments.
        registerProtectionSeries(
                registry,
                "projectile_protection",
                IS_PROJECTILE
        );
        registerProtectionSeries(
                registry,
                "blast_protection",
                IS_EXPLOSION
        );
        registerProtectionSeries(
                registry,
                "fire_protection",
                IS_FIRE
        );

        // Experimental immunity modules are narrow but absolute defenses.
        // Their high constant power draw and larger footprint make them a
        // deliberate build choice rather than a direct upgrade to general
        // protection.
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

        // An emergency shield represents an additional shield charge rather
        // than a larger primary shield. It starts at the mid tiers and the
        // main progression axis is recharge time.
        registerEmergencyShield(
                registry,
                "engineering_emergency_shield",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                0.5D,
                4 * TICKS_PER_MINUTE,
                10
        );
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

        // Revival is intentionally unavailable at the civilian tier. Even
        // the experimental version keeps a five-minute minimum cooldown.
        registerRevival(
                registry,
                "engineering_revival",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                4.0D,
                15 * TICKS_PER_MINUTE,
                25
        );
        registerRevival(
                registry,
                "military_revival",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                4.0D,
                10 * TICKS_PER_MINUTE,
                35
        );
        registerRevival(
                registry,
                "experimental_revival",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                6.0D,
                5 * TICKS_PER_MINUTE,
                50
        );
    }

    private static void registerProtectionSeries(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String suffix,
            ResourceLocation damageTag
    ) {
        registerSpecializedProtection(
                registry,
                "civilian_" + suffix,
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                damageTag,
                0.10D,
                20
        );
        registerSpecializedProtection(
                registry,
                "engineering_" + suffix,
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                damageTag,
                0.20D,
                40
        );
        registerSpecializedProtection(
                registry,
                "military_" + suffix,
                EquipmentTier.MILITARY,
                Rarity.RARE,
                damageTag,
                0.30D,
                60
        );
        registerSpecializedProtection(
                registry,
                "experimental_" + suffix,
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                damageTag,
                0.40D,
                100
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
                GENERAL_PROTECTION_SIZE,
                damageReduction,
                energyConsumption,
                PROTECTION_PRIORITY
        );
    }

    private static void registerSpecializedProtection(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            ResourceLocation damageTag,
            double reduction,
            int energyConsumption
    ) {
        registerDamageReduction(
                registry,
                id,
                tier,
                rarity,
                SPECIALIZED_PROTECTION_SIZE,
                new DamageReductionProperties(
                        Map.of(),
                        Map.of(damageTag, reduction)
                ),
                energyConsumption,
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
                IMMUNITY_SIZE,
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
                                        SHIELD_SIZE
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
                                        EMERGENCY_SHIELD_SIZE
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

    private static void registerRevival(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            double restoreHealth,
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
                                        REVIVAL_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        10
                                ))
                                .revival(new RevivalProperties(
                                        restoreHealth,
                                        cooldown,
                                        40
                                ))
                                .build()
        );
    }

    private static ResourceLocation minecraftTag(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                "minecraft",
                path
        );
    }
}
