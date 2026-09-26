package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.AttributeModifierProperties;
import com.github.littleemptydoll.exoequipment.module.AttributeProperties;
import com.github.littleemptydoll.exoequipment.module.EnergyProperties;
import com.github.littleemptydoll.exoequipment.module.HungerProperties;
import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.github.littleemptydoll.exoequipment.module.ModuleDefinition;
import com.github.littleemptydoll.exoequipment.module.ModuleSize;
import com.github.littleemptydoll.exoequipment.module.RegenerationProperties;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;

import java.util.Map;

final class ModSurvivalModules {
    private static final ModuleSize HEALTH_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize HUNGER_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize REGENERATION_SIZE = new ModuleSize(2, 2);

    // Survival systems should remain powered before mobility and utility
    // modules, but ordinary survival assistance stays below hard defenses.
    private static final int SURVIVAL_PRIORITY = 7;

    private static final ResourceLocation MAX_HEALTH =
            ResourceLocation.parse("minecraft:generic.max_health");

    private ModSurvivalModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerHealthModules(registry);
        registerHungerModules(registry);
        registerRegenerationModules(registry);
    }

    private static void registerHealthModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerHealth(
                registry,
                "civilian_health",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                3,
                2.0D
        );
        registerHealth(
                registry,
                "engineering_health",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                5,
                4.0D
        );
        registerHealth(
                registry,
                "military_health",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                8,
                6.0D
        );
        registerHealth(
                registry,
                "experimental_health",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                12,
                10.0D
        );
    }

    private static void registerHungerModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerHunger(
                registry,
                "civilian_metabolic_assist",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                2,
                0.10D
        );
        registerHunger(
                registry,
                "engineering_metabolic_assist",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                3,
                0.20D
        );
        registerHunger(
                registry,
                "military_metabolic_assist",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                5,
                0.30D
        );
        registerHunger(
                registry,
                "experimental_metabolic_assist",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                8,
                0.45D
        );
    }

    private static void registerRegenerationModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerRegeneration(
                registry,
                "civilian_regeneration",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                5,
                0.10D
        );
        registerRegeneration(
                registry,
                "engineering_regeneration",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                8,
                0.25D
        );
        registerRegeneration(
                registry,
                "military_regeneration",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                12,
                0.50D
        );
        registerRegeneration(
                registry,
                "experimental_regeneration",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                20,
                1.00D
        );
    }

    private static void registerHealth(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int energyConsumption,
            double maxHealth
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        HEALTH_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        SURVIVAL_PRIORITY
                                ))
                                .attributes(new AttributeProperties(
                                        Map.of(
                                                MAX_HEALTH,
                                                new AttributeModifierProperties(
                                                        maxHealth,
                                                        AttributeModifier.Operation.ADD_VALUE
                                                )
                                        )
                                ))
                                .build()
        );
    }

    private static void registerHunger(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int energyConsumption,
            double exhaustionReduction
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        HUNGER_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        SURVIVAL_PRIORITY
                                ))
                                .hunger(new HungerProperties(
                                        exhaustionReduction
                                ))
                                .build()
        );
    }

    private static void registerRegeneration(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int energyConsumption,
            double healthPerSecond
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        REGENERATION_SIZE
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        SURVIVAL_PRIORITY
                                ))
                                .regeneration(new RegenerationProperties(
                                        healthPerSecond
                                ))
                                .build()
        );
    }
}
