package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.EmergencyShieldProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.RevivalProperties;
import com.github.littleemptydoll.exoequipment.module.ShieldProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.world.item.Rarity;

final class ModDefenseModules {
    private static final ModuleSize SHIELD_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize EMERGENCY_SHIELD_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize REVIVAL_SIZE = new ModuleSize(2, 3);

    private static final int TICKS_PER_MINUTE = 20 * 60;

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
                100,
                4
        );
        registerShield(
                registry,
                "engineering_shield",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                6,
                0.05D,
                80,
                8
        );
        registerShield(
                registry,
                "military_shield",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                8,
                0.04D,
                60,
                10
        );
        registerShield(
                registry,
                "experimental_shield",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                12,
                0.10D,
                40,
                16
        );

        // An emergency shield represents an additional shield charge rather
        // than a larger primary shield. It starts at the mid tiers and the
        // main progression axis is recharge time.
        registerEmergencyShield(
                registry,
                "engineering_emergency_shield",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                4 * TICKS_PER_MINUTE,
                6
        );
        registerEmergencyShield(
                registry,
                "military_emergency_shield",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                3 * TICKS_PER_MINUTE,
                8
        );
        registerEmergencyShield(
                registry,
                "experimental_emergency_shield",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                2 * TICKS_PER_MINUTE,
                12
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
                8
        );
        registerRevival(
                registry,
                "military_revival",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                4.0D,
                10 * TICKS_PER_MINUTE,
                12
        );
        registerRevival(
                registry,
                "experimental_revival",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                6.0D,
                5 * TICKS_PER_MINUTE,
                20
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
                                                1.0D,
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
}
