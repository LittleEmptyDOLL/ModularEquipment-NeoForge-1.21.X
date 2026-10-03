package com.github.littleemptydoll.exoequipment.registry;

import com.github.littleemptydoll.exoequipment.item.ModuleItem;
import com.github.littleemptydoll.exoequipment.module.*;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Rarity;

import java.util.Map;

final class ModUtilityModules {
    private static final ModuleSize SMALL_SIZE = new ModuleSize(1, 2);
    private static final ModuleSize MEDIUM_SIZE = new ModuleSize(2, 2);
    private static final int UTILITY_PRIORITY = 2;

    private ModUtilityModules() {}

    private static final ResourceLocation BLOCK_BREAK_SPEED =
            ResourceLocation.parse("minecraft:generic.block_break_speed");
    private static final ResourceLocation BLOCK_INTERACTION_RANGE =
            ResourceLocation.parse("minecraft:generic.block_interaction_range");
    private static final ResourceLocation SUBMERGED_MINING_SPEED =
            ResourceLocation.parse("minecraft:generic.submerged_mining_speed");
    private static final ResourceLocation LUCK =
            ResourceLocation.parse("minecraft:generic.luck");
    private static final ResourceLocation EXPERIENCE_GAINED =
            ResourceLocation.parse("apothic_attributes:experience_gained");

    static void register(EquipmentRegistry<ModuleDefinition, ModuleItem> registry) {
        registerMagnet(registry, "pickup_magnet", EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON, 15, 5.0D, PickupMagnetProperties.Mode.ITEMS);
        registerMagnet(registry, "advanced_pickup_magnet", EquipmentTier.ENGINEERING,
                Rarity.RARE, 25, 10.0D, PickupMagnetProperties.Mode.BOTH);

        registerCloaking(registry, "cloaking_system", EquipmentTier.MILITARY,
                Rarity.RARE, 20, 2500, 150, 200);
        registerCloaking(registry, "experimental_cloaking", EquipmentTier.EXPERIMENTAL,
                Rarity.EPIC, 40, 5000, 250, 100);

        registerAttributeModule(
                registry,
                "mining_assist",
                EquipmentTier.CIVILIAN,
                Rarity.UNCOMMON,
                MEDIUM_SIZE,
                15,
                Map.ofEntries(Map.entry(BLOCK_BREAK_SPEED, multipliedBase(0.15D)))
        );
        registerAttributeModule(
                registry,
                "advanced_mining_assist",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                MEDIUM_SIZE,
                30,
                Map.ofEntries(
                        Map.entry(BLOCK_BREAK_SPEED, multipliedBase(0.3D)),
                        Map.entry(BLOCK_INTERACTION_RANGE, addValue(1.0D))
                )
        );

        registerAttributeModule(
                registry,
                "underwater_work_system",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                MEDIUM_SIZE,
                25,
                Map.ofEntries(Map.entry(SUBMERGED_MINING_SPEED, multipliedBase(0.5D)))
        );

        registerAttributeModule(
                registry,
                "fortune_analysis_system",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                SMALL_SIZE,
                20,
                Map.ofEntries(Map.entry(LUCK, addValue(1.0D)))
        );

        registerAttributeModule(
                registry,
                "ancient_knowledge",
                EquipmentTier.ENGINEERING,
                Rarity.RARE,
                SMALL_SIZE,
                20,
                Map.ofEntries(Map.entry(EXPERIENCE_GAINED, multipliedBase(0.2D)))
        );
    }

    private static void registerMagnet(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int energyConsumption, double radius, PickupMagnetProperties.Mode mode
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties,
                                ModuleCategory.UTILITY, SMALL_SIZE
                        )
                        .energy(new EnergyProperties(energyConsumption, UTILITY_PRIORITY))
                        .pickupMagnet(new PickupMagnetProperties(radius, mode))
                        .build());
    }

    private static void registerCloaking(
            EquipmentRegistry<ModuleDefinition, ModuleItem> registry,
            String id, EquipmentTier tier, Rarity rarity,
            int energyConsumption, int activationEnergy,
            int activeConsumption, int cooldown
    ) {
        registry.register(id, new EquipmentProperties(tier, rarity),
                (resourceLocation, properties) -> ModuleDefinition.builder(
                                resourceLocation, properties,
                                ModuleCategory.UTILITY, MEDIUM_SIZE
                        )
                        .energy(new EnergyProperties(energyConsumption, UTILITY_PRIORITY))
                        .cloaking(new CloakingProperties(activationEnergy,
                                activeConsumption, cooldown))
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
                                        ModuleCategory.UTILITY,
                                        size
                                )
                                .energy(new EnergyProperties(
                                        energyConsumption,
                                        UTILITY_PRIORITY
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
