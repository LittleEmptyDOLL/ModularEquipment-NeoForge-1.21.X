package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.ModList;

import java.util.Map;

final class ModSurvivalModules {
    private static final ModuleSize SMALL_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize MEDIUM_SIZE = new ModuleSize(2, 2);
    private static final ModuleSize LARGE_SIZE = new ModuleSize(2, 3);
    private static final ModuleSize HUGE_SIZE = new ModuleSize(3, 3);

    // Survival systems should remain powered before mobility and utility
    // modules, but ordinary survival assistance stays below hard defenses.
    private static final int SURVIVAL_PRIORITY = 7;
    private static final int TICKS_PER_MINUTE = 20 * 60;

    private static final ResourceLocation MAX_HEALTH =
            ResourceLocation.parse("minecraft:generic.max_health");
    private static final ResourceLocation OXYGEN_BONUS =
            ResourceLocation.parse("minecraft:generic.oxygen_bonus");

    private ModSurvivalModules() {}

    static void register(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerHealthModules(registry);
        registerLifeSupportModules(registry);
        registerRegenerationModules(registry);
        registerRevivals(registry);
        if (ModList.get() != null && ModList.get().isLoaded("legendarysurvivaloverhaul")) {
            registerBodyRegenerationModules(registry);
        }
        registerUnderwaterModules(registry);
    }

    private static void registerHealthModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerHealth(
                registry,
                "vitality_support",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                10,
                2.0D,
                SMALL_SIZE
        );
        registerHealth(
                registry,
                "combat_vitality_support",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                25,
                6.0D,
                MEDIUM_SIZE
        );
        registerHealth(
                registry,
                "experimental_vitality_support",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                40,
                10.0D,
                MEDIUM_SIZE
        );
    }

    private static void registerLifeSupportModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registry.register("metabolic_assist", new EquipmentProperties(EquipmentTier.CIVILIAN, Rarity.UNCOMMON),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.SURVIVAL, SMALL_SIZE)
                        .energy(new EnergyProperties(10, SURVIVAL_PRIORITY))
                        .hunger(new HungerProperties(0.15D))
                        .build()
        );

        if (ModList.get() != null && ModList.get().isLoaded("legendarysurvivaloverhaul")) {
            registry.register("hydration_assist", new EquipmentProperties(EquipmentTier.CIVILIAN, Rarity.UNCOMMON),
                    (resourceLocation, properties) -> ModuleDefinition.builder(
                                    resourceLocation, properties, ModuleCategory.SURVIVAL, SMALL_SIZE)
                            .energy(new EnergyProperties(10, SURVIVAL_PRIORITY))
                            .thirst(new ThirstProperties(0.15D))
                            .build());

            registry.register("life_support_system", new EquipmentProperties(EquipmentTier.ENGINEERING, Rarity.RARE),
                    (resourceLocation, properties) -> ModuleDefinition.builder(
                                    resourceLocation, properties, ModuleCategory.SURVIVAL, MEDIUM_SIZE)
                            .energy(new EnergyProperties(25, SURVIVAL_PRIORITY))
                            .hunger(new HungerProperties(0.25D))
                            .thirst(new ThirstProperties(0.25D))
                            .build()
            );
            registry.register("advanced_life_support", new EquipmentProperties(EquipmentTier.EXPERIMENTAL, Rarity.EPIC),
                    (resourceLocation, properties) -> ModuleDefinition.builder(
                                    resourceLocation, properties, ModuleCategory.SURVIVAL, MEDIUM_SIZE)
                            .energy(new EnergyProperties(50, SURVIVAL_PRIORITY))
                            .hunger(new HungerProperties(0.45D))
                            .thirst(new ThirstProperties(0.45D))
                            .build()
            );
        }
    }

    private static void registerRegenerationModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerRegeneration(
                registry,
                "engineering_regeneration",
                EquipmentTier.ENGINEERING,
                25,
                0.25D
        );
        registerRegeneration(
                registry,
                "military_combat_regeneration",
                EquipmentTier.MILITARY,
                60,
                0.50D
        );
    }

    private static void registerRevivals(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registerRevival(
                registry,
                "emergency_revival_system",
                EquipmentTier.MILITARY,
                Rarity.RARE,
                4.0D,
                10 * TICKS_PER_MINUTE,
                50,
                LARGE_SIZE
        );
        registerRevival(
                registry,
                "experimental_revival_system",
                EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC,
                6.0D,
                5 * TICKS_PER_MINUTE,
                100,
                HUGE_SIZE
        );
    }

    static void registerBodyRegenerationModules(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerBodyRegeneration(registry, "medical_recovery_system", EquipmentTier.ENGINEERING,
                35, 0.10D);
        registerBodyRegeneration(registry, "advanced_recovery_system", EquipmentTier.MILITARY,
                70, 0.25D);
    }

    private static void registerUnderwaterModules(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry
    ) {
        registry.register(
                "respiratory_support",
                new EquipmentProperties(EquipmentTier.ENGINEERING, Rarity.RARE),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        SMALL_SIZE
                                )
                                .energy(new EnergyProperties(
                                        20,
                                        SURVIVAL_PRIORITY
                                ))
                                .attributes(new AttributeProperties(
                                        Map.of(OXYGEN_BONUS, multipliedBase(0.25D)
                                        )
                                ))
                                .build()
        );
    }

    private static void registerHealth(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            int energyConsumption,
            double maxHealth,
            ModuleSize size
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        size
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

    private static void registerRegeneration(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            int energyConsumption,
            double healthPerSecond
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, Rarity.RARE),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        MEDIUM_SIZE
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

    private static void registerRevival(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id,
            EquipmentTier tier,
            Rarity rarity,
            double restoreHealth,
            int cooldown,
            int energyConsumption,
            ModuleSize size
    ) {
        registry.register(
                id,
                new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) ->
                        ModuleDefinition.builder(
                                        resourceLocation,
                                        properties,
                                        ModuleCategory.SURVIVAL,
                                        size
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

    private static void registerBodyRegeneration(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier,
            int consumption, double healthPerSecond
    ) {
        registry.register(id, new EquipmentProperties(tier, Rarity.RARE),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties, ModuleCategory.SURVIVAL, LARGE_SIZE)
                        .energy(new EnergyProperties(consumption, SURVIVAL_PRIORITY))
                        .bodyDamageRegeneration(new BodyDamageRegenerationProperties(healthPerSecond))
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
}
